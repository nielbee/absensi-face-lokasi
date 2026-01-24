package com.example.absensi.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.absensi.remote.VerifikasiResponse

class UserPreference(context: Context) {

    private val prefs =
        context.getSharedPreferences("user_pref", Context.MODE_PRIVATE)
    companion object{
        private const val nisn = "nisn"
        private const val nama = "nama"
        private const val id_jurusan = "id_jurusan"
        private const val tahun_masuk = "tahun_masuk"

        private const val api_key = "api_key"
    }
    fun saveVerifikasi(data: VerifikasiResponse) {
        prefs.edit()
            .putString("nisn", data.nisn)
            .putString("nama", data.nama)
            .putString("id_jurusan", data.id_jurusan)
            .putInt("tahun_masuk", data.tahun_masuk)
            .apply()
    }

    fun saveLogin(apiKey: String) {
        prefs.edit()
            .putString("api_key", apiKey)
            .putBoolean("is_login", true)
            .apply()
    }

    fun isLogin(): Boolean =
        prefs.getBoolean("is_login", false)

    fun getNisn():String =
        prefs.getString(nisn, "")?:""

    fun getNama(): String =
        prefs.getString(nama, "" )?:""

    fun getJurusan():String =
        prefs.getString(id_jurusan, "")?:""

    fun getApiKey(): String =
        prefs.getString(api_key, "")?:""

}
