package com.example.absensi.ui.face

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.absensi.MainActivity
import com.example.absensi.data.local.UserPreference
import com.example.absensi.data.local.pref.FacePreference
import com.example.absensi.databinding.ActivityFaceVerifyBinding
import com.example.absensi.ui.face.analyzer.FaceAnalyzer
import java.util.concurrent.Executors

class FaceVerifyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFaceVerifyBinding
    private lateinit var facePref: FacePreference
    private lateinit var userPref: UserPreference

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera()
        else {
            Toast.makeText(this, "Izin kamera diperlukan", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceVerifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        facePref = FacePreference(this)
        userPref = UserPreference(this)

        checkPermission()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
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

            imageAnalysis.setAnalyzer(
                Executors.newSingleThreadExecutor(),
                FaceAnalyzer(
                    context = this,
                    onStatus = { msg ->
                        runOnUiThread { binding.txtInfo.text = msg }
                    },
                    onCompleted = { currentEmbedding ->
                        runOnUiThread {
                            verifyFace(currentEmbedding)
                        }
                    }
                )
            )

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                preview,
                imageAnalysis
            )

        }, ContextCompat.getMainExecutor(this))
    }

    private fun verifyFace(currentEmbedding: FloatArray) {
        val id = userPref.getId()

        if (id.isEmpty()) {
            Toast.makeText(this, "ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val savedEmbedding = facePref.getFace(id)

        if (savedEmbedding == null) {
            Toast.makeText(this, "Wajah belum terdaftar", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val match = facePref.isFaceMatch(savedEmbedding, currentEmbedding)

        if (match) {
            Toast.makeText(this, "✅ Wajah cocok", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        } else {
            Toast.makeText(this, "❌ Wajah tidak cocok", Toast.LENGTH_SHORT).show()
        }
    }
}

