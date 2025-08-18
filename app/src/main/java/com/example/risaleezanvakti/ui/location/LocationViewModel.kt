package com.example.risaleezanvakti.ui.location

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.risaleezanvakti.data.model.Country
import com.example.risaleezanvakti.data.model.Place
import com.example.risaleezanvakti.data.repository.LocationRepository
import kotlinx.coroutines.launch
import retrofit2.Response

class LocationViewModel(private val repository: LocationRepository) : ViewModel() {

    private val _countries = MutableLiveData<List<Country>>()
    val countries: LiveData<List<Country>> = _countries

    private val _regions = MutableLiveData<List<String>>()
    val regions: LiveData<List<String>> = _regions

    private val _cities = MutableLiveData<List<String>>()
    val cities: LiveData<List<String>> = _cities

    private val _nearbyPlaces = MutableLiveData<List<Place>>()
    val nearbyPlaces: LiveData<List<Place>> = _nearbyPlaces

    fun fetchCountries() {
        viewModelScope.launch {
            try {
                val response: Response<List<Country>> = repository.getCountries()
                if (response.isSuccessful) {
                    _countries.postValue(response.body() ?: emptyList())
                } else {
                    Log.e("API_Response", "Ülkeler çekilirken hata oluştu: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API_Response", "Ülkeler çekilirken hata oluştu: ${e.message}")
            }
        }
    }

    fun fetchRegions(country: String) {
        viewModelScope.launch {
            try {
                val response: Response<List<String>> = repository.getRegions(country)
                if (response.isSuccessful) {
                    _regions.postValue(response.body() ?: emptyList())
                } else {
                    Log.e("API_Response", "Bölgeler çekilirken hata oluştu: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API_Response", "Bölgeler çekilirken hata oluştu: ${e.message}")
            }
        }
    }

    fun fetchCities(country: String, region: String) {
        viewModelScope.launch {
            try {
                val response: Response<List<String>> = repository.getCities(country, region)
                if (response.isSuccessful) {
                    _cities.postValue(response.body() ?: emptyList())
                } else {
                    Log.e("API_Response", "İlçeler çekilirken hata oluştu: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API_Response", "İlçeler çekilirken hata oluştu: ${e.message}")
            }
        }
    }

    fun fetchNearbyPlaces(lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                val response: Response<List<Place>> = repository.getNearbyPlaces(lat, lng)
                if (response.isSuccessful) {
                    _nearbyPlaces.postValue(response.body() ?: emptyList())
                } else {
                    Log.e("API_Response", "Yakın yerler çağrısı başarısız: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API_Response", "Yakın yerler çekilirken hata oluştu: ${e.message}")
            }
        }
    }
}
