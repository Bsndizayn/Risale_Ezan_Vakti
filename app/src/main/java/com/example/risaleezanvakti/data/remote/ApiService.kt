package com.example.risaleezanvakti.data.remote

import com.example.risaleezanvakti.data.model.Country
import com.example.risaleezanvakti.data.model.Place
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("api/countries")
    suspend fun getCountries(): Response<List<Country>>

    @GET("api/regions")
    suspend fun getRegions(@Query("country") country: String): Response<List<String>>

    @GET("api/cities")
    suspend fun getCities(
        @Query("country") country: String,
        @Query("region") region: String
    ): Response<List<String>>

    @GET("api/nearByPlaces")
    suspend fun getNearbyPlaces(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("lang") lang: String = "tr"
    ): Response<List<Place>>
}