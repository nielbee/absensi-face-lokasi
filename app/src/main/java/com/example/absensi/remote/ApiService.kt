package com.example.absensi.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

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
    ): Response<AbsensiResponse>

    @POST("pulang/{id_guru}")
    suspend fun absenPulang(
        @Path("id_guru") id_guru: String,
        @Body body: Map<String, String>
    ): Response<AbsensiResponse>

    @GET("detail/{id_guru}")
    suspend fun detail(
        @Path("id_guru") id_guru: String
    ): Response<List<DetailResponse>>

    @POST("ijin/{id_guru}")
    suspend fun ijin(
        @Path("id_guru") id_guru: String,
        @Body body: Map<String, String>
    ): Response<AbsensiResponse>
}
