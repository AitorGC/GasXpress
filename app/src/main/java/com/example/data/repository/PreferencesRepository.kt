package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.FuelType
import com.example.data.model.SpanishProvinces
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    AMOLED
}

data class UserSettings(
    val selectedProvinceId: String = "28", // Madrid by default
    val selectedProvinceName: String = "Madrid",
    val selectedIslandId: String? = null,
    val selectedIslandName: String? = null,
    val selectedFuelType: FuelType = FuelType.GASOLINA_95,
    val isFirstTimeLaunch: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM
) {
    val displayZoneName: String
        get() {
            return if (!selectedIslandName.isNullOrBlank()) {
                val prov = SpanishProvinces.findById(selectedProvinceId)
                "$selectedIslandName (${prov.ccaa})"
            } else {
                selectedProvinceName
            }
        }
}

class PreferencesRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("gasolina_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        val provinceId = prefs.getString("selected_province_id", "28") ?: "28"
        val province = SpanishProvinces.findById(provinceId)
        val islandId = prefs.getString("selected_island_id", null)
        val island = SpanishProvinces.findIslandById(islandId)
        val fuelTypeId = prefs.getString("selected_fuel_type", FuelType.GASOLINA_95.id)
        val isFirstTime = prefs.getBoolean("is_first_time", true)
        val notifications = prefs.getBoolean("notifications_enabled", true)
        val themeModeStr = prefs.getString("theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        val themeMode = try { AppThemeMode.valueOf(themeModeStr) } catch (e: Exception) { AppThemeMode.SYSTEM }

        return UserSettings(
            selectedProvinceId = province.id,
            selectedProvinceName = province.name,
            selectedIslandId = island?.id,
            selectedIslandName = island?.name,
            selectedFuelType = FuelType.fromId(fuelTypeId),
            isFirstTimeLaunch = isFirstTime,
            notificationsEnabled = notifications,
            themeMode = themeMode
        )
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _settings.value = _settings.value.copy(themeMode = mode)
    }

    fun setProvince(provinceId: String, islandId: String? = null) {
        val province = SpanishProvinces.findById(provinceId)
        val island = SpanishProvinces.findIslandById(islandId)
        prefs.edit()
            .putString("selected_province_id", province.id)
            .putString("selected_island_id", island?.id)
            .apply()
        _settings.value = _settings.value.copy(
            selectedProvinceId = province.id,
            selectedProvinceName = province.name,
            selectedIslandId = island?.id,
            selectedIslandName = island?.name
        )
    }

    fun setIsland(islandId: String?) {
        val island = SpanishProvinces.findIslandById(islandId)
        prefs.edit()
            .putString("selected_island_id", island?.id)
            .apply()
        _settings.value = _settings.value.copy(
            selectedIslandId = island?.id,
            selectedIslandName = island?.name
        )
    }

    fun setFuelType(fuelType: FuelType) {
        prefs.edit()
            .putString("selected_fuel_type", fuelType.id)
            .apply()
        _settings.value = _settings.value.copy(selectedFuelType = fuelType)
    }

    fun completeOnboarding() {
        prefs.edit().putBoolean("is_first_time", false).apply()
        _settings.value = _settings.value.copy(isFirstTimeLaunch = false)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
        _settings.value = _settings.value.copy(notificationsEnabled = enabled)
    }
}
