package com.example.myabsen.data.local.pref

data class UserModel(
    val nip: Int,
    val token: String,
    val fullname: String,
    val position: String,
    val email: String,
    val isLogin: Boolean = false,
    val message: String
)
