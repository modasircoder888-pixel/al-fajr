package com.example.alfajr.core.time

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import java.util.Calendar
import java.util.Locale

/**
 * Snapshot of the current watch time.
 */
data class WatchTime(
    val hours24: Int,
    val hours12: Int,
    val minutes: Int,
    val seconds: Int,
    val isAm: Boolean,
    val calendar: Calendar
) {
    val formattedClock: String
        get() = String.format(Locale.US, "%02d:%02d", hours24, minutes)

    val formattedClock12: String
        get() = String.format(Locale.US, "%d:%02d", if (hours12 == 0) 12 else hours12, minutes)

    val formattedSeconds: String
        get() = String.format(Locale.US, "%02d", seconds)

    val amPmText: String
        get() = if (isAm) "AM" else "PM"

    companion object {
        fun now(): WatchTime {
            val cal = Calendar.getInstance()
            val h24 = cal.get(Calendar.HOUR_OF_DAY)
            val h12 = cal.get(Calendar.HOUR)
            val m = cal.get(Calendar.MINUTE)
            val s = cal.get(Calendar.SECOND)
            val am = cal.get(Calendar.AM_PM) == Calendar.AM
            return WatchTime(
                hours24 = h24,
                hours12 = h12,
                minutes = m,
                seconds = s,
                isAm = am,
                calendar = cal
            )
        }
    }
}

/**
 * Emits updated [WatchTime] every second while active.
 * Pauses automatically when coroutine scope is cancelled or inactive.
 */
object TimeTicker {
    fun tickerFlow(): Flow<WatchTime> = flow {
        while (currentCoroutineContext().isActive) {
            emit(WatchTime.now())
            // Align to next whole second for clean digital clock transitions
            val nowMs = System.currentTimeMillis()
            val sleepMs = 1000 - (nowMs % 1000)
            delay(sleepMs.coerceAtLeast(100))
        }
    }
}
