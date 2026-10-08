package com.example.alfajr.data.settings

import android.content.Context
import android.content.SharedPreferences
import com.example.alfajr.data.prayer.AsrMethod
import com.example.alfajr.data.prayer.CalculationMethod
import com.example.alfajr.data.prayer.HighLatitudeRule
import com.example.alfajr.data.prayer.PrayerCalculationSettings
import com.example.alfajr.data.prayer.PrayerCoordinates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AlFajrSettings(
    val is24HourFormat: Boolean = false,
    val hijriAdjustment: Int = 0,
    val calculationMethod: CalculationMethod = CalculationMethod.UMM_AL_QURA,
    val asrMethod: AsrMethod = AsrMethod.STANDARD,
    val highLatitudeRule: HighLatitudeRule = HighLatitudeRule.ANGLE_BASED,
    val coordinates: PrayerCoordinates = PrayerCoordinates.MAKKAH
) {
    fun toPrayerSettings(): PrayerCalculationSettings {
        return PrayerCalculationSettings(
            coordinates = coordinates,
            calculationMethod = calculationMethod,
            asrMethod = asrMethod,
            highLatitudeRule = highLatitudeRule
        )
    }
}

/**
 * Lightweight, fast SharedPreferences-backed settings manager.
 * Fully compatible with Android 8.1 / SC9832E smartwatch without any database overhead.
 */
class AlFajrSettingsManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("al_fajr_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AlFajrSettings> = _settings.asStateFlow()

    private fun loadSettings(): AlFajrSettings {
        val is24h = prefs.getBoolean("is_24_hour", false)
        val hijriAdj = prefs.getInt("hijri_adjustment", 0)
        val methodStr = prefs.getString("calc_method", CalculationMethod.UMM_AL_QURA.name) ?: CalculationMethod.UMM_AL_QURA.name
        val asrStr = prefs.getString("asr_method", AsrMethod.STANDARD.name) ?: AsrMethod.STANDARD.name
        val highLatStr = prefs.getString("high_lat_rule", HighLatitudeRule.ANGLE_BASED.name) ?: HighLatitudeRule.ANGLE_BASED.name
        val locName = prefs.getString("loc_name", "Makkah") ?: "Makkah"
        val lat = prefs.getFloat("latitude", 21.4225f).toDouble()
        val lng = prefs.getFloat("longitude", 39.8262f).toDouble()

        val method = try {
            CalculationMethod.valueOf(methodStr)
        } catch (_: Exception) {
            CalculationMethod.UMM_AL_QURA
        }

        val asr = try {
            AsrMethod.valueOf(asrStr)
        } catch (_: Exception) {
            AsrMethod.STANDARD
        }

        val highLat = try {
            HighLatitudeRule.valueOf(highLatStr)
        } catch (_: Exception) {
            HighLatitudeRule.ANGLE_BASED
        }

        return AlFajrSettings(
            is24HourFormat = is24h,
            hijriAdjustment = hijriAdj,
            calculationMethod = method,
            asrMethod = asr,
            highLatitudeRule = highLat,
            coordinates = PrayerCoordinates(lat, lng, locName)
        )
    }

    fun setTimeFormat(is24Hour: Boolean) {
        prefs.edit().putBoolean("is_24_hour", is24Hour).apply()
        _settings.value = _settings.value.copy(is24HourFormat = is24Hour)
    }

    fun setHijriAdjustment(adjustment: Int) {
        val clamped = adjustment.coerceIn(-2, 2)
        prefs.edit().putInt("hijri_adjustment", clamped).apply()
        _settings.value = _settings.value.copy(hijriAdjustment = clamped)
    }

    fun setCalculationMethod(method: CalculationMethod) {
        prefs.edit().putString("calc_method", method.name).apply()
        _settings.value = _settings.value.copy(calculationMethod = method)
    }

    fun setAsrMethod(asr: AsrMethod) {
        prefs.edit().putString("asr_method", asr.name).apply()
        _settings.value = _settings.value.copy(asrMethod = asr)
    }

    fun setHighLatitudeRule(rule: HighLatitudeRule) {
        prefs.edit().putString("high_lat_rule", rule.name).apply()
        _settings.value = _settings.value.copy(highLatitudeRule = rule)
    }

    fun setCoordinates(coords: PrayerCoordinates) {
        prefs.edit()
            .putString("loc_name", coords.locationName)
            .putFloat("latitude", coords.latitude.toFloat())
            .putFloat("longitude", coords.longitude.toFloat())
            .apply()
        _settings.value = _settings.value.copy(coordinates = coords)
    }
}
