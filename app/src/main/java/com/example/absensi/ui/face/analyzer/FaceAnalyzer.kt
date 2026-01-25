package com.example.absensi.ui.face.analyzer

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.*
import java.util.concurrent.atomic.AtomicBoolean

class FaceAnalyzer(
    private val context: Context,
    private val onStatus: (String) -> Unit,
    private val onCompleted: (FloatArray) -> Unit
) : ImageAnalysis.Analyzer {

    private var blinkCount = 0
    private var lastBlink = false
    private var state = LivenessState.WAIT_FACE
    private val finished = AtomicBoolean(false)
    private val extractor = EmbeddingExtractor(context)

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
    )

    // ✅ FUNGSI RESET: Penting agar bisa digunakan kembali jika absen gagal
    fun reset() {
        finished.set(false)
        state = LivenessState.WAIT_FACE
        blinkCount = 0
        lastBlink = false
        onStatus("Mencari wajah...")
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        // Jika sudah selesai dan belum di-reset, jangan proses frame baru
        if (finished.get()) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image ?: run { imageProxy.close(); return }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isEmpty()) {
                    onStatus("❌ Wajah tidak terdeteksi")
                    return@addOnSuccessListener
                }

                val face = faces.first()
                Log.d("FACE_DEBUG", "Blink: ${face.leftEyeOpenProbability}, Smile: ${face.smilingProbability}")
                if (!FaceUtils.isFacingCamera(face)) {
                    onStatus("❌ Hadap ke kamera")
                    return@addOnSuccessListener
                }

                when (state) {
                    LivenessState.WAIT_FACE -> {
                        state = LivenessState.WAIT_BLINK
                    }

                    LivenessState.WAIT_BLINK -> {
                        val blink = FaceUtils.isBlinking(face)
                        if (blink && !lastBlink) {
                            blinkCount++
                            lastBlink = true
                        }
                        if (!blink) lastBlink = false

                        onStatus("👁 Kedip mata ($blinkCount/2)")

                        if (blinkCount >= 2) state = LivenessState.WAIT_SMILE
                    }

                    LivenessState.WAIT_SMILE -> {
                        if (FaceUtils.isSmiling(face)) {
                            state = LivenessState.COMPLETED
                        } else {
                            onStatus("😄 Silakan tersenyum")
                        }
                    }

                    LivenessState.COMPLETED -> {
                        // Kunci analyzer agar tidak mengambil gambar berkali-kali
                        if (finished.compareAndSet(false, true)) {
                            onStatus("📸 Mengambil data wajah...")

                            val fullBitmap = FaceUtils.toBitmap(imageProxy)
                            val faceBitmap = FaceUtils.cropFace(fullBitmap, face)
                            val embedding = extractor.extract(faceBitmap)

                            onCompleted(embedding)
                        }
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}