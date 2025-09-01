package com.example.risaleezanvakti.ui.settings

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.risaleezanvakti.R
import com.example.risaleezanvakti.data.local.LocationPreferenceManager
import com.example.risaleezanvakti.databinding.FragmentNotificationSettingsBinding
import com.google.android.material.button.MaterialButton
import android.content.ComponentName

class NotificationSettingsFragment : Fragment() {

    private var _binding: FragmentNotificationSettingsBinding? = null
    private val binding get() = _binding!!

    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            val message = if (isGranted) "Konum izni verildi." else "Konum izni verilmedi."
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            updateButtonColors()
        }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            val message = if (isGranted) "Bildirim izni verildi." else "Bildirim izni verilmedi."
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            updateButtonColors()
        }

    override fun onResume() {
        super.onResume()
        // Ayarlar ekranından geri dönüldüğünde buton renklerini güncelle
        updateButtonColors()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        updateButtonColors() // Fragment ilk açıldığında renkleri ayarla
    }

    private fun setupListeners() {
        val prefs = LocationPreferenceManager(requireContext())

        binding.buttonLocationPermission.setOnClickListener {
            if (hasLocationPermission()) {
                Toast.makeText(requireContext(), "Konum izni zaten verilmiş.", Toast.LENGTH_SHORT).show()
            } else {
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        binding.buttonNotificationPermission.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (hasNotificationPermission()) {
                    Toast.makeText(requireContext(), "Bildirim izni zaten verilmiş.", Toast.LENGTH_SHORT).show()
                } else {
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                Toast.makeText(requireContext(), "Bu Android sürümünde bildirim izni gerekmez.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonExactAlarm.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !canScheduleExactAlarms()) {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            } else {
                Toast.makeText(requireContext(), "Alarm izni zaten verilmiş veya gerekmiyor.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonBatteryOptimization.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isIgnoringBatteryOptimizations()) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${requireContext().packageName}")
                }
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Pil optimizasyonu zaten devre dışı.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonAutostart.setOnClickListener {
            // Cihaz üreticisine özel otomatik başlatma ayarları ekranını açar
            val intent = Intent()
            val manufacturer = Build.MANUFACTURER.lowercase()
            val component = when {
                manufacturer.contains("xiaomi") -> ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
                manufacturer.contains("oppo") -> ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")
                manufacturer.contains("vivo") -> ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
                manufacturer.contains("huawei") -> ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")
                else -> null
            }
            if (component != null) {
                intent.component = component
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Otomatik başlatma ayarı bulunamadı.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(requireContext(), "Cihazınız için özel bir otomatik başlatma ayarı bulunamadı.", Toast.LENGTH_LONG).show()
            }
        }

        binding.buttonSave.setOnClickListener {
            prefs.setSetupCompleted()
            findNavController().navigate(
                R.id.action_notificationSettingsFragment_to_prayerTimesFragment,
                null,
                NavOptions.Builder().setPopUpTo(R.id.locationSelectionFragment, true).build()
            )
        }

        binding.buttonSkip.setOnClickListener {
            prefs.setSetupCompleted()
            findNavController().navigate(
                R.id.action_notificationSettingsFragment_to_prayerTimesFragment,
                null,
                NavOptions.Builder().setPopUpTo(R.id.locationSelectionFragment, true).build()
            )
        }
    }

    // --- İzin Kontrol Fonksiyonları ---

    private fun hasLocationPermission() = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    private fun hasNotificationPermission() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else true
    private fun canScheduleExactAlarms() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.canScheduleExactAlarms()
    } else true
    private fun isIgnoringBatteryOptimizations() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val pm = requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
        pm.isIgnoringBatteryOptimizations(requireContext().packageName)
    } else true

    // --- Görsel Güncelleme Fonksiyonu ---

    private fun updateButtonColors() {
        // Renkleri tanımla
        val greenColor = ContextCompat.getColor(requireContext(), R.color.green_approved)
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.custom_button_background)

        // İzin durumuna göre butonun arkaplan rengini ayarla
        fun setButtonState(button: MaterialButton, isGranted: Boolean) {
            button.setBackgroundColor(if (isGranted) greenColor else defaultColor)
        }

        setButtonState(binding.buttonLocationPermission, hasLocationPermission())
        setButtonState(binding.buttonNotificationPermission, hasNotificationPermission())
        setButtonState(binding.buttonExactAlarm, canScheduleExactAlarms())
        setButtonState(binding.buttonBatteryOptimization, isIgnoringBatteryOptimizations())
        // Otomatik başlatma izninin durumu programatik olarak güvenilir bir şekilde kontrol edilemediği için rengi değiştirilmemiştir.
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}