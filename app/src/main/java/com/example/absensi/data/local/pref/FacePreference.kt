package com.example.absensi.data.local.pref

import android.content.Context
import android.util.Log
import kotlin.math.sqrt

class FacePreference(context: Context) {

    private val prefs = context.getSharedPreferences("face_pref", Context.MODE_PRIVATE)

    fun saveFace(nisn: String, data: FloatArray) {
        val stringData = data.joinToString(",")
        prefs.edit().putString("face_$nisn", stringData).apply()
    }

    fun getFace(nisn: String): FloatArray? {
        val data = prefs.getString("face_$nisn", null) ?: return null
        return data.split(",").map { it.toFloat() }.toFloatArray()
    }

    // Fungsi baru untuk mengambil nilai ANGKA skor
    fun calculateSimilarity(emb1: FloatArray, emb2: FloatArray): Float {
        if (emb1.size != emb2.size) return 0f

        var dotProduct = 0.0f
        var normA = 0.0f
        var normB = 0.0f

        for (i in emb1.indices) {
            dotProduct += emb1[i] * emb2[i]
            normA += emb1[i] * emb1[i]
            normB += emb2[i] * emb2[i]
        }

        val denominator = sqrt(normA.toDouble()) * sqrt(normB.toDouble())
        if (denominator <= 0.0) return 0f

        return (dotProduct / denominator).toFloat()
    }

    // Gunakan threshold yang lebih fleksibel (default 0.45)
    fun isFaceMatch(emb1: FloatArray, emb2: FloatArray, threshold: Float = 0.45f): Boolean {
        val similarity = calculateSimilarity(emb1, emb2)
        return similarity >= threshold
    }
}