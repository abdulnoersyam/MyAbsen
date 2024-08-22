package com.example.myabsen.data.remote.retrofit

import com.example.myabsen.data.remote.response.GetAbsenGuruDanKaryawanResponse
import com.example.myabsen.data.remote.response.GetAbsenGuruDanKaryawanResponseItem
import com.example.myabsen.data.remote.response.GetProfileGuruDanKaryawanResponse
import com.example.myabsen.data.remote.response.GetWaktuAbsenResponse
import com.example.myabsen.data.remote.response.InsertAbsenGuruDanKaryawanResponse
import com.example.myabsen.data.remote.response.LoginGuruDanKaryawanResponse
import com.example.myabsen.data.remote.response.RegisterGuruDanKaryawanResponse
import com.example.myabsen.data.remote.response.UpdateAbsenGuruDanKaryawanResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @FormUrlEncoded
    @POST("login_guru_dan_karyawan.php")
    suspend fun loginGuruDanKaryawan(
        @Field("email") email: String,
        @Field("password") password: String,
    ): LoginGuruDanKaryawanResponse

    @FormUrlEncoded
    @POST("register_guru_dan_karyawan.php")
    suspend fun registerGuruDanKaryawan(
        @Field("nip") nip: String,
        @Field("fullname") fullname: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("position") position: String,
    ): RegisterGuruDanKaryawanResponse

    @FormUrlEncoded
    @POST("get_profile_guru_dan_karyawan.php")
    suspend fun getProfileGuruDanKaryawan(
        @Field("nip") nip: Int,
    ): Response<GetProfileGuruDanKaryawanResponse>

    @FormUrlEncoded
    @POST("get_waktu_absen.php")
    suspend fun getWaktuAbsen(
        @Field("id_absen") idAbsen: Int,
    ): Response<GetWaktuAbsenResponse>

    @FormUrlEncoded
    @POST("get_absen_guru_dan_karyawan.php")
    suspend fun getAbsenGuruDanKaryawan(
        @Field("nip") nip: Int,
        @Field("year") year: Int,
        @Field("month") month: Int,
    ): Response<List<GetAbsenGuruDanKaryawanResponseItem>>

    @Multipart
    @POST("insert_absen_guru_dan_karyawan.php")
    suspend fun insertAbsenGuruDanKaryawan(
        @Part("absen_masuk") absenMasuk: RequestBody,
        @Part("absen_keluar") absenKeluar: RequestBody,
        @Part("tanggal") tanggal: RequestBody,
        @Part("status") status: RequestBody,
        @Part("nip") nip: RequestBody,
        @Part foto_masuk: MultipartBody.Part,
        @Part foto_keluar: MultipartBody.Part
    ): InsertAbsenGuruDanKaryawanResponse

    @Multipart
    @POST("update_absen_guru_dan_karyawan.php")
    suspend fun updateAbsenGuruDanKaryawan(
        @Part("id_absen") idAbsen: RequestBody,
        @Part("absen_keluar") absenKeluar: RequestBody,
        @Part foto_keluar: MultipartBody.Part
    ): UpdateAbsenGuruDanKaryawanResponse
}