package com.example.absensi.remote

data class VerifikasiResponse(
    val nisn: String,
    val nama: String,
    val id_jurusan: String,
    val tahun_masuk: Int
)
