package com.example.alfajr.data.prayer

import java.util.Calendar
import java.util.Locale

interface PrayerRepository {
    fun getPrayerSchedule(
        currentCalendar: Calendar,
        settings: PrayerCalculationSettings = PrayerCalculationSettings(),
        is24HourFormat: Boolean = false
    ): PrayerScheduleSnapshot
}

class AstronomicalPrayerRepository : PrayerRepository {

    override fun getPrayerSchedule(
        currentCalendar: Calendar,
        settings: PrayerCalculationSettings,
        is24HourFormat: Boolean
    ): PrayerScheduleSnapshot {
        val year = currentCalendar.get(Calendar.YEAR)
        val month = currentCalendar.get(Calendar.MONTH) + 1
        val day = currentCalendar.get(Calendar.DAY_OF_MONTH)
        val currentMs = currentCalendar.timeInMillis

        // Calculate astronomical times for today
        val todayTimes = AstronomicalPrayerCalculator.calculateTimes(
            year = year,
            month = month,
            day = day,
            settings = settings,
            timeZone = currentCalendar.timeZone
        )

        // Build schedule items for today with absolute timestamps
        val prayerOrder = listOf(
            PrayerType.FAJR,
            PrayerType.SUNRISE,
            PrayerType.DHUHR,
            PrayerType.ASR,
            PrayerType.MAGHRIB,
            PrayerType.ISHA
        )

        var nextItem: PrayerScheduleItem? = null
        val items = prayerOrder.map { type ->
            val fractionalHours = todayTimes[type] ?: 12.0
            val (h, m) = AstronomicalPrayerCalculator.toHourAndMinute(fractionalHours)

            val prayerCal = (currentCalendar.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, m)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val prayerMs = prayerCal.timeInMillis
            val isPassed = prayerMs <= currentMs
            val isNext = !isPassed && nextItem == null

            val time12 = formatTime12h(h, m)
            val time24 = String.format(Locale.US, "%02d:%02d", h, m)
            val formatted = if (is24HourFormat) time24 else time12

            val item = PrayerScheduleItem(
                type = type,
                hour24 = h,
                minute = m,
                formattedTime = formatted,
                formattedTime12 = time12,
                formattedTime24 = time24,
                isNext = isNext,
                isPassed = isPassed,
                timestampMs = prayerMs
            )

            if (isNext) {
                nextItem = item
            }

            item
        }

        // If all prayers today passed (e.g. after Isha), next prayer is tomorrow's Fajr
        val resolvedNext = if (nextItem == null) {
            val tomorrowCal = (currentCalendar.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, 1)
            }
            val tomorrowTimes = AstronomicalPrayerCalculator.calculateTimes(
                year = tomorrowCal.get(Calendar.YEAR),
                month = tomorrowCal.get(Calendar.MONTH) + 1,
                day = tomorrowCal.get(Calendar.DAY_OF_MONTH),
                settings = settings,
                timeZone = tomorrowCal.timeZone
            )
            val (fajrH, fajrM) = AstronomicalPrayerCalculator.toHourAndMinute(
                tomorrowTimes[PrayerType.FAJR] ?: 5.0
            )
            tomorrowCal.set(Calendar.HOUR_OF_DAY, fajrH)
            tomorrowCal.set(Calendar.MINUTE, fajrM)
            tomorrowCal.set(Calendar.SECOND, 0)
            tomorrowCal.set(Calendar.MILLISECOND, 0)

            val time12 = formatTime12h(fajrH, fajrM)
            val time24 = String.format(Locale.US, "%02d:%02d", fajrH, fajrM)
            PrayerScheduleItem(
                type = PrayerType.FAJR,
                hour24 = fajrH,
                minute = fajrM,
                formattedTime = if (is24HourFormat) time24 else time12,
                formattedTime12 = time12,
                formattedTime24 = time24,
                isNext = true,
                isPassed = false,
                timestampMs = tomorrowCal.timeInMillis
            )
        } else {
            nextItem
        }

        // Calculate countdown to the next prayer in seconds
        val targetMs = resolvedNext?.timestampMs ?: currentMs
        val diffMs = (targetMs - currentMs).coerceAtLeast(0L)
        val countdownSeconds = diffMs / 1000L

        val hours = countdownSeconds / 3600L
        val minutes = (countdownSeconds % 3600L) / 60L
        val seconds = countdownSeconds % 60L
        val countdownFormatted = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)

        return PrayerScheduleSnapshot(
            items = items,
            nextPrayer = resolvedNext,
            countdownSeconds = countdownSeconds,
            countdownFormatted = countdownFormatted
        )
    }

    private fun formatTime12h(hour24: Int, minute: Int): String {
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        return String.format(Locale.US, "%02d:%02d", hour12, minute)
    }
}

// Retain DefaultPrayerRepository as alias for backward compatibility
typealias DefaultPrayerRepository = AstronomicalPrayerRepository
