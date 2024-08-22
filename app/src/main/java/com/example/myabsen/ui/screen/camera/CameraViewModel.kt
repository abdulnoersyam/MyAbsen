package com.example.myabsen.ui.screen.camera

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myabsen.data.UserRepository
import com.example.myabsen.data.local.pref.AbsenModel
import com.example.myabsen.data.local.pref.UserModel
import com.example.myabsen.data.remote.response.InsertAbsenGuruDanKaryawanResponse
import com.example.myabsen.data.remote.response.UpdateAbsenGuruDanKaryawanResponse
import com.example.myabsen.ui.common.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class CameraViewModel(private val repository: UserRepository) : ViewModel() {
    private val _addAbsenResult = MutableStateFlow<ResultState<AbsenModel>>(ResultState.Loading)
    val addAbsenResult: StateFlow<ResultState<AbsenModel>> = _addAbsenResult
    private val _updateAbsenResult = MutableStateFlow<ResultState<UpdateAbsenGuruDanKaryawanResponse>>(ResultState.Loading)
    val updateAbsenResult: StateFlow<ResultState<UpdateAbsenGuruDanKaryawanResponse>> = _updateAbsenResult
    private val _userModel = MutableStateFlow<UserModel?>(null)
    val userModel: StateFlow<UserModel?> get() = _userModel

    private val _absen = MutableStateFlow<String?>(null)
    val absen: StateFlow<String?> get() = _absen

    fun getAbsenSession() {
        viewModelScope.launch {
            repository.getAbsenSession()?.let {
                _absen.value = it
            }
        }
    }

    fun addAbsen(
        absenMasuk: String,
        absenKeluar: String,
        tanggal: String,
        status: String,
        nip: String,
        fotoMasuk: MultipartBody.Part,
        fotoKeluar: MultipartBody.Part
    ) {
        viewModelScope.launch {
            _addAbsenResult.value = ResultState.Loading
            try {
                val response = repository.insertAbsen(
                    absenMasuk, absenKeluar, tanggal, status, nip, fotoMasuk, fotoKeluar
                )
                _addAbsenResult.value = response
            } catch (e: Exception) {
                _addAbsenResult.value = ResultState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateAbsen(
        idAbsen: String,
        absenKeluar: String,
        foto_keluar: MultipartBody.Part
    ) {
        viewModelScope.launch {
            try {
                val result = repository.updateAbsen(idAbsen, absenKeluar, foto_keluar)
                _updateAbsenResult.value = result
                Log.d("CameraViewModel", "updateAbsen: Success")
            } catch (e: Exception) {
                _updateAbsenResult.value = ResultState.Error(e.message ?: "An error occurred")
                Log.e("CameraViewModel", "updateAbsen: Error", e)
            }
        }
    }

    fun saveSessionAbsen(absen: String) {
        viewModelScope.launch {
            repository.saveSessionAbsen(absen)
        }
    }
}