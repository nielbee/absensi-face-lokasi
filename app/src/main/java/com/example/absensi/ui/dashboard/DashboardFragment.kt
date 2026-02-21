package com.example.absensi.ui.dashboard

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.absensi.R
import com.example.absensi.data.local.UserPreference
import com.example.absensi.data.local.pref.FacePreference
import com.example.absensi.databinding.FragmentDashboardBinding
import com.example.absensi.remote.RetrofitClient
import com.example.absensi.ui.face.analyzer.FaceAnalyzer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class DashboardFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var facePref: FacePreference
    private lateinit var userPref: UserPreference
    private lateinit var cameraExecutor: ExecutorService

    private var faceAnalyzer: FaceAnalyzer? = null

    private var isFaceMatched = false
    private var isLocationMatch = false

    companion object {
        private const val SEKOLAH_LAT = -8.47240444613784
        private const val SEKOLAH_LNG = 119.89182673243097
        private const val RADIUS_MAKSIMAL = 100.0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        facePref = FacePreference(requireContext())
        userPref = UserPreference(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        cameraExecutor = Executors.newSingleThreadExecutor()

        checkPermissions()
        setupMap()
        setupCamera()
        setupAbsenButton()
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val listPermissionsNeeded = permissions.filter {
            ContextCompat.checkSelfPermission(
                requireContext(),
                it
            ) != PackageManager.PERMISSION_GRANTED
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                listPermissionsNeeded.toTypedArray(),
                1001
            )
        }
    }

    private fun setupMap() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        checkLocationStatus()
    }

    private fun checkLocationStatus() {
        lifecycleScope.launch {
            try {
                val loc: Location? = fusedLocationClient.getCurrentLocationSuspend(requireContext())
                showCurrentLocationOnMap(loc)

                loc?.let { location ->
                    val results = FloatArray(1)
                    Location.distanceBetween(
                        location.latitude,
                        location.longitude,
                        SEKOLAH_LAT,
                        SEKOLAH_LNG,
                        results
                    )
                    val distance = results[0]

                    isLocationMatch = distance <= RADIUS_MAKSIMAL
                    if (!isLocationMatch) {
                        binding.tvStatus.text = "📍 Di luar radius sekolah"
                    }
                    updateAbsenButton(isFaceMatched && isLocationMatch)
                }
            } catch (e: Exception) {
                Log.e("LOKASI_CHECK", "Error: ${e.message}")
            }
        }
    }

    private fun showCurrentLocationOnMap(location: Location?) {
        location?.let {
            val latLng = LatLng(it.latitude, it.longitude)
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(latLng).title("Lokasi Saya"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
        }
    }

    private fun setupCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            faceAnalyzer = FaceAnalyzer(
                context = requireContext(),
                onStatus = { status ->
                    activity?.runOnUiThread { binding.tvStatus?.text = status }
                },
                onCompleted = { embedding ->
                    activity?.runOnUiThread { verifyFace(embedding) }
                }
            )

            imageAnalysis.setAnalyzer(cameraExecutor, faceAnalyzer!!)

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                Log.e("Camera", "Binding failed", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun verifyFace(currentEmbedding: FloatArray) {
        val nisn = userPref.getNisn()
        val savedEmbedding = facePref.getFace(nisn)

        if (savedEmbedding != null) {
            val score = facePref.calculateSimilarity(currentEmbedding, savedEmbedding)
            Log.d("FACE_SCORE", "Skor Kemiripan (Angka): $score")

            // Menggunakan threshold 0.45 agar lebih akurat namun tetap user-friendly
            val isMatched = facePref.isFaceMatch(currentEmbedding, savedEmbedding, 0.45f)

            if (isMatched) {
                binding.tvStatus?.text = "✅ Wajah Cocok (${String.format("%.2f", score)})"
                binding.tvStatus?.setBackgroundResource(R.drawable.bg_status_badege_success)
                isFaceMatched = true
                updateAbsenButton(isFaceMatched && isLocationMatch)
            } else {
                binding.tvStatus?.text = "❌ Tidak Cocok (${String.format("%.2f", score)})"
                isFaceMatched = false
                updateAbsenButton(false)

                lifecycleScope.launch {
                    delay(2000)
                    faceAnalyzer?.reset()
                }
            }
        } else {
            Log.e("FACE_SCORE", "Data wajah tidak ditemukan untuk NISN: $nisn")
        }
    }

    private fun updateAbsenButton(enabled: Boolean) {
        val color = if (enabled) R.color.teal_700 else R.color.teal_200
        val colorStateList = ContextCompat.getColorStateList(requireContext(), color)

        // Update tombol Datang
        binding.btnAbsenDatang?.apply {
            isEnabled = enabled
            backgroundTintList = colorStateList
        }

        // Update tombol Pulang
        binding.btnAbsenPulang?.apply {
            isEnabled = enabled
            backgroundTintList = colorStateList
        }
    }

    private fun setupAbsenButton() {
        // Tombol Datang
        binding.btnAbsenDatang?.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                sendAbsen(isDatang = true)
            }
        }

        // Tombol Pulang
        binding.btnAbsenPulang?.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                sendAbsen(isDatang = false)
            }
        }
    }


    private suspend fun sendAbsen(isDatang: Boolean) {
        val location = try {
            fusedLocationClient.getCurrentLocationSuspend(requireContext())
        } catch (e: Exception) {
            null
        }

        // cek mock location :
        if(location.isMock == true){
            Toast.makeText(requireContext(), "Mock Location Detected", Toast.LENGTH_SHORT).show()
            return
        }


        val lat = location?.latitude?.toString() ?: "0.0"
        val lng = location?.longitude?.toString() ?: "0.0"
        val nisn = userPref.getNisn()
        val apiKey = userPref.getApiKey()

        val body = mapOf(
            "nisn" to nisn,
            "lat" to lat,
            "long" to lng,
            "app_key" to "asdjsandkjasvfamd",
            "token_key" to apiKey
        )

        try {
            // Logika pemilihan endpoint API
            val response = if (isDatang) {
                RetrofitClient.api.absenDatang(nisn, body)
            } else {
                RetrofitClient.api.absenPulang(nisn, body)
            }

            if (response.isSuccessful && response.body() != null) {
                val res = response.body()
                Toast.makeText(requireContext(), res?.msg, Toast.LENGTH_SHORT).show()

                if (res?.status == "berhasil") {
                    // Setelah sukses, kunci kembali tombol dan reset kamera
                    isFaceMatched = false
                    updateAbsenButton(false)
                    faceAnalyzer?.reset()

                    binding.tvStatus?.text = "Mencari Wajah..."
                    binding.tvStatus?.setBackgroundResource(R.drawable.bg_status_badge)
                } else {
                    faceAnalyzer?.reset()
                }
            } else {
                Toast.makeText(requireContext(), "Gagal: ${response.message()}", Toast.LENGTH_SHORT)
                    .show()
                faceAnalyzer?.reset()
            }
        } catch (e: Exception) {
            Log.e("ABSEN_ERROR", "Error: ${e.message}")
            Toast.makeText(requireContext(), "Koneksi Error", Toast.LENGTH_SHORT).show()
            faceAnalyzer?.reset()
        }
    }
}

// Extension Function dipindah ke level top-level (di luar class DashboardFragment)
suspend fun FusedLocationProviderClient.getCurrentLocationSuspend(context: Context): Location? {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        throw SecurityException("Location permission not granted")
    }

    return suspendCancellableCoroutine { cont ->
        val tokenSource = CancellationTokenSource()
        this.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
            .addOnSuccessListener { loc ->
                if (cont.isActive) cont.resume(loc)
            }
            .addOnFailureListener { e ->
                if (cont.isActive) cont.resumeWithException(e)
            }

        cont.invokeOnCancellation { tokenSource.cancel() }
    }
}