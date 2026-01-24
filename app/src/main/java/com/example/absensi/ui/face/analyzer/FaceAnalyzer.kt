package com.example.absensi.ui.face.analyzer

import android.content.Context
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

    // ✅ INIT DI SINI
    private val extractor = EmbeddingExtractor(context)

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .enableTracking()
            .build()
    )

    override fun analyze(imageProxy: ImageProxy) {
        if (finished.get()) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image ?: run {
            imageProxy.close(); return
        }

        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        detector.process(image)
            .addOnSuccessListener { faces ->

                if (faces.isEmpty()) {
                    onStatus("❌ Wajah tidak terdeteksi")
                    return@addOnSuccessListener
                }

                if (faces.size > 1) {
                    onStatus("❌ Pastikan hanya 1 wajah")
                    return@addOnSuccessListener
                }

                val face = faces.first()

                if (!FaceUtils.isFacingCamera(face)) {
                    onStatus("❌ Hadap ke kamera")
                    return@addOnSuccessListener
                }

                if (!FaceUtils.isFaceCloseEnough(face)) {
                    onStatus("❌ Dekatkan wajah")
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

                        onStatus("👁 Kedip mata 2x ($blinkCount/2)")

                        if (blinkCount >= 2) {
                            state = LivenessState.WAIT_SMILE
                        }
                    }

                    LivenessState.WAIT_SMILE -> {
                        if (FaceUtils.isSmiling(face)) {
                            state = LivenessState.COMPLETED
                        } else {
                            onStatus("😄 Silakan tersenyum")
                        }
                    }

                    LivenessState.COMPLETED -> {
                        finished.set(true)
                        onStatus("📸 Wajah berhasil direkam")

                        val bitmap = FaceUtils.toBitmap(imageProxy)
                        val embedding = extractor.extract(bitmap)

                        onCompleted(embedding)
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}


