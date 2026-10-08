package com.example.alfajr.core.calendar

/**
 * Representation of an Islamic holiday or blessed occasion.
 */
data class IslamicHoliday(
    val id: String,
    val nameEnglish: String,
    val nameArabic: String,
    val hijriMonth: Int,
    val hijriDay: Int,
    val description: String = ""
)

/**
 * Registry containing the foundational Islamic holidays and holy occasions.
 * Allows easy checking for special days on the watchface or calendar.
 */
object IslamicHolidayRegistry {

    val HOLIDAYS: List<IslamicHoliday> = listOf(
        IslamicHoliday(
            id = "islamic_new_year",
            nameEnglish = "Islamic New Year",
            nameArabic = "رأس السنة الهجرية",
            hijriMonth = 1,
            hijriDay = 1,
            description = "1st of Muharram, marks the beginning of the new Hijri year."
        ),
        IslamicHoliday(
            id = "ashura",
            nameEnglish = "Day of Ashura",
            nameArabic = "يوم عاشوراء",
            hijriMonth = 1,
            hijriDay = 10,
            description = "10th of Muharram, day of solemn fasting and reflection."
        ),
        IslamicHoliday(
            id = "mawlid",
            nameEnglish = "Mawlid al-Nabi",
            nameArabic = "المولد النبوي الشريف",
            hijriMonth = 3,
            hijriDay = 12,
            description = "Birth of Prophet Muhammad (peace be upon him)."
        ),
        IslamicHoliday(
            id = "isra_miraj",
            nameEnglish = "Al-Isra wal-Mi'raj",
            nameArabic = "الإسراء والمعراج",
            hijriMonth = 7,
            hijriDay = 27,
            description = "The miraculous Night Journey and Ascension."
        ),
        IslamicHoliday(
            id = "mid_shaban",
            nameEnglish = "Mid-Sha'ban (Nisf Sha'ban)",
            nameArabic = "ليلة النصف من شعبان",
            hijriMonth = 8,
            hijriDay = 15,
            description = "Night of forgiveness and preparation for Ramadan."
        ),
        IslamicHoliday(
            id = "ramadan_start",
            nameEnglish = "First Day of Ramadan",
            nameArabic = "بداية شهر رمضان المبارك",
            hijriMonth = 9,
            hijriDay = 1,
            description = "Beginning of the Holy Month of Fasting."
        ),
        IslamicHoliday(
            id = "laylat_al_qadr",
            nameEnglish = "Laylat al-Qadr",
            nameArabic = "ليلة القدر",
            hijriMonth = 9,
            hijriDay = 27,
            description = "The Night of Power and Decree in Ramadan."
        ),
        IslamicHoliday(
            id = "eid_al_fitr",
            nameEnglish = "Eid al-Fitr",
            nameArabic = "عيد الفطر المبارك",
            hijriMonth = 10,
            hijriDay = 1,
            description = "Festival of Breaking the Fast concluding Ramadan."
        ),
        IslamicHoliday(
            id = "day_of_arafah",
            nameEnglish = "Day of Arafah",
            nameArabic = "يوم عرفة",
            hijriMonth = 12,
            hijriDay = 9,
            description = "9th of Dhu al-Hijjah, pinnacle day of Hajj."
        ),
        IslamicHoliday(
            id = "eid_al_adha",
            nameEnglish = "Eid al-Adha",
            nameArabic = "عيد الأضحى المبارك",
            hijriMonth = 12,
            hijriDay = 10,
            description = "Festival of Sacrifice celebrated globally."
        )
    )

    /**
     * Check if a given Hijri date corresponds to an Islamic holiday.
     */
    fun getHoliday(date: HijriDate): IslamicHoliday? {
        return HOLIDAYS.firstOrNull { it.hijriMonth == date.month && it.hijriDay == date.day }
    }

    /**
     * Check if the date is within the blessed month of Ramadan.
     */
    fun isRamadan(date: HijriDate): Boolean {
        return date.month == 9
    }

    /**
     * Returns all Islamic holidays occurring within a given Hijri month.
     */
    fun getHolidaysInMonth(hijriMonth: Int): List<IslamicHoliday> {
        return HOLIDAYS.filter { it.hijriMonth == hijriMonth }
    }
}
