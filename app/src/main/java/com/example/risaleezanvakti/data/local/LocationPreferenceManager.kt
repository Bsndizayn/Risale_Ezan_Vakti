package com.example.risaleezanvakti.data.local

import android.content.Context

class LocationPreferenceManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun hasSavedLocation(): Boolean {
        return prefs.contains("country") && prefs.contains("region") && prefs.contains("city")
    }

    fun saveLocation(country: String, region: String, city: String) {
        prefs.edit()
            .putString("country", country)
            .putString("region", region)
            .putString("city", city)
            .apply()
    }

    fun getCountry(): String? = prefs.getString("country", null)
    fun getRegion(): String? = prefs.getString("region", null)
    fun getCity(): String? = prefs.getString("city", null)

    fun isSetupCompleted(): Boolean = prefs.getBoolean("setup_completed", false)
    fun setSetupCompleted() {
        prefs.edit().putBoolean("setup_completed", true).apply()
    }
}
