package com.example.myabsen.data.remote.response

import com.google.gson.annotations.SerializedName

data class LoginGuruDanKaryawanResponse(
	@field:SerializedName("payload")
	val payload: Payload? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("token")
	val token: String? = null
)

data class Payload(
	@field:SerializedName("nip")
	val nip: Int? = null,

	@field:SerializedName("fullname")
	val fullname: String? = null,

	@field:SerializedName("position")
	val position: String? = null,

	@field:SerializedName("email")
	val email: String? = null
)
