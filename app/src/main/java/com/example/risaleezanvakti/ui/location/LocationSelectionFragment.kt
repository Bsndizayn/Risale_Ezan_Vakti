package com.example.risaleezanvakti.ui.location

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.risaleezanvakti.data.model.Place
import com.example.risaleezanvakti.data.remote.RetrofitClient
import com.example.risaleezanvakti.data.repository.LocationRepository
import com.example.risaleezanvakti.databinding.FragmentLocationSelectionBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.example.risaleezanvakti.R
import androidx.navigation.fragment.findNavController

class LocationSelectionFragment : Fragment() {

    private var _binding: FragmentLocationSelectionBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: LocationViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isProgrammaticSelection = false
    private var nearbyPlace: Place? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                findDeviceLocation()
            } else {
                Toast.makeText(requireContext(), "Konum izni olmadan otomatik konum bulunamaz.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLocationSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val repository = LocationRepository(RetrofitClient.apiService)
        val viewModelFactory = LocationViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[LocationViewModel::class.java]

        viewModel.fetchCountries()

        // Ülkeleri yükle ve Türkiye’yi öne al
        viewModel.countries.observe(viewLifecycleOwner) { countriesList ->
            val mutableList = countriesList.toMutableList()
            val turkeyIndex = mutableList.indexOfFirst {
                it.name.equals("Turkey", ignoreCase = true) || it.name.equals("Türkiye", ignoreCase = true)
            }
            if (turkeyIndex != -1) {
                val turkey = mutableList[turkeyIndex]
                mutableList.removeAt(turkeyIndex)
                mutableList.add(0, turkey)
            }

            val displayList = mutableList.map { country ->
                if (country.name.equals("Turkey", ignoreCase = true)) "Türkiye" else country.name
            }

            val adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_item, displayList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCountries.adapter = adapter

            nearbyPlace?.country?.let { country ->
                val pos = displayList.indexOfFirst { it.equals(country, ignoreCase = true) || it.equals("Türkiye", ignoreCase = true) }
                if (pos != -1) {
                    isProgrammaticSelection = true
                    binding.spinnerCountries.setSelection(pos)
                }
            }
        }

        viewModel.regions.observe(viewLifecycleOwner) { regionsList ->
            val adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_item, regionsList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerRegions.adapter = adapter
            nearbyPlace?.region?.let { region ->
                val pos = regionsList.indexOfFirst { it.equals(region, ignoreCase = true) }
                if (pos != -1) {
                    isProgrammaticSelection = true
                    binding.spinnerRegions.setSelection(pos)
                }
            }
        }

        viewModel.cities.observe(viewLifecycleOwner) { citiesList ->
            val adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_item, citiesList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCities.adapter = adapter
            nearbyPlace?.city?.let { city ->
                val pos = citiesList.indexOfFirst { it.equals(city, ignoreCase = true) }
                if (pos != -1) {
                    binding.spinnerCities.setSelection(pos)
                }
            }
        }

        viewModel.nearbyPlaces.observe(viewLifecycleOwner) { placesList ->
            if (placesList.isNotEmpty() && !placesList[0].country.isNullOrEmpty() && !placesList[0].region.isNullOrEmpty() && !placesList[0].city.isNullOrEmpty()) {
                val place = placesList[0]
                nearbyPlace = place
                val locationText = "${place.city}, ${place.region}, ${if (place.country.equals("Turkey", ignoreCase = true)) "Türkiye" else place.country}"
                binding.textViewDetectedLocation.text = "Konumunuz: $locationText "
                binding.spinnerCountries.visibility = View.GONE
                binding.spinnerRegions.visibility = View.GONE
                binding.spinnerCities.visibility = View.GONE
            } else {
                binding.textViewDetectedLocation.text = "Konumunuz bulunamadı. Lütfen manuel seçip kaydedin."
                binding.spinnerCountries.visibility = View.VISIBLE
                binding.spinnerRegions.visibility = View.VISIBLE
                binding.spinnerCities.visibility = View.VISIBLE
            }
        }

        binding.spinnerCountries.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!isProgrammaticSelection) {
                    var selected = parent?.getItemAtPosition(position)?.toString()
                    // Türkiye seçildiğinde API için "Turkey" gönder
                    if (selected.equals("Türkiye", ignoreCase = true)) selected = "Turkey"
                    if (!selected.isNullOrEmpty()) {
                        viewModel.fetchRegions(selected)
                    }
                }
                isProgrammaticSelection = false
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerRegions.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!isProgrammaticSelection) {
                    val country = binding.spinnerCountries.selectedItem?.toString()
                    var apiCountry = country
                    if (country.equals("Türkiye", ignoreCase = true)) apiCountry = "Turkey"
                    val region = parent?.getItemAtPosition(position)?.toString()
                    if (!apiCountry.isNullOrEmpty() && !region.isNullOrEmpty()) {
                        viewModel.fetchCities(apiCountry, region)
                    }
                }
                isProgrammaticSelection = false
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.buttonAutoDetect.setOnClickListener {
            checkLocationSettingsAndPermissions()
        }

        binding.buttonContinue.setOnClickListener {
            val country: String?
            val region: String?
            val city: String?

            if (nearbyPlace != null && !nearbyPlace?.country.isNullOrEmpty() && !nearbyPlace?.region.isNullOrEmpty() && !nearbyPlace?.city.isNullOrEmpty()) {
                country = nearbyPlace?.country
                region = nearbyPlace?.region
                city = nearbyPlace?.city
            } else {
                country = binding.spinnerCountries.selectedItem?.toString()
                region = binding.spinnerRegions.selectedItem?.toString()
                city = binding.spinnerCities.selectedItem?.toString()
            }

            if (!country.isNullOrEmpty() && !region.isNullOrEmpty() && !city.isNullOrEmpty()) {
                val saveCountry = if (country.equals("Türkiye", ignoreCase = true)) "Turkey" else country
                saveLocation(saveCountry, region, city)
                Toast.makeText(requireContext(), "Konum kaydedildi: $city", Toast.LENGTH_SHORT).show()
                // Konum kaydedildikten sonra bir sonraki ekrana geçiş yap
                findNavController().navigate(R.id.action_locationSelectionFragment_to_settingsFragment)
            } else {
                Toast.makeText(requireContext(), "Lütfen geçerli bir konum seçin veya otomatik konumu deneyin.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveLocation(country: String, region: String, city: String) {
        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("country", country)
            .putString("region", region)
            .putString("city", city)
            .apply()
    }

    private fun checkLocationSettingsAndPermissions() {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            Toast.makeText(requireContext(), "Lütfen konum servislerini açın.", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
            checkPermissionsAndFindLocation()
        }
    }

    private fun checkPermissionsAndFindLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            findDeviceLocation()
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    private fun findDeviceLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude
                val lng = location.longitude
                viewModel.fetchNearbyPlaces(lat, lng)
            } else {
                binding.textViewDetectedLocation.text = "Konum alınamadı. Lütfen manuel seçin."
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
