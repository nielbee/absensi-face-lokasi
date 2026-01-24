package com.example.absensi.ui.dashboard

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import com.example.absensi.data.local.pref.FacePreference
import com.example.absensi.data.local.UserPreference
import com.example.absensi.data.local.utils.toBitmap
import com.example.absensi.databinding.FragmentDashboardBinding
import com.example.absensi.remote.AbsensiResponse
import com.example.absensi.remote.ApiService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.sqrt
import com.google.android.gms.tasks.CancellationTokenSource
import android.location.Location
import com.example.absensi.remote.RetrofitClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

//class DashboardFragment : Fragment(), OnMapReadyCallback {
//
//    private var _binding: FragmentDashboardBinding? = null
//    private val binding get() = _binding!!
//
//    private lateinit var googleMap: GoogleMap
//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private lateinit var embeddingExtractor: Interpreter
//    private lateinit var facePref: FacePreference
//    private lateinit var userPref: UserPreference
//    private var storedEmbedding: FloatArray? = null
//    private var isFaceMatch = false
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // Inisialisasi interpreter TFLite
//        initEmbeddingExtractor()
//
//        facePref = FacePreference(requireContext())
//        userPref = UserPreference(requireContext())
//        fusedLocationClient =
//            LocationServices.getFusedLocationProviderClient(requireContext())
//
//        setupMap()
//        loadFaceEmbedding()
//        setupCamera()
//        setupAbsenButton()
//    }
//
//    private fun initEmbeddingExtractor() {
//        try {
//            val assetFileDescriptor = requireContext().assets.openFd("mobile_facenet.tflite")
//            val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
//            val fileChannel = inputStream.channel
//            val startOffset = assetFileDescriptor.startOffset
//            val declaredLength = assetFileDescriptor.declaredLength
//            val mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
//
//            embeddingExtractor = Interpreter(mappedByteBuffer)
//        } catch (e: Exception) {
//            Log.e("DashboardFragment", "Error init TFLite model: ${e.message}")
//        }
//    }
//
//
//    private fun setupMap() {
//        val mapFragment = childFragmentManager
//            .findFragmentById(R.id.mapFragment) as SupportMapFragment
//        mapFragment.getMapAsync(this)
//    }
//
//    override fun onMapReady(map: GoogleMap) {
//        googleMap = map
//        // Default posisi sementara
//        val lokasi = LatLng(-6.200000, 106.816666)
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lokasi, 16f))
//        // Coba tampilkan lokasi real
//        lifecycleScope.launch {
//            val loc = fusedLocationClient.getCurrentLocationSuspend(requireContext())
//            showCurrentLocationOnMap(loc)
//        }
//    }
//
//    private fun showCurrentLocationOnMap(location: Location?) {
//        location?.let {
//            val latLng = LatLng(it.latitude, it.longitude)
//            googleMap.clear()
//            googleMap.addMarker(MarkerOptions().position(latLng).title("Lokasi Saya"))
//            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
//        }
//    }
//
//    private fun loadFaceEmbedding() {
//        val nisn = userPref.getNisn()
//        storedEmbedding = facePref.getFace(nisn)
//    }
//
//    private fun setupCamera() {
//        if (ActivityCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.CAMERA
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                arrayOf(Manifest.permission.CAMERA),
//                1001
//            )
//            return
//        }
//
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
//        cameraProviderFuture.addListener({
//            val cameraProvider = cameraProviderFuture.get()
//
//            val preview = Preview.Builder()
//                .build()
//                .also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }
//
//            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
//
//            val analysis = ImageAnalysis.Builder()
//                .setTargetResolution(android.util.Size(112, 112))
//                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                .build()
//
//            analysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext())) { imageProxy ->
//                val bitmap = imageProxy.toBitmap()
//                bitmap?.let { checkFace(it) }
//                imageProxy.close()
//            }
//
//            try {
//                cameraProvider.unbindAll()
//                cameraProvider.bindToLifecycle(this, cameraSelector, preview, analysis)
//            } catch (e: Exception) {
//                Log.e("CameraX", "Bind failed", e)
//            }
//        }, ContextCompat.getMainExecutor(requireContext()))
//    }
//
//    private fun checkFace(bitmap: Bitmap) {
//        storedEmbedding?.let { stored ->
//            val embedding = extractEmbedding(bitmap)
//            isFaceMatch = facePref.isFaceMatch(stored, embedding)
//            updateAbsenButton()
//        }
//    }
//
////    private fun updateAbsenButton() {
////        binding.btnAbsen.isEnabled = isFaceMatch
////    }
//private fun updateAbsenButton() {
//    if (isFaceMatch) {
//        binding.btnAbsen.isEnabled = true
//        // Ubah warna jadi terang (misalnya hijau terang)
//        binding.btnAbsen.backgroundTintList =
//            ContextCompat.getColorStateList(requireContext(), R.color.teal_700)
//    } else {
//        binding.btnAbsen.isEnabled = false
//        // Kembalikan ke warna default (misalnya teal_200)
//        binding.btnAbsen.backgroundTintList =
//            ContextCompat.getColorStateList(requireContext(), R.color.teal_200)
//    }
//}
//
//    private fun setupAbsenButton() {
//        binding.btnAbsen.setOnClickListener {
//            lifecycleScope.launch(Dispatchers.Main) {
//                if (!isFaceMatch) {
//                    Toast.makeText(requireContext(), "Wajah tidak cocok!", Toast.LENGTH_SHORT).show()
//                    return@launch
//                }
//                sendAbsen()
//            }
//        }
//    }
//
//    private suspend fun sendAbsen() {
//        // Cek permission
//        if (ActivityCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED &&
//            ActivityCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            Toast.makeText(requireContext(), "Permission lokasi tidak diberikan!", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val location = try {
//            fusedLocationClient.getCurrentLocationSuspend(requireContext())
//        } catch (e: SecurityException) {
//            Toast.makeText(requireContext(), "Tidak bisa akses lokasi!", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val lat = location?.latitude ?: 0.0
//        val lng = location?.longitude ?: 0.0
//
//        val nisn = userPref.getNisn()
//        val apiKey = userPref.getApiKey()
//        val appKey = "asdjsandkjasvfamd"
//
//
//        val body = mapOf(
//            "nisn" to nisn,
//            "app_key" to appKey,
//            "token_key" to apiKey,
//            "lat" to lat,
//            "long" to lng
//        )
//
////        val response = RetrofitClient.api.absenDatang(nisn, body as Map<String, String>)
//
//
//        try {
//            val response = RetrofitClient.api.absenDatang(nisn, body as Map<String, String>)
//            val absensiResponse: AbsensiResponse? = response.body()
//            if (response.isSuccessful && absensiResponse != null)
//            { Toast.makeText(requireContext(), absensiResponse.msg, Toast.LENGTH_SHORT).show()
//                showCurrentLocationOnMap(location) }
//            else { Toast.makeText(requireContext(), "Gagal absen!", Toast.LENGTH_SHORT).show()
//            }
//        } catch (e: Exception) {
//            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//
//private fun extractEmbedding(bitmap: Bitmap): FloatArray {
//    // Resize ke ukuran input model
//    val resized = Bitmap.createScaledBitmap(bitmap, 112, 112, true)
//
//    // Buat buffer input
//    val input = ByteBuffer.allocateDirect(1 * 112 * 112 * 3 * 4).apply {
//        order(ByteOrder.nativeOrder())
//        for (y in 0 until 112) {
//            for (x in 0 until 112) {
//                val px = resized.getPixel(x, y)
//                // Normalisasi sesuai MobileFaceNet
//                putFloat(((px shr 16 and 0xFF) - 128f) / 128f) // R
//                putFloat(((px shr 8 and 0xFF) - 128f) / 128f)  // G
//                putFloat(((px and 0xFF) - 128f) / 128f)        // B
//            }
//        }
//    }
//
//    // Output sesuai dimensi model (192)
//    val output = Array(1) { FloatArray(192) }
//    embeddingExtractor.run(input, output)
//
//    return output[0]
//}
//
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}

class DashboardFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var embeddingExtractor: Interpreter
    private lateinit var facePref: FacePreference
    private lateinit var userPref: UserPreference
    private var storedEmbedding: FloatArray? = null
    private var isFaceMatch = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initEmbeddingExtractor()

        facePref = FacePreference(requireContext())
        userPref = UserPreference(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        checkLocationPermission()   // ✅ cek permission lokasi

        setupMap()
        loadFaceEmbedding()
        setupCamera()
        setupAbsenButton()
    }

    private fun initEmbeddingExtractor() {
        try {
            val afd = requireContext().assets.openFd("mobile_facenet.tflite")
            val inputStream = FileInputStream(afd.fileDescriptor)
            val fileChannel = inputStream.channel
            val mappedByteBuffer =
                fileChannel.map(FileChannel.MapMode.READ_ONLY, afd.startOffset, afd.declaredLength)
            embeddingExtractor = Interpreter(mappedByteBuffer)
        } catch (e: Exception) {
            Log.e("DashboardFragment", "Error init TFLite model: ${e.message}")
        }
    }

    // ✅ Tambahkan permission check
    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1002
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1002 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            lifecycleScope.launch {
                val loc = fusedLocationClient.getCurrentLocationSuspend(requireContext())
                showCurrentLocationOnMap(loc)
            }
        } else {
            Toast.makeText(requireContext(), "Permission lokasi ditolak!", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun setupMap() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val lokasi = LatLng(-6.200000, 106.816666)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lokasi, 16f))
        lifecycleScope.launch {
            try {
                val loc = fusedLocationClient.getCurrentLocationSuspend(requireContext())
                showCurrentLocationOnMap(loc)
            } catch (e: SecurityException) {
                Toast.makeText(requireContext(), "Lokasi tidak bisa diakses!", Toast.LENGTH_SHORT)
                    .show()
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

    private fun loadFaceEmbedding() {
        val nisn = userPref.getNisn()
        storedEmbedding = facePref.getFace(nisn)
    }

    private fun setupCamera() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                1001
            )
            return
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            val analysis = ImageAnalysis.Builder()
                .setTargetResolution(android.util.Size(112, 112))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext())) { imageProxy ->
                val bitmap = imageProxy.toBitmap()
                bitmap?.let { checkFace(it) }
                imageProxy.close()
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, analysis)
            } catch (e: Exception) {
                Log.e("CameraX", "Bind failed", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun checkFace(bitmap: Bitmap) {
        storedEmbedding?.let { stored ->
            val embedding = extractEmbedding(bitmap)
            isFaceMatch = facePref.isFaceMatch(stored, embedding)
            updateAbsenButton()
        }
    }

    // ✅ Update tombol + warna
    private fun updateAbsenButton() {
        if (isFaceMatch) {
            binding.btnAbsen.isEnabled = true
            binding.btnAbsen.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.teal_700)
        } else {
            binding.btnAbsen.isEnabled = false
            binding.btnAbsen.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.teal_200)
        }
    }

    private fun setupAbsenButton() {
        binding.btnAbsen.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                if (!isFaceMatch) {
                    Toast.makeText(requireContext(), "Wajah tidak cocok!", Toast.LENGTH_SHORT)
                        .show()
                    return@launch
                }
                sendAbsen()
            }
        }
    }
