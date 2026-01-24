package com.example.absensi.ui.login

import com.example.absensi.remote.VerifikasiResponse

//data class LoginUiState(
//    val isLoading: Boolean = false,
//    val errorMessage: String? = null,
//    val siswaData: Siswa? = null,
//    val showPopup: Boolean = false,
//    val isFaceRegistered: Boolean = false,
//    val isLoginSuccess: Boolean = false
//)
data class LoginState(
    val isLoading: Boolean = false,
    val verifikasiData: VerifikasiResponse? = null,
    val showPopup: Boolean = false,
    val error: String? = null
)
