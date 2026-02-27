package com.example.absensi.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.absensi.data.local.UserPreference
import com.example.absensi.remote.ApiService
import com.example.absensi.remote.AbsensiResponse
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.Response

class NotificationsViewModel(
    private val api: ApiService,
    private val prefs: UserPreference
) : ViewModel() {

    private val _name = MutableLiveData<String>()
    private val _id = MutableLiveData<String>()
    private val _ijinResult = MutableLiveData<String>()

    val name: LiveData<String> = _name
    val id: LiveData<String> = _id
    val ijinResult: LiveData<String> = _ijinResult

    init {
        _name.value = prefs.getNama()
        _id.value = prefs.getId()
    }

    fun sendIjin(appKey: String, keterangan: String) {
        val idGuru = prefs.getId()
        val appToken = prefs.getApiKey()
        
        val body = mapOf(
            "app_key" to appKey,
            "token_key" to appToken,
            "keterangan" to keterangan
        )

        viewModelScope.launch {
            try {
                val response = api.ijin(idGuru, body)
                if (response.isSuccessful && response.body() != null) {
                    _ijinResult.value = Gson().toJson(response.body())
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    _ijinResult.value = "Error ${response.code()}: $errorBody"
                }
            } catch (e: Exception) {
                _ijinResult.value = "Exception: ${e.message}"
            }
        }
    }
}
