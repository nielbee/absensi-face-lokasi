package com.example.absensi.ui.face.analyzer

import android.graphics.*
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.face.Face
import java.io.ByteArrayOutputStream
import kotlin.math.abs

object FaceUtils {

    // ===============================
    // KONVERSI ImageProxy → Bitmap
    // ===============================
    fun toBitmap(imageProxy: ImageProxy): Bitmap {
        val yBuffer = imageProxy.planes[0].buffer
        val uBuffer = imageProxy.planes[1].buffer
        val vBuffer = imageProxy.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(
            nv21,
            ImageFormat.NV21,
            imageProxy.width,
            imageProxy.height,
            null
        )

        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(
            Rect(0, 0, imageProxy.width, imageProxy.height),
            100,
            out
        )

        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    // ===============================
    // VALIDASI WAJAH
    // ===============================
    fun isFacingCamera(face: Face): Boolean {
        return abs(face.headEulerAngleY) < 15 &&
                abs(face.headEulerAngleX) < 15
    }

    fun isFaceCloseEnough(face: Face): Boolean {
        return face.boundingBox.width() > 250
    }

    fun isBlinking(face: Face): Boolean {
        val left = face.leftEyeOpenProbability ?: return false
        val right = face.rightEyeOpenProbability ?: return false
        return left < 0.3f && right < 0.3f
    }

    fun isSmiling(face: Face): Boolean {
        return (face.smilingProbability ?: 0f) > 0.6f
    }
}
