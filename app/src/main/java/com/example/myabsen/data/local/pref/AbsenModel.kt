package com.example.myabsen.data.local.pref

data class AbsenModel(
    val idAbsen: String,
    val message: String,
    val tanggal: String,
    val absenMasuk: String,
    val absenKeluar: String,
    val fotoMasuk: String,
    val fotoKeluar: String,
    val nip: String,
    val status: String
)