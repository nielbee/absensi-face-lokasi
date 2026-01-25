package com.example.absensi.ui.face.analyzer

import android.content.Context
import android.graphics.Bitmap
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.io.File
import java.io.FileOutputStream
import java.nio.FloatBuffer
import java.util.Collections

class EmbeddingExtractor(context: Context) {

    private val ortEnv: OrtEnvironment = OrtEnvironment.getEnvironment()
    private val ortSession: OrtSession

    init {
        // Memuat model dari assets ke cache untuk efisiensi memori (mencegah OOM)
        val modelPath = getModelPath(context, "arcface.onnx")
        ortSession = ortEnv.createSession(modelPath)
    }

    private fun getModelPath(context: Context, fileName: String): String {
        val modelFile = File(context.cacheDir, fileName)
        if (!modelFile.exists()) {
            context.assets.open(fileName).use { inputStream ->
                FileOutputStream(modelFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
        return modelFile.absolutePath
    }

    fun extract(bitmap: Bitmap): FloatArray {
        // ArcFace input size adalah 112x112
        val resized = Bitmap.createScaledBitmap(bitmap, 112, 112, true)
        val floatBuffer = FloatBuffer.allocate(1 * 3 * 112 * 112)
        floatBuffer.rewind()

        val pixels = IntArray(112 * 112)
        resized.getPixels(pixels, 0, 112, 0, 0, 112, 112)

        // Normalisasi ArcFace: (x - 127.5) / 128.0
        // Format NCHW: RRR...GGG...BBB...
        for (i in 0 until 112 * 112) {
            val px = pixels[i]
            floatBuffer.put(i, ((px shr 16 and 0xFF) - 127.5f) / 128f)           // R
            floatBuffer.put(i + 112 * 112, ((px shr 8 and 0xFF) - 127.5f) / 128f) // G
            floatBuffer.put(i + 2 * 112 * 112, ((px and 0xFF) - 127.5f) / 128f)   // B
        }

        val inputName = ortSession.inputNames.iterator().next()
        val shape = longArrayOf(1, 3, 112, 112)
        val inputTensor = OnnxTensor.createTensor(ortEnv, floatBuffer, shape)

        val results = ortSession.run(Collections.singletonMap(inputName, inputTensor))
        val output = results[0].value as Array<FloatArray>

        inputTensor.close()
        results.close()

        return output[0] // Mengembalikan 512-D embedding
    }
}