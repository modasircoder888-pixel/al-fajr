package com.example.alfajr.data.prayer

import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

/**
 * High-precision, completely offline astronomical prayer time calculator.
 * Implements standard solar positioning algorithms without any external dependencies.
 * Designed for low-power SC9832E smartwatch execution (runs in < 1 millisecond).
 */
object AstronomicalPrayerCalculator {

    private const val DEG_TO_RAD = Math.PI / 180.0
    private const val RAD_TO_DEG = 180.0 / Math.PI

    private fun dsin(d: Double): Double = sin(d * DEG_TO_RAD)
    private fun dcos(d: Double): Double = cos(d * DEG_TO_RAD)
    private fun dtan(d: Double): Double = tan(d * DEG_TO_RAD)
    private fun dasin(r: Double): Double = asin(r) * RAD_TO_DEG
    private fun dacos(r: Double): Double = acos(r) * RAD_TO_DEG
    private fun datan(r: Double): Double = atan(r) * RAD_TO_DEG
    private fun datan2(y: Double, x: Double): Double = atan2(y, x) * RAD_TO_DEG

    private fun fixAngle(a: Double): Double {
        var res = a - 360.0 * floor(a / 360.0)
        if (res < 0) res += 360.0
        return res
    }

    private fun fixHour(h: Double): Double {
        var res = h - 24.0 * floor(h / 24.0)
        if (res < 0) res += 24.0
        return res
    }

    /**
     * Julian Day from calendar date.
     */
    private fun julianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    /**
     * Calculates prayer times for a given day and settings.
     * Returns a map of PrayerType to Double hours of the day (e.g. 5.25 = 05:15).
     */
    fun calculateTimes(
        year: Int,
        month: Int,
        day: Int,
        settings: PrayerCalculationSettings,
        timeZone: TimeZone = TimeZone.getDefault()
    ): Map<PrayerType, Double> {
        val lat = settings.coordinates.latitude
        val lng = settings.coordinates.longitude
        val tzHours = timeZone.getOffset(
            Calendar.getInstance(timeZone).apply {
                set(year, month - 1, day, 12, 0, 0)
            }.timeInMillis
        ) / 3600000.0

        val jd = julianDay(year, month, day)
        val d = jd - 2451545.0 // Days since J2000.0

        // Mean anomaly of the Sun
        val g = fixAngle(357.529 + 0.98560028 * d)
        // Mean longitude of the Sun
        val q = fixAngle(280.459 + 0.98564736 * d)
        // Ecliptic longitude of the Sun
        val l = fixAngle(q + 1.915 * dsin(g) + 0.020 * dsin(2 * g))
        // Obliquity of ecliptic
        val e = 23.439 - 0.00000036 * d

        // Sun's declination
        val declination = dasin(dsin(e) * dsin(l))
        // Right ascension in degrees
        var ra = datan2(dcos(e) * dsin(l), dcos(l))
        ra = fixAngle(ra) / 15.0 // Right ascension in hours

        // Equation of Time in hours
        val eqt = q / 15.0 - ra

        // Solar Noon / Dhuhr in local clock hours
        val noon = fixHour(12.0 + tzHours - (lng / 15.0) - eqt)

        // Helper to compute hour angle for sun elevation angle alpha
        fun hourAngle(alpha: Double): Double {
            val cosH = (dsin(alpha) - dsin(lat) * dsin(declination)) / (dcos(lat) * dcos(declination))
            return if (cosH > 1.0) 0.0 else if (cosH < -1.0) 180.0 else dacos(cosH) / 15.0
        }

        // Standard refraction and sun angle for Sunrise/Sunset
        val sunriseSunsetAngle = -0.8333
        val hSun = hourAngle(sunriseSunsetAngle)

        val sunrise = noon - hSun
        val maghrib = noon + hSun

        // Fajr calculation
        val method = settings.calculationMethod
        val hFajr = hourAngle(-method.fajrAngle)
        var fajr = noon - hFajr

        // Asr calculation
        val shadowFactor = settings.asrMethod.shadowRatio.toDouble()
        val asrAngle = datan(1.0 / (shadowFactor + dtan(abs(lat - declination))))
        val hAsr = hourAngle(asrAngle)
        val asr = noon + hAsr

        // Isha calculation
        var isha = if (method.isIshaFixedMinutes) {
            maghrib + (method.ishaMinutesAfterMaghrib / 60.0)
        } else {
            val hIsha = hourAngle(-method.ishaAngle)
            noon + hIsha
        }

        // High latitude adjustment fallback if night length is too short
        val nightDuration = fixHour(sunrise - maghrib + 24.0)
        if (hFajr == 0.0 || hFajr == 12.0) {
            fajr = when (settings.highLatitudeRule) {
                HighLatitudeRule.MIDDLE_OF_NIGHT -> sunrise - (nightDuration / 2.0)
                HighLatitudeRule.ONE_SEVENTH -> sunrise - (nightDuration / 7.0)
                HighLatitudeRule.ANGLE_BASED -> sunrise - (nightDuration * (method.fajrAngle / 60.0))
            }
        }
        if (!method.isIshaFixedMinutes && (hourAngle(-method.ishaAngle) == 0.0 || hourAngle(-method.ishaAngle) == 12.0)) {
            isha = when (settings.highLatitudeRule) {
                HighLatitudeRule.MIDDLE_OF_NIGHT -> maghrib + (nightDuration / 2.0)
                HighLatitudeRule.ONE_SEVENTH -> maghrib + (nightDuration / 7.0)
                HighLatitudeRule.ANGLE_BASED -> maghrib + (nightDuration * (method.ishaAngle / 60.0))
            }
        }

        // Apply manual user offsets in minutes
        fun applyOffset(time: Double, type: PrayerType): Double {
            val offsetMin = settings.userOffsetsMinutes[type] ?: 0
            return fixHour(time + (offsetMin / 60.0))
        }

        return mapOf(
            PrayerType.FAJR to applyOffset(fajr, PrayerType.FAJR),
            PrayerType.SUNRISE to applyOffset(sunrise, PrayerType.SUNRISE),
            PrayerType.DHUHR to applyOffset(noon, PrayerType.DHUHR),
            PrayerType.ASR to applyOffset(asr, PrayerType.ASR),
            PrayerType.MAGHRIB to applyOffset(maghrib, PrayerType.MAGHRIB),
            PrayerType.ISHA to applyOffset(isha, PrayerType.ISHA)
        )
    }

    /**
     * Converts fractional hours to hour (0..23) and minute (0..59).
     */
    fun toHourAndMinute(fractionalHours: Double): Pair<Int, Int> {
        val totalMinutes = Math.round(fixHour(fractionalHours) * 60.0).toInt()
        val h = (totalMinutes / 60) % 24
        val m = totalMinutes % 60
        return Pair(h, m)
    }
}
