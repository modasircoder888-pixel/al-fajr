package com.example.alfajr.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alfajr.core.battery.BatteryInfo
import com.example.alfajr.core.calendar.GregorianDate
import com.example.alfajr.core.calendar.HijriDate
import com.example.alfajr.core.time.WatchTime
import com.example.alfajr.data.prayer.PrayerScheduleItem
import com.example.alfajr.ui.watchface.WatchTab
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.BatteryDigitalCharging
import com.example.ui.theme.BatteryDigitalGood
import com.example.ui.theme.BatteryDigitalLow
import com.example.ui.theme.BatteryDigitalMid
import com.example.ui.theme.DarkLcdBackground
import com.example.ui.theme.DigitalGreen
import com.example.ui.theme.DigitalGreenBright
import com.example.ui.theme.DigitalGreenDark
import com.example.ui.theme.DigitalGreenDim
import com.example.ui.theme.DigitalGreenMuted
import com.example.ui.theme.LcdSegmentOff

/**
 * Top LCD status bar showing Weekday, Gregorian date, Hijri date, and Battery.
 * Bold, high-contrast typography optimized for 320x386 watch display.
 */
@Composable
fun WatchTopStatusBar(
    gregorian: GregorianDate,
    hijri: HijriDate,
    battery: BatteryInfo,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("watch_top_status_bar"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Line 1: Day of Week & Battery
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = gregorian.dayOfWeekName.uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = DigitalGreenBright,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    letterSpacing = 1.2.sp
                )
            )

            // Large, clear digital battery gauge
            DigitalBatteryIndicator(
                level = battery.level,
                isCharging = battery.isCharging
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Line 2: Gregorian Date (e.g. 07 OCT 2026)
        Text(
            text = "${String.format("%02d", gregorian.day)} ${gregorian.monthNameShort.uppercase()} ${gregorian.year}",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = DigitalGreen,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                letterSpacing = 1.2.sp
            )
        )

        Spacer(modifier = Modifier.height(1.dp))

        // Line 3: Hijri Date (e.g. 25 RABI II 1448 AH)
        Text(
            text = "${hijri.day} ${hijri.monthShortEnglish.uppercase()} ${hijri.year} AH",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = DigitalGreenBright,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                letterSpacing = 1.2.sp
            )
        )
    }
}

/**
 * Digital battery meter in classic LCD block style with large readable text.
 */
