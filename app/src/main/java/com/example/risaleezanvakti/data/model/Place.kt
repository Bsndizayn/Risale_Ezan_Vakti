package com.example.risaleezanvakti.data.model

import com.google.gson.annotations.SerializedName

data class Place(
    @SerializedName("id")
    val id: Int,
    @SerializedName("country")
    val country: String?,
    @SerializedName("stateName")
    val region: String?,
    @SerializedName("name")
    val city: String?,
    @SerializedName("latitude")
    val lat: Double,
    @SerializedName("longitude")
    val lng: Double,
    @SerializedName("timezone")
    val timezone: String? = null,
    @SerializedName("countryCode")
    val countryCode: String? = null,
    @SerializedName("alternativeNames")
    val alternativeNames: List<String>? = null
)