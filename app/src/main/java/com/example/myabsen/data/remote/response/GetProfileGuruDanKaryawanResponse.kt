package com.example.myabsen.data.remote.response

import com.google.gson.annotations.SerializedName

data class GetProfileGuruDanKaryawanResponse(

	@field:SerializedName("profile")
	val profile: Profile? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class Profile(

	@field:SerializedName("password")
	val password: String? = null,

	@field:SerializedName("nip")
	val nip: String? = null,

	@field:SerializedName("fullname")
	val fullname: String? = null,

	@field:SerializedName("position")
	val position: String? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("token")
	val token: String? = null
)
