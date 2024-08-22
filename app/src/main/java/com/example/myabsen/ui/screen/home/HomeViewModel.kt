package com.example.myabsen.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myabsen.data.UserRepository
import com.example.myabsen.data.remote.response.GetAbsenGuruDanKaryawanResponse
import com.example.myabsen.data.remote.response.GetAbsenGuruDanKaryawanResponseItem
import com.example.myabsen.data.remote.response.GetProfileGuruDanKaryawanResponse
import com.example.myabsen.data.remote.response.GetWaktuAbsenResponse

import com.example.myabsen.ui.common.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(private val repository: UserRepository) : ViewModel() {
    private val _absenResponse = MutableStateFlow<ResultState<List<GetAbsenGuruDanKaryawanResponseItem>>>(ResultState.Loading)
    val absenResponse: StateFlow<ResultState<List<GetAbsenGuruDanKaryawanResponseItem>>> = _absenResponse

    fun getAbsenGuruDanKaryawan(nip: Int, year: Int, month: Int) {
        viewModelScope.launch {
            _absenResponse.value = ResultState.Loading
            val result = withContext(Dispatchers.IO) {
                repository.getAbsenGuruDanKaryawan(nip, year, month)
            }
            _absenResponse.value = result
        }
    }

    private val _getWaktuAbsenState = MutableStateFlow<ResultState<GetWaktuAbsenResponse>>(ResultState.Loading)
    val getWaktuAbsenState: StateFlow<ResultState<GetWaktuAbsenResponse>> = _getWaktuAbsenState

    fun getWaktuAbsen(idAbsen: Int) {
        viewModelScope.launch {
            _getWaktuAbsenState.value = ResultState.Loading
            val result = withContext(Dispatchers.IO) {
                repository.getWaktuAbsen(idAbsen)
            }
            _getWaktuAbsenState.value = result
        }
    }

    fun clearSession() {
        viewModelScope.launch {
            repository.clearSession()
            _getWaktuAbsenState.value = ResultState.Loading
        }
    }
}