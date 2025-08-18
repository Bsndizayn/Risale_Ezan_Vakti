package com.example.risaleezanvakti.ui.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.risaleezanvakti.data.repository.LocationRepository

class LocationViewModelFactory(private val repository: LocationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}