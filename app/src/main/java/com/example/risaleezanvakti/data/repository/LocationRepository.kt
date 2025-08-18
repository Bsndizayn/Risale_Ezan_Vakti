package com.example.risaleezanvakti.data.repository

import com.example.risaleezanvakti.data.model.Country
import com.example.risaleezanvakti.data.model.Place
import com.example.risaleezanvakti.data.remote.ApiService
import retrofit2.Response

class LocationRepository(private val apiService: ApiService) {

    suspend fun getCountries(): Response<List<Country>> {
        return apiService.getCountries()
    }

    suspend fun getRegions(country: String): Response<List<String>> {
        return apiService.getRegions(country)
    }

    suspend fun getCities(country: String, region: String): Response<List<String>> {
        return apiService.getCities(country, region)
    }

    suspend fun getNearbyPlaces(lat: Double, lng: Double): Response<List<Place>> {
        return apiService.getNearbyPlaces(lat, lng)
    }
}