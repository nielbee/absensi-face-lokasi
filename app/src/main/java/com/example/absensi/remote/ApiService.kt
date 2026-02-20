package com.example.absensi.remote
//import com.google.android.gms.common.api.Response
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

// import ret
interface ApiService {

    @POST("verifikasi/{id_guru}")
    suspend fun verifikasi(
        @Path("id_guru") id_guru: String,
        @Body body: Map<String, String>
    ): VerifikasiResponse

    @POST("login/{id_guru}")
    suspend fun login(
        @Path("id_guru") id_guru: String,
        @Body body: Map<String, String>
    ): LoginResponse

    @POST("datang/{id_guru}")
    suspend fun absenDatang(
        @Path("id_guru") id_guru: String,
        @Body body: Map<String, String>
    ):Response<AbsensiResponse>

    @POST("pulang/{id_guru}")
    suspend fun absenPulang(
        @Path("id_guru") id_guru: String,
        @Body body: Map<String, String>
    ): Response<AbsensiResponse>
}
