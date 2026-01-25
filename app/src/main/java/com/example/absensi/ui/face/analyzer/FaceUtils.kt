package com.example.absensi.ui.face.analyzer

import android.graphics.*
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.face.Face
import java.io.ByteArrayOutputStream
import kotlin.math.abs

object FaceUtils {

    fun toBitmap(imageProxy: ImageProxy): Bitmap {
        val yBuffer = imageProxy.planes[0].buffer
        val uBuffer = imageProxy.planes[1].buffer
        val vBuffer = imageProxy.planes[2].buffer

        val nv21 = ByteArray(yBuffer.remaining() + uBuffer.remaining() + vBuffer.remaining())
        yBuffer.get(nv21, 0, yBuffer.remaining())
        vBuffer.get(nv21, yBuffer.remaining(), vBuffer.remaining())
        uBuffer.get(nv21, yBuffer.remaining() + vBuffer.remaining(), uBuffer.remaining())

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 100, out)
        val imageBytes = out.toByteArray()
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

        val matrix = Matrix()
        matrix.postRotate(270f) // Sesuaikan jika kamera terbalik
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun cropFace(bitmap: Bitmap, face: Face): Bitmap {
        val rect = face.boundingBox
        // Margin tambahan agar wajah tidak terlalu mepet saat di-crop
        val margin = 20
        val left = maxOf(rect.left - margin, 0)
        val top = maxOf(rect.top - margin, 0)
        val width = if (left + rect.width() + (margin*2) <= bitmap.width) rect.width() + (margin*2) else bitmap.width - left
        val height = if (top + rect.height() + (margin*2) <= bitmap.height) rect.height() + (margin*2) else bitmap.height - top

        return Bitmap.createBitmap(bitmap, left, top, width, height)
    }

    // Fungsi tambahan untuk membandingkan wajah (Cosine Similarity)
//    fun calculateSimilarity(vec1: FloatArray, vec2: FloatArray): Float {
//        var dotProduct = 0.0f
//        var normA = 0.0f
//        var normB = 0.0f
//        for (i in vec1.indices) {
//            dotProduct += vec1[i] * vec2[i]
//            normA += vec1[i] * vec1[i]
//            normB += vec2[i] * vec2[i]
//        }
//        return dotProduct / (Math.sqrt(normA.toDouble()).toFloat() * Math.sqrt(normB.toDouble()).toFloat())
//    }
// Tambahkan ini ke dalam object FaceUtils di FaceUtils.kt
    fun calculateSimilarity(vec1: FloatArray, vec2: FloatArray): Float {
        var dotProduct = 0.0f
        var normA = 0.0f
        var normB = 0.0f
        for (i in vec1.indices) {
            dotProduct += vec1[i] * vec2[i]
            normA += vec1[i] * vec1[i]
            normB += vec2[i] * vec2[i]
        }
        return dotProduct / (Math.sqrt(normA.toDouble()).toFloat() * Math.sqrt(normB.toDouble()).toFloat())
    }
    fun isFacingCamera(face: Face): Boolean = abs(face.headEulerAngleY) < 15 && abs(face.headEulerAngleX) < 15
    fun isFaceCloseEnough(face: Face): Boolean = face.boundingBox.width() > 200
    fun isBlinking(face: Face): Boolean = (face.leftEyeOpenProbability ?: 1f) < 0.2f && (face.rightEyeOpenProbability ?: 1f) < 0.2f
    fun isSmiling(face: Face): Boolean = (face.smilingProbability ?: 0f) > 0.6f
}