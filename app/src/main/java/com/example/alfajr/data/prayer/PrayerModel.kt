package com.example.alfajr.data.prayer

enum class PrayerType(
    val englishName: String,
    val arabicName: String
) {
    FAJR("FAJR", "الفجر"),
    SUNRISE("SUNRISE", "الشروق"),
    DHUHR("DHUHR", "الظهر"),
    ASR("ASR", "العصر"),
    MAGHRIB("MAGHRIB", "المغرب"),
    ISHA("ISHA", "العشاء")
}

data class PrayerScheduleItem(
    val type: PrayerType,
    val hour24: Int,
    val minute: Int,
    val formattedTime: String,
    val formattedTime12: String,
    val formattedTime24: String,
    val isNext: Boolean = false,
    val isPassed: Boolean = false,
    val timestampMs: Long = 0L
)

data class PrayerScheduleSnapshot(
    val items: List<PrayerScheduleItem>,
    val nextPrayer: PrayerScheduleItem?,
    val countdownSeconds: Long,
    val countdownFormatted: String // e.g. "01:24:37"
)
