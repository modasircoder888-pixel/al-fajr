package com.example.alfajr.core.calendar

import java.util.Calendar

/**
 * High-performance, offline algorithmic Hijri calendar engine.
 * Converts Gregorian dates to Islamic (Hijri) dates and vice-versa
 * using the standard astronomical Julian Day Number (JDN) algorithm.
 *
 * Fully compatible with Android 8.1 (API 27) and lower without any external dependencies.
 * Supports configurable Hijri date adjustments (-2 to +2 days) for local moon-sighting variance.
 */
class HijriCalendarEngine(
    private var adjustmentDays: Int = 0
) {

    /**
     * Set the manual day adjustment (-2, -1, 0, +1, +2 days) based on user preference or local sighting.
     */
    fun setAdjustment(days: Int) {
        adjustmentDays = days.coerceIn(-2, 2)
    }

    fun getAdjustment(): Int = adjustmentDays

    /**
     * Converts a Gregorian date to a Hijri date with the current adjustment.
     */
    fun toHijri(gregorian: GregorianDate, customAdjustment: Int = adjustmentDays): HijriDate {
        val jdn = gregorianToJulianDay(gregorian.year, gregorian.month, gregorian.day) + customAdjustment
        return julianDayToHijri(jdn, gregorian.dayOfWeek)
    }

    /**
     * Converts a java.util.Calendar instance directly to HijriDate.
     */
    fun toHijri(calendar: Calendar, customAdjustment: Int = adjustmentDays): HijriDate {
        val gregorian = GregorianDate.fromCalendar(calendar)
        return toHijri(gregorian, customAdjustment)
    }

    /**
     * Returns today's Hijri date based on system clock.
     */
    fun today(customAdjustment: Int = adjustmentDays): HijriDate {
        val cal = Calendar.getInstance()
        return toHijri(cal, customAdjustment)
    }

    /**
     * Determines whether a Hijri year is a leap (kabisa) year in the 30-year cycle.
     * The 30-year Islamic cycle has 11 leap years: 2, 5, 7, 10, 13, 16, 18, 21, 24, 26, 29.
     */
    fun isLeapYear(year: Int): Boolean {
        val remainder = (year * 11 + 14) % 30
        return remainder < 11
    }

    /**
     * Returns the number of days in a given Hijri month.
     * Odd months typically have 30 days, even months have 29 days,
     * except month 12 (Dhu al-Hijjah) which has 30 days in a leap year.
     */
    fun getDaysInMonth(year: Int, month: Int): Int {
        return when {
            month % 2 != 0 -> 30
            month == 12 -> if (isLeapYear(year)) 30 else 29
            else -> 29
        }
    }

    /**
     * Navigate to the next Hijri month, keeping day clamped if necessary.
     */
    fun nextMonth(current: HijriDate): HijriDate {
        val nextMonth = if (current.month == 12) 1 else current.month + 1
        val nextYear = if (current.month == 12) current.year + 1 else current.year
        val maxDays = getDaysInMonth(nextYear, nextMonth)
        val clampedDay = current.day.coerceAtMost(maxDays)
        // Recalculate day of week
        val approxJdn = hijriToJulianDay(nextYear, nextMonth, clampedDay)
        val dayOfWeek = ((approxJdn + 1.5).toInt() % 7) + 1
        return HijriDate(nextYear, nextMonth, clampedDay, dayOfWeek)
    }

    /**
     * Navigate to the previous Hijri month.
     */
    fun previousMonth(current: HijriDate): HijriDate {
        val prevMonth = if (current.month == 1) 12 else current.month - 1
        val prevYear = if (current.month == 1) current.year - 1 else current.year
        val maxDays = getDaysInMonth(prevYear, prevMonth)
        val clampedDay = current.day.coerceAtMost(maxDays)
        val approxJdn = hijriToJulianDay(prevYear, prevMonth, clampedDay)
        val dayOfWeek = ((approxJdn + 1.5).toInt() % 7) + 1
        return HijriDate(prevYear, prevMonth, clampedDay, dayOfWeek)
    }

    /**
     * Julian Day Number from Gregorian Date.
     */
    fun gregorianToJulianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = y / 100
        val b = 2 - a + (a / 4)
        return (Math.floor(365.25 * (y + 4716)) +
                Math.floor(30.6001 * (m + 1)) +
                day + b - 1524.5)
    }

    /**
     * Converts a Julian Day Number to a Hijri Date.
     */
    fun julianDayToHijri(jdn: Double, dayOfWeek: Int): HijriDate {
        val z = Math.floor(jdn + 0.5).toLong()
        val l0 = z - 1948440 + 10632
        val n = (l0 - 1) / 10631
        val l = l0 - 10631 * n + 354
        val j = (((10985 - l) / 5316) * ((50 * l) / 17719) +
                (l / 5670) * ((43 * l) / 15238))
        val l2 = l - (((30 - j) / 15) * ((17719 * j) / 50)) -
                ((j / 16) * ((15238 * j) / 43)) + 29
        val m = (24 * l2) / 709
        val d = l2 - (709 * m) / 24
        val y = 30 * n + j - 30

        return HijriDate(
            year = y.toInt(),
            month = m.toInt(),
            day = d.toInt(),
            dayOfWeek = dayOfWeek
        )
    }

    /**
     * Converts a Hijri date back to Julian Day Number.
     */
    fun hijriToJulianDay(year: Int, month: Int, day: Int): Double {
        val y = year.toLong()
        val m = month.toLong()
        val d = day.toLong()
        val epoch = 1948439.5
        val jdn = d + Math.ceil(29.5 * (m - 1)) + (y - 1) * 354 +
                Math.floor((3 + 11 * y) / 30.0) + epoch - 1
        return jdn
    }

    /**
     * Converts a Julian Day Number to Gregorian Date.
     */
    fun julianDayToGregorian(jdn: Double): GregorianDate {
        val z = Math.floor(jdn + 0.5).toInt()
        val a = if (z < 2299161) z else {
            val alpha = Math.floor((z - 1867216.25) / 36524.25).toInt()
            z + 1 + alpha - Math.floor(alpha / 4.0).toInt()
        }
        val b = a + 1524
        val c = Math.floor((b - 122.1) / 365.25).toInt()
        val d = Math.floor(365.25 * c).toInt()
        val e = Math.floor((b - d) / 30.6001).toInt()
        val day = b - d - Math.floor(30.6001 * e).toInt()
        val month = if (e < 14) e - 1 else e - 13
        val year = if (month > 2) c - 4716 else c - 4715
        val dayOfWeek = ((Math.floor(jdn + 1.5).toInt()) % 7) + 1
        return GregorianDate(year, month, day, dayOfWeek)
    }

    /**
     * Converts a HijriDate to its corresponding GregorianDate.
     */
    fun toGregorian(hijri: HijriDate): GregorianDate {
        val jdn = hijriToJulianDay(hijri.year, hijri.month, hijri.day) - adjustmentDays
        return julianDayToGregorian(jdn)
    }
}
