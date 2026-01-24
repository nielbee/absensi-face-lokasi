package com.example.absensi.remote
//import com.google.android.gms.common.api.Response
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

// import ret
interface ApiService {

    @POST("verifikasi/{nisn}")
    suspend fun verifikasi(
        @Path("nisn") nisn: String,
        @Body body: Map<String, String>
    ): VerifikasiResponse

    @POST("login/{nisn}")
    suspend fun login(
        @Path("nisn") nisn: String,
        @Body body: Map<String, String>
    ): LoginResponse

    @POST("datang/{nisn}")
    suspend fun absenDatang(
        @Path("nisn") nisn: String,
        @Body body: Map<String, String>
    ):Response<AbsensiResponse>

    @POST("pulang/{nisn}")
    suspend fun absenPulang(
        @Path("nisn") nisn: String,
        @Body body: Map<String, String>
    ): Response<AbsensiResponse>
}
