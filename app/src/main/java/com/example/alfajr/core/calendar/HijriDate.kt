package com.example.alfajr.core.calendar

/**
 * Representation of an Islamic Hijri calendar date.
 * Designed with full awareness of Islamic calendar rules,
 * supporting both English and Arabic nomenclature.
 */
data class HijriDate(
    val year: Int,
    val month: Int, // 1 to 12
    val day: Int,   // 1 to 30
    val dayOfWeek: Int // Calendar.SUNDAY .. Calendar.SATURDAY
) {
    val monthNameEnglish: String
        get() = when (month) {
            1 -> "Muharram"
            2 -> "Safar"
            3 -> "Rabi' al-Awwal"
            4 -> "Rabi' al-Thani"
            5 -> "Jumada al-Ula"
            6 -> "Jumada al-Akhirah"
            7 -> "Rajab"
            8 -> "Sha'ban"
            9 -> "Ramadan"
            10 -> "Shawwal"
            11 -> "Dhu al-Qi'dah"
            12 -> "Dhu al-Hijjah"
            else -> "Unknown"
        }

    val monthNameArabic: String
        get() = when (month) {
            1 -> "مُحَرَّم"
            2 -> "صَفَر"
            3 -> "رَبِيع الأَوَّل"
            4 -> "رَبِيع الآخِر"
            5 -> "جُمَادَى الأُولَى"
            6 -> "جُمَادَى الآخِرَة"
            7 -> "رَجَب"
            8 -> "شَعْبَان"
            9 -> "رَمَضَان"
            10 -> "شَوَّال"
            11 -> "ذُو القَعْدَة"
            12 -> "ذُو الحِجَّة"
            else -> ""
        }

    val monthShortEnglish: String
        get() = when (month) {
            1 -> "Muh"
            2 -> "Saf"
            3 -> "Rab I"
            4 -> "Rab II"
            5 -> "Jum I"
            6 -> "Jum II"
            7 -> "Raj"
            8 -> "Sha"
            9 -> "Ram"
            10 -> "Shaw"
            11 -> "Dhu-Q"
            12 -> "Dhu-H"
            else -> ""
        }

    /**
     * Compact display string suited for smartwatch screens:
     * e.g., "25 Rabi' al-Thani 1448 AH"
     */
    val formattedDisplay: String
        get() = "$day $monthNameEnglish $year AH"

    /**
     * Ultra compact display string:
     * e.g., "25 Rab II 1448"
     */
    val formattedShort: String
        get() = "$day $monthShortEnglish $year"

    /**
     * Arabic display string:
     * e.g., "٢٥ ربيع الآخر ١٤٤٨ هـ"
     */
    val formattedArabic: String
        get() = "$day $monthNameArabic $year هـ"
}