@Composable
fun DigitalBatteryIndicator(
    level: Int,
    isCharging: Boolean,
    modifier: Modifier = Modifier
) {
    val meterColor = when {
        isCharging -> BatteryDigitalCharging
        level >= 40 -> BatteryDigitalGood
        level >= 20 -> BatteryDigitalMid
        else -> BatteryDigitalLow
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isCharging) {
            Text(
                text = "⚡",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = BatteryDigitalCharging,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
            Spacer(modifier = Modifier.width(2.dp))
        }

        // 4-segment LCD battery gauge (enlarged for watch distance)
        Canvas(modifier = Modifier.size(width = 24.dp, height = 11.dp)) {
            val strokeW = 1.dp.toPx()
            val w = size.width - 2.5.dp.toPx()
            val h = size.height

            // Outer border
            drawRect(
                color = DigitalGreenMuted,
                topLeft = Offset.Zero,
                size = Size(w, h),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
            )
            // Anode tip
            drawRect(
                color = DigitalGreenMuted,
                topLeft = Offset(w, h * 0.25f),
                size = Size(2.5.dp.toPx(), h * 0.5f)
            )

            // Inner bars (4 segments)
            val segments = 4
            val filledSegments = when {
                level >= 75 -> 4
                level >= 50 -> 3
                level >= 25 -> 2
                level > 0 -> 1
                else -> 0
            }

            val segW = (w - (strokeW * 2) - ((segments - 1) * 1.dp.toPx())) / segments
            val segH = h - (strokeW * 2)

            for (i in 0 until segments) {
                val segX = strokeW + i * (segW + 1.dp.toPx())
                val segY = strokeW
                val color = if (i < filledSegments) meterColor else LcdSegmentOff
                drawRect(
                    color = color,
                    topLeft = Offset(segX, segY),
                    size = Size(segW, segH)
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = "$level%",
            style = MaterialTheme.typography.labelLarge.copy(
                color = meterColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        )
    }
}

/**
 * Very large digital clock display (HH:MM :SS).
 * The strongest visual element on the watchface.
 */
@Composable
fun LargeDigitalClock(
    watchTime: WatchTime,
    is24Hour: Boolean,
    modifier: Modifier = Modifier
) {
    val displayHoursMinutes = if (is24Hour) {
        watchTime.formattedClock
    } else {
        watchTime.formattedClock12
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkLcdBackground)
            .border(1.2.dp, DigitalGreenMuted, RoundedCornerShape(8.dp))
            .padding(horizontal = 4.dp, vertical = 5.dp)
            .testTag("digital_clock_main"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Massive, bold digital clock numerals in the centerpiece medal
            Text(
                text = displayHoursMinutes,
                style = MaterialTheme.typography.displayLarge.copy(
                    color = DigitalGreen,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 66.sp,
                    lineHeight = 66.sp,
                    letterSpacing = (-0.5).sp
                )
            )

            Spacer(modifier = Modifier.width(5.dp))

            // Sub-display for AM/PM and seconds
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                if (!is24Hour) {
                    Text(
                        text = watchTime.amPmText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = DigitalGreenBright,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AmoledBlack)
                        .border(0.8.dp, DigitalGreenMuted, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = ":${watchTime.formattedSeconds}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = DigitalGreenDim,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * Classic digital watch "NEXT PRAYER ONLY" section.
 * Large, bold prayer name, prayer time, and live countdown.
 */
@Composable
fun NextPrayerOnlySection(
    nextPrayer: PrayerScheduleItem?,
    countdownFormatted: String,
    modifier: Modifier = Modifier
) {
    if (nextPrayer == null) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DigitalGreenDark)
            .border(1.dp, DigitalGreenMuted, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("next_prayer_only_section")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header line: NEXT PRAYER
            Text(
                text = "— NEXT PRAYER —",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = DigitalGreenDim,
                    letterSpacing = 2.sp,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Prayer Name (e.g. FAJR, ASR)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = nextPrayer.type.englishName,
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = DigitalGreenBright,
                        fontSize = 30.sp,
                        letterSpacing = 3.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = nextPrayer.type.arabicName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = DigitalGreenDim,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Prayer Time (e.g. 04:37 PM)
            Text(
                text = nextPrayer.formattedTime,
                style = MaterialTheme.typography.displaySmall.copy(
                    color = DigitalGreen,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Live Countdown (e.g. 01:42:18)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(AmoledBlack)
                    .border(1.dp, DigitalGreenMuted, RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 3.dp)
            ) {
                Text(
                    text = countdownFormatted,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = DigitalGreenBright,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        letterSpacing = 2.sp
                    )
                )
            }
        }
    }
}

/**
 * Watch navigation tabs with comfortable touch targets.
 * Optimized for 4 tabs: HOME, CAL, QIBLA, SET.
 */
@Composable
fun WatchNavBar(
    currentTab: WatchTab,
    onTabSelected: (WatchTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .testTag("watch_nav_bar"),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        WatchTab.entries.forEach { tab ->
            val isSelected = tab == currentTab
            val label = when (tab) {
                WatchTab.WATCH -> "HOME"
                WatchTab.CALENDAR -> "CAL"
                WatchTab.QIBLA -> "QIBLA"
                WatchTab.SETTINGS -> "SET"
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) DigitalGreenDark else Color.Transparent)
                    .border(
                        if (isSelected) 1.5.dp else 0.dp,
                        if (isSelected) DigitalGreenBright else Color.Transparent,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 9.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = if (isSelected) DigitalGreenBright else DigitalGreenMuted,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }
    }
}


/**
 * Final reference-style HOME composition.
 * Kept intentionally compact for the 320x386 target display.
 */
@Composable
fun ClassicReferenceHome(
    uiState: com.example.alfajr.ui.watchface.WatchfaceUiState,
    modifier: Modifier = Modifier
) {
    val schedule = uiState.prayerSchedule
    val visiblePrayers = listOf(
        com.example.alfajr.data.prayer.PrayerType.FAJR,
        com.example.alfajr.data.prayer.PrayerType.DHUHR,
        com.example.alfajr.data.prayer.PrayerType.ASR,
        com.example.alfajr.data.prayer.PrayerType.MAGHRIB,
        com.example.alfajr.data.prayer.PrayerType.ISHA
    )
    val byType = schedule.items.associateBy { it.type }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(1.dp))

        // Reference-style date header.
        Text(
            text = "${uiState.gregorian.dayOfWeekName.uppercase()}, " +
                "${uiState.gregorian.monthNameShort.uppercase()} ${uiState.gregorian.day}",
            color = Color.White,
            fontFamily = com.example.ui.theme.DigitalFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            letterSpacing = 0.4.sp
        )

        Text(
            text = "${uiState.hijri.day} ${uiState.hijri.monthShortEnglish.uppercase()} ${uiState.hijri.year} AH",
            color = Color.White.copy(alpha = 0.92f),
            fontFamily = com.example.ui.theme.DigitalFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 9.5.sp,
            letterSpacing = 0.25.sp
        )

        Spacer(modifier = Modifier.height(1.dp))

        // Large white reference-style clock.
        val clock = if (uiState.settings.is24HourFormat) {
            uiState.time.formattedClock
        } else {
            uiState.time.formattedClock12
        }
        Text(
            text = clock,
            color = Color.White,
            fontFamily = com.example.ui.theme.DigitalFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 61.sp,
            lineHeight = 61.sp,
            letterSpacing = (-2.0).sp
        )

        // Five-prayer compact row.
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 1.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top
        ) {
            visiblePrayers.forEach { type ->
                val item = byType[type]
                if (item != null) {
                    ClassicPrayerCell(
                        item = item,
                        highlighted = item.isNext
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Green day/progress accent matching the reference.
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .padding(horizontal = 2.dp)
        ) {
            val y = size.height / 2f
            val start = 0f
            val end = size.width
            drawLine(
                color = Color(0xFF707070),
                start = Offset(start, y),
                end = Offset(end, y),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = Color(0xFF00DFA0),
                start = Offset(start, y),
                end = Offset(size.width * 0.18f, y),
                strokeWidth = 3.dp.toPx()
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Real battery, bottom-centered like the reference.
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (uiState.battery.isCharging) "⚡" else "ϟ",
                color = Color(0xFF00DFA0),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "${uiState.battery.level}%",
                color = Color(0xFF00DFA0),
                fontFamily = com.example.ui.theme.DigitalFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 0.3.sp
            )
        }
    }
}

@Composable
private fun ClassicPrayerCell(
    item: PrayerScheduleItem,
    highlighted: Boolean
) {
    val mainColor = if (highlighted) Color(0xFF00DFA0) else Color.White
    val secondaryColor = if (highlighted) Color(0xFF00DFA0) else Color.White.copy(alpha = 0.88f)

    Column(
        modifier = Modifier.width(58.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when (item.type) {
                com.example.alfajr.data.prayer.PrayerType.FAJR -> "FAJR"
                com.example.alfajr.data.prayer.PrayerType.DHUHR -> "DHUHR"
                com.example.alfajr.data.prayer.PrayerType.ASR -> "ASR"
                com.example.alfajr.data.prayer.PrayerType.MAGHRIB -> "MAGH"
                com.example.alfajr.data.prayer.PrayerType.ISHA -> "ISHA"
                else -> item.type.englishName.take(5)
            },
            color = mainColor,
            fontFamily = com.example.ui.theme.DigitalFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 0.sp
        )

        Spacer(modifier = Modifier.height(1.dp))

        PrayerGlyph(type = item.type, color = secondaryColor)

        Spacer(modifier = Modifier.height(1.dp))

        Text(
            text = if (item.formattedTime12.isNotEmpty()) item.formattedTime12 else item.formattedTime24,
            color = secondaryColor,
            fontFamily = com.example.ui.theme.DigitalFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = (-0.15).sp
        )
    }
}

@Composable
private fun PrayerGlyph(
    type: com.example.alfajr.data.prayer.PrayerType,
    color: Color
) {
    Canvas(
        modifier = Modifier.size(width = 22.dp, height = 13.dp)
    ) {
        val cx = size.width / 2f
        val cy = 6.5.dp.toPx()
        val r = 3.2.dp.toPx()
        val stroke = 1.4.dp.toPx()

        when (type) {
            com.example.alfajr.data.prayer.PrayerType.FAJR -> {
                drawArc(
                    color = color,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(cx - r, cy - r),
                    size = Size(r * 2, r * 2),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(stroke)
                )
                for (i in 0..4) {
                    val x = cx - 7.dp.toPx() + i * 3.5.dp.toPx()
                    drawLine(color, Offset(x, cy + 4.dp.toPx()), Offset(x, cy + 7.dp.toPx()), strokeWidth = stroke)
                }
            }
            com.example.alfajr.data.prayer.PrayerType.DHUHR -> {
                drawCircle(color, r, Offset(cx, cy), style = androidx.compose.ui.graphics.drawscope.Fill)
                for (i in 0 until 8) {
                    val a = Math.toRadians((i * 45).toDouble())
                    val x1 = cx + kotlin.math.cos(a).toFloat() * 5.2.dp.toPx()
                    val y1 = cy + kotlin.math.sin(a).toFloat() * 5.2.dp.toPx()
                    val x2 = cx + kotlin.math.cos(a).toFloat() * 7.2.dp.toPx()
                    val y2 = cy + kotlin.math.sin(a).toFloat() * 7.2.dp.toPx()
                    drawLine(color, Offset(x1, y1), Offset(x2, y2), strokeWidth = stroke)
                }
            }
            com.example.alfajr.data.prayer.PrayerType.ASR -> {
                drawCircle(color, r, Offset(cx, cy), style = androidx.compose.ui.graphics.drawscope.Stroke(stroke))
                drawCircle(color, r * 0.35f, Offset(cx, cy), style = androidx.compose.ui.graphics.drawscope.Fill)
                for (i in 0 until 8) {
                    val a = Math.toRadians((i * 45).toDouble())
                    val x1 = cx + kotlin.math.cos(a).toFloat() * 5.dp.toPx()
                    val y1 = cy + kotlin.math.sin(a).toFloat() * 5.dp.toPx()
                    val x2 = cx + kotlin.math.cos(a).toFloat() * 7.dp.toPx()
                    val y2 = cy + kotlin.math.sin(a).toFloat() * 7.dp.toPx()
                    drawLine(color, Offset(x1, y1), Offset(x2, y2), strokeWidth = stroke)
                }
            }
            com.example.alfajr.data.prayer.PrayerType.MAGHRIB -> {
                drawArc(
                    color = color,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(cx - r, cy - r),
                    size = Size(r * 2, r * 2),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(stroke)
                )
                drawLine(color, Offset(cx - 7.dp.toPx(), cy + 4.dp.toPx()), Offset(cx + 7.dp.toPx(), cy + 4.dp.toPx()), strokeWidth = stroke)
            }
            com.example.alfajr.data.prayer.PrayerType.ISHA -> {
                drawArc(
                    color = color,
                    startAngle = 55f,
                    sweepAngle = 250f,
                    useCenter = false,
                    topLeft = Offset(cx - 4.5.dp.toPx(), cy - 4.5.dp.toPx()),
                    size = Size(9.dp.toPx(), 9.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx())
                )
            }
            else -> Unit
        }
    }
}
