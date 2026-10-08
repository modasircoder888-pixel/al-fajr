package com.example.alfajr.data.prayer

/**
 * Calculation methods approved by major Islamic authorities worldwide.
 */
enum class CalculationMethod(
    val title: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val isIshaFixedMinutes: Boolean = false,
    val ishaMinutesAfterMaghrib: Int = 90
) {
    UMM_AL_QURA(
        title = "Umm al-Qura (Makkah)",
        fajrAngle = 18.5,
        ishaAngle = 0.0,
        isIshaFixedMinutes = true,
        ishaMinutesAfterMaghrib = 90
    ),
    MUSLIM_WORLD_LEAGUE(
        title = "Muslim World League",
        fajrAngle = 18.0,
        ishaAngle = 17.0
    ),
    EGYPTIAN(
        title = "Egyptian General Authority",
        fajrAngle = 19.5,
        ishaAngle = 17.5
    ),
    ISNA(
        title = "ISNA (North America)",
        fajrAngle = 15.0,
        ishaAngle = 15.0
    ),
    KARACHI(
        title = "Univ. of Islamic Sciences, Karachi",
        fajrAngle = 18.0,
        ishaAngle = 18.0
    ),
    GULF(
        title = "Gulf Region (90 min Isha)",
        fajrAngle = 19.5,
        ishaAngle = 0.0,
        isIshaFixedMinutes = true,
        ishaMinutesAfterMaghrib = 90
    )
}

/**
 * Asr jurisprudential shadow ratio.
 */
enum class AsrMethod(val title: String, val shadowRatio: Int) {
    STANDARD("Standard (Shafi'i, Maliki, Hanbali)", 1),
    HANAFI("Hanafi", 2)
}

/**
 * High-latitude night proportion rules for polar and sub-polar regions.
 */
enum class HighLatitudeRule(val title: String) {
    MIDDLE_OF_NIGHT("Middle of Night"),
    ONE_SEVENTH("One Seventh of Night"),
    ANGLE_BASED("Angle Based")
}

data class PrayerCoordinates(
    val latitude: Double,
    val longitude: Double,
    val locationName: String = "Makkah"
) {
    companion object {
        // Standard presets for instant offline setup on smartwatch
        val MAKKAH = PrayerCoordinates(21.4225, 39.8262, "Makkah")
        val MADINAH = PrayerCoordinates(24.4672, 39.6111, "Madinah")
        val RIYADH = PrayerCoordinates(24.7136, 46.6753, "Riyadh")
        val CAIRO = PrayerCoordinates(30.0444, 31.2357, "Cairo")
        val DUBAI = PrayerCoordinates(25.2048, 55.2708, "Dubai")
        val ISTANBUL = PrayerCoordinates(41.0082, 28.9784, "Istanbul")
        val JAKARTA = PrayerCoordinates(-6.2088, 106.8456, "Jakarta")
        val LONDON = PrayerCoordinates(51.5074, -0.1278, "London")
        val NEW_YORK = PrayerCoordinates(40.7128, -74.0060, "New York")

        val DEFAULT_PRESETS = listOf(
            MAKKAH, MADINAH, RIYADH, CAIRO, DUBAI, ISTANBUL, JAKARTA, LONDON, NEW_YORK
        )
    }
}

data class PrayerCalculationSettings(
    val coordinates: PrayerCoordinates = PrayerCoordinates.MAKKAH,
    val calculationMethod: CalculationMethod = CalculationMethod.UMM_AL_QURA,
    val asrMethod: AsrMethod = AsrMethod.STANDARD,
    val highLatitudeRule: HighLatitudeRule = HighLatitudeRule.ANGLE_BASED,
    val userOffsetsMinutes: Map<PrayerType, Int> = emptyMap()
)
