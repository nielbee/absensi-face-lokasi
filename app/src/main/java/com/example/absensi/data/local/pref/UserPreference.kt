package com.example.absensi.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.absensi.remote.VerifikasiResponse

class UserPreference(context: Context) {

    private val prefs =
        context.getSharedPreferences("user_pref", Context.MODE_PRIVATE)
    companion object{
        private const val id_guru = "id_guru"
        private const val nama = "nama"
      

        private const val api_key = "api_key"
    }
    fun saveVerifikasi(data: VerifikasiResponse) {
        prefs.edit()
            .putString("id_guru", data.id_guru)
            .putString("nama", data.nama)
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

    fun getId():String =
        prefs.getString(id_guru, "")?:""

    fun getNama(): String =
        prefs.getString(nama, "" )?:""



    fun getApiKey(): String =
        prefs.getString(api_key, "")?:""

}
