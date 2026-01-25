package com.example.absensi.ui.face.analyzer

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class EmbeddingExtractorxyz(context: Context) {

    private val interpreter: Interpreter

    init {

    Log.d("ASSET", context.assets.list("")!!.joinToString())

        val assetFileDescriptor = context.assets.openFd("mobile_facenet.tflite")
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength

        val buffer = fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            startOffset,
            declaredLength
        )

        interpreter = Interpreter(buffer)
    }

    fun extract(bitmap: Bitmap): FloatArray {

        // MobileFaceNet input: 112x112
        val resized = Bitmap.createScaledBitmap(bitmap, 112, 112, true)

        val input = ByteBuffer.allocateDirect(1 * 112 * 112 * 3 * 4)
        input.order(ByteOrder.nativeOrder())

        for (y in 0 until 112) {
            for (x in 0 until 112) {
                val px = resized.getPixel(x, y)

                input.putFloat(((px shr 16 and 0xFF) - 128f) / 128f)
                input.putFloat(((px shr 8 and 0xFF) - 128f) / 128f)
                input.putFloat(((px and 0xFF) - 128f) / 128f)
            }
        }

        val output = Array(1) { FloatArray(192) }
        interpreter.run(input, output)

        return output[0]
    }
}
