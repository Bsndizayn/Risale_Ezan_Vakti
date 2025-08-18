package com.example.risaleezanvakti.data.model

import com.google.gson.annotations.SerializedName

data class RegionResponse(
    @SerializedName("data")
    val regions: List<String>
)