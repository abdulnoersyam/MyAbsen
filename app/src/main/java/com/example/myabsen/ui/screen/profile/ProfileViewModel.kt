package com.example.myabsen.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myabsen.data.UserRepository
import com.example.myabsen.data.local.pref.UserModel
import com.example.myabsen.data.remote.response.GetProfileGuruDanKaryawanResponse
import com.example.myabsen.ui.common.ResultState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: UserRepository) : ViewModel() {

    private val _userModel = MutableStateFlow<UserModel?>(null)
    val userModel: StateFlow<UserModel?> get() = _userModel

    fun getUserSessionGuruDanKaryawan() {
        viewModelScope.launch {
            repository.getUserSessionGuruDanKaryawan()?.let {
                _userModel.value = it
            }
        }
    }

    private val _getProfileGuruDanKaryawanState = MutableStateFlow<ResultState<GetProfileGuruDanKaryawanResponse>>(
        ResultState.Loading)
    val getProfileGuruDanKaryawanState: StateFlow<ResultState<GetProfileGuruDanKaryawanResponse>> = _getProfileGuruDanKaryawanState

    fun getProfileGuruDanKaryawan(
        nip: Int,
    ) {
        viewModelScope.launch {
            _getProfileGuruDanKaryawanState.value = ResultState.Loading
            val result = repository.getProfileGuruDanKaryawan(nip)
            _getProfileGuruDanKaryawanState.value = result
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun clearUserModel() {
        _userModel.value = null
    }
}