package com.example.risaleezanvakti.data.local

import android.content.Context
import android.content.SharedPreferences

class LocationPreferenceManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("location_prefs", Context.MODE_PRIVATE)

    fun saveLocation(country: String, region: String, city: String) {
        with(sharedPreferences.edit()) {
            putString("country", country)
            putString("region", region)
            putString("city", city)
            apply()
        }
    }

    fun getLocation(): Triple<String?, String?, String?> {
        val country = sharedPreferences.getString("country", null)
        val region = sharedPreferences.getString("region", null)
        val city = sharedPreferences.getString("city", null)
        return Triple(country, region, city)
    }

    fun hasSavedLocation(): Boolean {
        val (country, region, city) = getLocation()
        return country != null && region != null && city != null
    }
}