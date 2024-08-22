package com.example.myabsen.data

import android.util.Log
import com.example.myabsen.data.local.pref.AbsenModel
import com.example.myabsen.data.local.pref.AbsenPreference
import com.example.myabsen.data.local.pref.UserModel
import com.example.myabsen.data.local.pref.UserPreference
import com.example.myabsen.data.remote.response.GetAbsenGuruDanKaryawanResponse
import com.example.myabsen.data.remote.response.GetAbsenGuruDanKaryawanResponseItem
import com.example.myabsen.data.remote.response.GetProfileGuruDanKaryawanResponse
import com.example.myabsen.data.remote.response.GetWaktuAbsenResponse
import com.example.myabsen.data.remote.response.InsertAbsenGuruDanKaryawanResponse
import com.example.myabsen.data.remote.response.LoginGuruDanKaryawanResponse
import com.example.myabsen.data.remote.response.RegisterGuruDanKaryawanResponse
import com.example.myabsen.data.remote.response.UpdateAbsenGuruDanKaryawanResponse
import com.example.myabsen.data.remote.retrofit.ApiService
import com.example.myabsen.ui.common.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

class UserRepository(
    private val userPreference: UserPreference,
    private val absenPreference: AbsenPreference,
    private val apiService: ApiService,
) {
    private val TAG: String = "UserRepository"

    suspend fun saveSessionGuruDanKaryawan(user: UserModel) {
        userPreference.saveSession(user)
    }

    suspend fun saveSessionAbsen(absen: String) {
        absenPreference.saveAbsenSession(absen)
    }

    suspend fun logout() {
        userPreference.logout()
    }

    suspend fun getUserSessionGuruDanKaryawan(): UserModel? {
        return userPreference.getSession().firstOrNull()
    }

    suspend fun getAbsenSession(): String? {
        return absenPreference.getAbsenSession().firstOrNull()
    }

    suspend fun clearSession() {
        absenPreference.clearSession()
    }

    suspend fun getProfileGuruDanKaryawan(
        nip: Int,
    ): ResultState<GetProfileGuruDanKaryawanResponse> {
        return try {
            val response = apiService.getProfileGuruDanKaryawan(nip)
            Log.d(TAG, "Respons API editMenu: $response")

            if (response.isSuccessful) {
                ResultState.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Error dalam panggilan API: $errorBody")
                ResultState.Error("Error: $errorBody")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}")
            ResultState.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun getWaktuAbsen(
        idAbsen: Int,
    ): ResultState<GetWaktuAbsenResponse> {
        return try {
            val response = apiService.getWaktuAbsen(idAbsen)
            Log.d(TAG, "Respons API waktuAbsen: $response")

            if (response.isSuccessful) {
                ResultState.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Error dalam panggilan API: $errorBody")
                ResultState.Error("Error: $errorBody")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}")
            ResultState.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun insertAbsen(
        absenMasuk: String,
        absenKeluar: String,
        tanggal: String,
        status: String,
        nip: String,
        foto_masuk: MultipartBody.Part,
        foto_keluar: MultipartBody.Part
    ): ResultState<AbsenModel> {
        return try {
            val absenMasukBody = absenMasuk.toRequestBody("text/plain".toMediaType())
            val absenKeluarBody = absenKeluar.toRequestBody("text/plain".toMediaType())
            val tanggalBody = tanggal.toRequestBody("text/plain".toMediaType())
            val statusBody = status.toRequestBody("text/plain".toMediaType())
            val nipBody = nip.toRequestBody("text/plain".toMediaType())

            val successResponse = apiService.insertAbsenGuruDanKaryawan(
                absenMasukBody,
                absenKeluarBody,
                tanggalBody,
                statusBody,
                nipBody,
                foto_masuk,
                foto_keluar
            )
            Log.d(TAG, "Add Menu API response: $successResponse")
            val menuModel = convertToModelAddAbsen(successResponse)
            ResultState.Success(menuModel)
        } catch (e: HttpException) {
            val errorMessage = e.response()?.errorBody()?.string() ?: "An error occurred"
            Log.e(TAG, "HttpException: $errorMessage")
            ResultState.Error(errorMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}")
            ResultState.Error(e.message ?: "An error occurred")
        }
    }

    private fun convertToModelAddAbsen(response: InsertAbsenGuruDanKaryawanResponse): AbsenModel {
        val absen = response.absen
            ?: throw IllegalStateException(response.message)

        return AbsenModel(
            idAbsen = absen.idAbsen ?: "",
            message = response.message ?: "",
            tanggal = absen.tanggal ?: "",
            absenMasuk = absen.absenMasuk ?: "",
            absenKeluar = absen.absenKeluar ?: "",
            fotoMasuk = absen.fotoMasuk ?: "",
            fotoKeluar = absen.fotoKeluar ?: "",
            nip = absen.nip ?: "",
            status = absen.status ?: "",
        )
    }

    suspend fun updateAbsen(
        idAbsen: String,
        absenKeluar: String,
        foto_keluar: MultipartBody.Part
    ): ResultState<UpdateAbsenGuruDanKaryawanResponse> {
        return try {
            val absenKeluarBody = absenKeluar.toRequestBody("text/plain".toMediaType())
            val idAbsen = idAbsen.toRequestBody("text/plain".toMediaType())
            val successResponse = apiService.updateAbsenGuruDanKaryawan(
                idAbsen,
                absenKeluarBody,
                foto_keluar
            )
            Log.d(TAG, "Add Menu API response: $successResponse")
            ResultState.Success(successResponse)
        } catch (e: HttpException) {
            val errorMessage = e.response()?.errorBody()?.string() ?: "An error occurred"
            Log.e(TAG, "HttpException: $errorMessage")
            ResultState.Error(errorMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}")
            ResultState.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun getAbsenGuruDanKaryawan(
        nip: Int,
        year: Int,
        month: Int
    ): ResultState<List<GetAbsenGuruDanKaryawanResponseItem>> {
        return try {
            val response = apiService.getAbsenGuruDanKaryawan(nip, year, month)
            Log.d(TAG, "Respons API editMenu: $response")

            if (response.isSuccessful) {
                ResultState.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Error dalam panggilan API: $errorBody")
                ResultState.Error("Error: $errorBody")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}")
            ResultState.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun loginGuruDanKaryawan(email: String, password: String): ResultState<UserModel> {
        return try {
            val successResponse = apiService.loginGuruDanKaryawan(email, password)
            Log.d(TAG, "Login API response: $successResponse")

            // Log the raw response for debugging purposes
            Log.d(TAG, "Raw response: ${successResponse}")

            val userModel = convertToUserModelGuruDanKaryawan(successResponse)
            ResultState.Success(userModel)
        } catch (e: HttpException) {
            Log.e(TAG, "Http error: ${e.code()}")
            val errorBody = e.response()?.errorBody()?.string()
            Log.e(TAG, "Error Body: $errorBody")

            val errorMessage = when (e.code()) {
                400 -> "Enter correct password!"
                401 -> "User is not registered, Sign Up first"
                else -> "An error occurred"
            }

            ResultState.Error(errorMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}")
            ResultState.Error(e.message ?: "An error occurred")
        }
    }

    private fun convertToUserModelGuruDanKaryawan(response: LoginGuruDanKaryawanResponse): UserModel {
        val user = response.payload
            ?: throw IllegalStateException(response.message)

        return UserModel(
            nip = (user.nip ?: 0) as Int,
            message = response.message ?: "",
            fullname = user.fullname ?: "",
            position = user.position ?: "",
            email = user.email ?: "",
            token = response.token ?: "",
            isLogin = true
        )
    }

    suspend fun registerGuruDanKaryawan(
        nip: String,
        fullname: String,
        email: String,
        password: String,
        position: String
    ): ResultState<UserModel> {
        return try {
            val successResponse =
                apiService.registerGuruDanKaryawan(nip, fullname, email, password, position)
            Log.d(TAG, "Login API response: $successResponse")
            val registerModel = convertToRegisterGuruDanKaryawan(successResponse)
            ResultState.Success(registerModel)
        } catch (e: HttpException) {
            Log.e(TAG, "Http error: ${e.code()}")
            val errorBody = e.response()?.errorBody()?.string()
            Log.e(TAG, "Error Body: $errorBody")

            val errorMessage = when (e.code()) {
                400 -> "Email exists. No need to register again"
                else -> "An error occurred"
            }

            ResultState.Error(errorMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}")
            ResultState.Error(e.message ?: "An error occurred")
        }
    }

    private fun convertToRegisterGuruDanKaryawan(response: RegisterGuruDanKaryawanResponse): UserModel {

        return UserModel(
            nip = 0,
            message = response.message ?: "",
            fullname = "",
            email = "",
            isLogin = false,
            position = "",
            token = ""
        )
    }



    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            userPreference: UserPreference,
            absenPreference: AbsenPreference,
            apiService: ApiService
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(userPreference, absenPreference, apiService)
            }.also { instance = it }
    }
}
