package com.example.absensi.data.local.pref

import android.content.Context
import kotlin.math.sqrt

class FacePreference(context: Context) {

    private val prefs =
        context.getSharedPreferences("face_pref", Context.MODE_PRIVATE)

    fun saveFace(nisn: String, data: FloatArray) {
        val stringData = data.joinToString(",")
        prefs.edit()
            .putString("face_$nisn", stringData)
            .apply()
    }

    fun hasFace(nisn: String): Boolean {
        return prefs.contains("face_$nisn")
    }

    fun getFace(nisn: String): FloatArray? {
        val data = prefs.getString("face_$nisn", null) ?: return null
        return data.split(",").map { it.toFloat() }.toFloatArray()
    }

    fun isFaceMatch(
        emb1: FloatArray,
        emb2: FloatArray,
        threshold: Float = 0.9f
    ): Boolean {
        var sum = 0f
        for (i in emb1.indices) {
            val diff = emb1[i] - emb2[i]
            sum += diff * diff
        }
        val distance = sqrt(sum)
        return distance < threshold
    }
}
