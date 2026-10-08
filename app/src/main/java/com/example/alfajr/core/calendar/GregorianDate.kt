package com.example.alfajr.core.calendar

import java.util.Calendar
import java.util.Locale

/**
 * Clean data model representing a Gregorian date.
 * Fully compatible with Android 8.1 and minSdk 24 without desugaring overhead.
 */
data class GregorianDate(
    val year: Int,
    val month: Int, // 1 to 12
    val day: Int,   // 1 to 31
    val dayOfWeek: Int // Calendar.SUNDAY .. Calendar.SATURDAY
) {
    val dayOfWeekName: String
        get() = when (dayOfWeek) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> ""
        }

    val dayOfWeekShort: String
        get() = when (dayOfWeek) {
            Calendar.SUNDAY -> "Sun"
            Calendar.MONDAY -> "Mon"
            Calendar.TUESDAY -> "Tue"
            Calendar.WEDNESDAY -> "Wed"
            Calendar.THURSDAY -> "Thu"
            Calendar.FRIDAY -> "Fri"
            Calendar.SATURDAY -> "Sat"
            else -> ""
        }

    val monthName: String
        get() = when (month) {
            1 -> "January"
            2 -> "February"
            3 -> "March"
            4 -> "April"
            5 -> "May"
            6 -> "June"
            7 -> "July"
            8 -> "August"
            9 -> "September"
            10 -> "October"
            11 -> "November"
            12 -> "December"
            else -> ""
        }

    val monthNameShort: String
        get() = when (month) {
            1 -> "Jan"
            2 -> "Feb"
            3 -> "Mar"
            4 -> "Apr"
            5 -> "May"
            6 -> "Jun"
            7 -> "Jul"
            8 -> "Aug"
            9 -> "Sep"
            10 -> "Oct"
            11 -> "Nov"
            12 -> "Dec"
            else -> ""
        }

    /**
     * Compact display suitable for 320px smartwatch screen, e.g. "Wed, 7 Oct 2026"
     */
    val formattedDisplay: String
        get() = "$dayOfWeekShort, $day $monthNameShort $year"

    companion object {
        fun fromCalendar(calendar: Calendar): GregorianDate {
            return GregorianDate(
                year = calendar.get(Calendar.YEAR),
                month = calendar.get(Calendar.MONTH) + 1,
                day = calendar.get(Calendar.DAY_OF_MONTH),
                dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            )
        }

        fun today(): GregorianDate {
            return fromCalendar(Calendar.getInstance())
        }
    }
}
