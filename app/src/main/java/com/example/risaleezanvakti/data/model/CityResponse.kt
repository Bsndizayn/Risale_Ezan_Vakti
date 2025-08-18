package com.example.risaleezanvakti.data.model

import com.google.gson.annotations.SerializedName

data class CityResponse(
    @SerializedName("data")
    val cities: List<String>
)