package com.example.absensi.remote

data class DetailResponse(
    val hari: String?,
    val tanggal: String?,
    val id_guru: String?,
    val tanggal_kehadiran: String?,
    val jam_masuk: String?,
    val jam_keluar: String?,
    val keterangan: String?
)
