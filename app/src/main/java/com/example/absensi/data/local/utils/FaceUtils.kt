package com.example.absensi.data.local.utils

fun calculateCosineSimilarity(a: FloatArray, b: FloatArray): Float {
    var dot = 0f
    var normA = 0f
    var normB = 0f
    for (i in a.indices) {
        dot += a[i] * b[i]
        normA += a[i] * a[i]
        normB += b[i] * b[i]
    }
    return dot / ((Math.sqrt(normA.toDouble()) * Math.sqrt(normB.toDouble())).toFloat())
}