private suspend fun sendAbsen() {
    if (ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        Toast.makeText(requireContext(), "Permission lokasi tidak diberikan!", Toast.LENGTH_SHORT).show()
        return
    }

    val location = try {
        fusedLocationClient.getCurrentLocationSuspend(requireContext())
    } catch (e: SecurityException) {
        Toast.makeText(requireContext(), "Tidak bisa akses lokasi!", Toast.LENGTH_SHORT).show()
        return
    }

//    val lat = location?.latitude?.toString() ?: "0.0"
//    val lng = location?.longitude?.toString() ?: "0.0"
    val lat = "-8.47240444613784"
    val lng = "119.89182673243097"

    val nisn = userPref.getNisn()
    val apiKey = userPref.getApiKey()
    val appKey = "asdjsandkjasvfamd"

    val body = mapOf(
        "nisn" to nisn,
        "lat" to lat,
        "long" to lng,
        "app_key" to appKey,
        "token_key" to apiKey
    )

    try {
        val response = RetrofitClient.api.absenDatang(nisn, body)
        val absensiResponse: AbsensiResponse? = response.body()

        Log.d("DashboardFragment", "Request body: $body") // ✅ Log response detail
        Log.d("DashboardFragment", "Response code: ${response.code()}")
        Log.d("DashboardFragment", "Response message: ${response.message()}")
        Log.d("DashboardFragment", "Response raw: ${response.raw()}")
        Log.d("DashboardFragment", "Response body: $absensiResponse")

        if (response.isSuccessful && absensiResponse != null) {
            when (absensiResponse.status) {
                "berhasil" -> {
                    Toast.makeText(requireContext(), absensiResponse.msg, Toast.LENGTH_SHORT).show()
                    showCurrentLocationOnMap(location)
                }
                "peringatan" -> {
                    Toast.makeText(requireContext(), absensiResponse.msg, Toast.LENGTH_SHORT).show()
                    // Bisa juga disable tombol lagi kalau gagal
                    binding.btnAbsen.isEnabled = false
                    binding.btnAbsen.backgroundTintList =
                        ContextCompat.getColorStateList(requireContext(), R.color.teal_200)
                }
                else -> {
                    Toast.makeText(requireContext(), "Response tidak dikenal!", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "Gagal absen!", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
    }

}
    private fun extractEmbedding(bitmap: Bitmap): FloatArray {
        val resized = Bitmap.createScaledBitmap(bitmap, 112, 112, true)
        val input = ByteBuffer.allocateDirect(1 * 112 * 112 * 3 * 4).apply {
            order(ByteOrder.nativeOrder())
            for (y in 0 until 112) {
                for (x in 0 until 112) {
                    val px = resized.getPixel(x, y)
                    putFloat(((px shr 16 and 0xFF) - 128f) / 128f)
                    putFloat(((px shr 8 and 0xFF) - 128f) / 128f)
                    putFloat(((px and 0xFF) - 128f) / 128f)
                }
            }
        }
        val output = Array(1) { FloatArray(192) }
        embeddingExtractor.run(input, output)
        return output[0]
    }

    override fun onDestroyView() { super.onDestroyView()
        _binding = null
        if (::embeddingExtractor.isInitialized) {
            embeddingExtractor.close()
        }

    }
}
// Extension suspend function
suspend fun FusedLocationProviderClient.getCurrentLocationSuspend(context: Context): Location? {
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        throw SecurityException("Location permission not granted")
    }

    return suspendCancellableCoroutine { cont ->
        val tokenSource = CancellationTokenSource()
        this.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
            .addOnSuccessListener { loc ->
                cont.resume(loc)
            }
            .addOnFailureListener { e ->
                cont.resumeWithException(e)
            }

        cont.invokeOnCancellation { tokenSource.cancel() }
    }
}


