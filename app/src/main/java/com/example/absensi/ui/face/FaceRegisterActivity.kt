package com.example.absensi.ui.face

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import com.example.absensi.MainActivity
import com.example.absensi.data.local.UserPreference
import com.example.absensi.data.local.pref.FacePreference
import com.example.absensi.databinding.ActivityFaceRegisterBinding
import com.example.absensi.remote.RetrofitClient
import com.example.absensi.ui.login.LoginViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.absensi.ui.face.analyzer.FaceAnalyzer

class FaceRegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFaceRegisterBinding
    private lateinit var userPref: UserPreference
    private lateinit var facePref: FacePreference
    private lateinit var loginViewModel: LoginViewModel

    private var tempEmbedding: FloatArray? = null
private val requestCameraPermission = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted ->
    if (isGranted) {
        startCamera()
    } else {
        Toast.makeText(this, "Izin kamera diperlukan", Toast.LENGTH_LONG).show()
        finish()
    }
}

    private fun checkCameraPermission() {
    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        startCamera()
    } else {
        requestCameraPermission.launch(Manifest.permission.CAMERA)
    }
}

    override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityFaceRegisterBinding.inflate(layoutInflater)
    setContentView(binding.root)

    userPref = UserPreference(this)
    facePref = FacePreference(this)
    loginViewModel = LoginViewModel(RetrofitClient.api, userPref)

    checkCameraPermission() //  WAJIB
}

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            //  INI BAGIAN PALING PENTING
            imageAnalysis.setAnalyzer(
                Executors.newSingleThreadExecutor(),
                FaceAnalyzer(
                    context = this,
                    onStatus = { msg ->
                        runOnUiThread {
                            binding.txtInfo.text = msg
                        }
                    },
                    onCompleted = { embedding ->
                        runOnUiThread {
                            saveFaceAndLogin(embedding)
                        }
                    }
                )
            )

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalysis
            )

        }, ContextCompat.getMainExecutor(this))
    }


private fun saveFaceAndLogin(embedding: FloatArray) {
    val id_guru = userPref.getId()
    if (id_guru.isEmpty()) {
        Toast.makeText(this, "NISN tidak tersedia", Toast.LENGTH_SHORT).show()
        return
    }

    // 1️⃣ Simpan wajah
    facePref.saveFace(id_guru, embedding)
    userPref.saveLogin(id_guru)
    // 2️⃣ Popup sukses
    Toast.makeText(this, "✅ Wajah berhasil terdaftar", Toast.LENGTH_SHORT).show()
    // 3️⃣ Delay 2 detik → login otomatis
    lifecycleScope.launch {
        kotlinx.coroutines.delay(2000)

        loginViewModel.loginOtomatisLangsung(
            id_guru,
            onDone = { loginResponse ->
                // Bisa pakai loginResponse jika perlu
                startActivity(Intent(this@FaceRegisterActivity, MainActivity::class.java))
                finishAffinity()
            },
            onError = { error ->
                Toast.makeText(this@FaceRegisterActivity, error, Toast.LENGTH_SHORT).show()
            }
        )
    }
}


}
