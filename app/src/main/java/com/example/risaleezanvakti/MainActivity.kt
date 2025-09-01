package com.example.risaleezanvakti

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.risaleezanvakti.data.local.LocationPreferenceManager
import com.example.risaleezanvakti.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

        val locationPreferenceManager = LocationPreferenceManager(this)

        if (locationPreferenceManager.isSetupCompleted()) {
            // Daha önce setup tamamlandı, direkt PrayerTimesFragment
            navGraph.setStartDestination(R.id.prayerTimesFragment)
        } else if (locationPreferenceManager.hasSavedLocation()) {
            // Konum kaydedilmiş ama setup tamamlanmamışsa, izin ekranına yönlendir
            navGraph.setStartDestination(R.id.notificationSettingsFragment)
        } else {
            // Hiçbir kurulum yapılmamışsa, konum seçim ekranından başla
            navGraph.setStartDestination(R.id.locationSelectionFragment)
        }

        // Ayarlanan yeni grafiği NavController'a ver
        navController.graph = navGraph
    }
}