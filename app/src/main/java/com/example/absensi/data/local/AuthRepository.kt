//package com.example.absensi.data.local
//
//import com.example.absensi.remote.ApiService
//import com.example.absensi.remote.LoginResponse
//
//class AuthRepository(
//    private val api: ApiService
//) {
//
//    suspend fun login(nisn: String): LoginResponse {
//        val response = api.login(
//            nisn,
//            mapOf("app_key" to "asdjsandkjasvfamd")
//        )
//
//        if (response.isSuccessful) {
//            return response.body()!!
//        } else {
//            throw Exception("Login gagal")
//        }
//    }
//}
