package com.example.myabsen.ui.screen.register

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myabsen.data.UserRepository
import com.example.myabsen.data.local.pref.UserModel
import com.example.myabsen.ui.common.ResultState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(private val repository: UserRepository) : ViewModel() {

    private val _registerResult = MutableStateFlow<ResultState<UserModel>>(ResultState.Loading)
    val registerResult: StateFlow<ResultState<UserModel>> = _registerResult

    private val _nip = mutableStateOf("")
    val nip: State<String> = _nip

    private val _fullname = mutableStateOf("")
    val fullname: State<String> = _fullname

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _position = mutableStateOf("")
    val position: State<String> = _position

    fun setNip(nip: String) {
        _nip.value = nip
    }

    fun setFullName(fullname: String) {
        _fullname.value = fullname
    }

    fun setEmail(email: String) {
        _email.value = email
    }

    fun setPassword(password: String) {
        _password.value = password
    }

    fun setPosition(position: String) {
        _position.value = position
    }

    fun registerGuruDanKaryawan(
        nip: String,
        fullname: String,
        email: String,
        password: String,
        position: String
    ) {
        viewModelScope.launch {
            try {
                val result = repository.registerGuruDanKaryawan(nip, fullname, email, password, position)
                _registerResult.value = result
            } catch (e: Exception) {
                _registerResult.value = ResultState.Error(e.message ?: "An error occurred")
            }
        }
    }
}
