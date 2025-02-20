package com.example.myabsen.data.remote.response

import com.google.gson.annotations.SerializedName

data class InsertAbsenGuruDanKaryawanResponse(

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("absen")
	val absen: Absen? = null
)

data class Absen(

	@field:SerializedName("absen_masuk")
	val absenMasuk: String? = null,

	@field:SerializedName("nip")
	val nip: String? = null,

	@field:SerializedName("id_absen")
	val idAbsen: String? = null,

	@field:SerializedName("tanggal")
	val tanggal: String? = null,

	@field:SerializedName("absen_keluar")
	val absenKeluar: String? = null,

	@field:SerializedName("foto_keluar")
	val fotoKeluar: String? = null,

	@field:SerializedName("foto_masuk")
	val fotoMasuk: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
