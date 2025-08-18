package com.example.risaleezanvakti.data.model

import com.google.gson.annotations.SerializedName

data class PrayerTimes(
    @SerializedName("Fajr") val fajr: String,
    @SerializedName("Sunrise") val sunrise: String,
    @SerializedName("Dhuhr") val dhuhr: String,
    @SerializedName("Asr") val asr: String,
    @SerializedName("Maghrib") val maghrib: String,
    @SerializedName("Isha") val isha: String,
    @SerializedName("date") val date: String
)