package com.example.absensi.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.absensi.data.local.UserPreference
import com.example.absensi.remote.ApiService
import com.example.absensi.remote.LoginResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val api: ApiService,
    private val pref: UserPreference
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    fun verifikasiNisn(nisn: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            try {
                val data = api.verifikasi(
                    nisn,
                    mapOf("app_key" to "asdjsandkjasvfamd")
                )

                _state.value = LoginState(
                    isLoading = false,
                    verifikasiData = data,
                    showPopup = true
                )

            } catch (e: Exception) {
                _state.value = LoginState(
                    isLoading = false,
                    error = "Verifikasi gagal, cek koneksi atau NISN"
                )
            }
        }
    }

    fun loginOtomatis(onDone: () -> Unit, onError: (String) -> Unit) {
        val data = _state.value.verifikasiData ?: return

        viewModelScope.launch {
            try {
                val res = api.login(
                    data.nisn,
                    mapOf("app_key" to "asdjsandkjasvfamd")
                )

                if (res.status == "berhasil") {
                    pref.saveVerifikasi(data)
                    pref.saveLogin(res.api_key)
                    onDone()
                } else {
                    onError("Login gagal")
                }

            } catch (e: Exception) {
                onError("Login error")
            }
        }
    }
    
    fun loginOtomatisLangsung(
        nisn: String,
        onDone: (LoginResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val verifikasiData = api.verifikasi(nisn, mapOf("app_key" to "asdjsandkjasvfamd"))
                val res = api.login(nisn, mapOf("app_key" to "asdjsandkjasvfamd"))

                if (res.status == "berhasil") {
                    pref.saveVerifikasi(verifikasiData)
                    pref.saveLogin(res.api_key)

                    onDone(res) // sekarang mengirim LoginResponse
                } else {
                    onError("Login gagal")
                }
            } catch (e: Exception) {
                onError("Login error")
            }
        }
    }


    fun popupConfirmed() {
        _state.value = _state.value.copy(showPopup = false)
    }
}
