package com.example.absensi.data.local.session

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("absensi_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGIN = "is_login"
        private const val KEY_NISN = "nisn"
        private const val KEY_NAMA = "nama"
        private const val KEY_JURUSAN = "jurusan"
        private const val KEY_TAHUN = "tahun"
        private const val KEY_API_KEY = "api_key"
    }

    fun saveLogin(
        nisn: String,
        nama: String,
        jurusan: String,
        tahun: Int,
        apiKey: String
    ) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGIN, true)
            putString(KEY_NISN, nisn)
            putString(KEY_NAMA, nama)
            putString(KEY_JURUSAN, jurusan)
            putInt(KEY_TAHUN, tahun)
            putString(KEY_API_KEY, apiKey)
            apply()
        }
    }

    fun isLogin(): Boolean =
        prefs.getBoolean(KEY_IS_LOGIN, false)

    fun getApiKey(): String? =
        prefs.getString(KEY_API_KEY, null)

    fun logout() {
        prefs.edit().clear().apply()
    }
}