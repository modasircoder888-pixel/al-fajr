package com.example.alfajr.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alfajr.core.calendar.IslamicHolidayRegistry
import com.example.alfajr.ui.components.WatchNavBar
import com.example.alfajr.ui.watchface.WatchTab
import com.example.alfajr.ui.watchface.WatchfaceUiState
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.DigitalGreen
import com.example.ui.theme.DigitalGreenBright
import com.example.ui.theme.DigitalGreenDark
import com.example.ui.theme.DigitalGreenDim
import com.example.ui.theme.DigitalGreenMuted

@Composable
fun CalendarScreen(
    uiState: WatchfaceUiState,
    onTabSelected: (WatchTab) -> Unit,
    onNextMonth: () -> Unit,
    onPrevMonth: () -> Unit,
    onToggleCalendarMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cv = uiState.calendarView
    val scrollState = rememberScrollState()

    val monthName = if (cv.isHijriView) {
        getHijriMonthName(cv.displayedHijriMonth)
    } else {
        getGregorianMonthName(cv.displayedGregorianMonth)
    }

    val yearText = if (cv.isHijriView) "${cv.displayedHijriYear} AH" else "${cv.displayedGregorianYear}"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("calendar_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation Bar
            WatchNavBar(
                currentTab = uiState.currentTab,
                onTabSelected = onTabSelected
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Calendar mode toggle (GREGORIAN vs HIJRI) - Larger touch area & bold text
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(DigitalGreenDark)
                    .border(1.dp, DigitalGreenMuted, RoundedCornerShape(6.dp))
                    .padding(2.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (!cv.isHijriView) DigitalGreen else DigitalGreenDark)
                        .clickable { if (cv.isHijriView) onToggleCalendarMode() }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "GREGORIAN",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = if (!cv.isHijriView) AmoledBlack else DigitalGreenDim,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (cv.isHijriView) DigitalGreen else DigitalGreenDark)
                        .clickable { if (!cv.isHijriView) onToggleCalendarMode() }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "HIJRI",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = if (cv.isHijriView) AmoledBlack else DigitalGreenDim,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Month navigation banner (< MONTH YEAR >) with large 36dp touch targets
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(DigitalGreenDark)
                    .border(1.dp, DigitalGreenMuted, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AmoledBlack)
                        .border(1.dp, DigitalGreenMuted, RoundedCornerShape(6.dp))
                        .clickable { onPrevMonth() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "◀",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = DigitalGreenBright,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = monthName.uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = DigitalGreenBright,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    )
                    Text(
                        text = yearText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DigitalGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AmoledBlack)
                        .border(1.dp, DigitalGreenMuted, RoundedCornerShape(6.dp))
                        .clickable { onNextMonth() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = DigitalGreenBright,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Today's Date readout banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(DigitalGreenDark)
                    .border(0.8.dp, DigitalGreenMuted, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TODAY: ${uiState.gregorianDate.day} ${uiState.gregorianDate.monthNameShort.uppercase()} • ${uiState.hijriDate.day} ${uiState.hijriDate.monthShortEnglish.uppercase()}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = DigitalGreenBright,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Islamic Holidays & Events for this month
            val holidays = if (cv.isHijriView) {
                IslamicHolidayRegistry.getHolidaysInMonth(cv.displayedHijriMonth)
            } else {
                IslamicHolidayRegistry.HOLIDAYS.take(4)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(DigitalGreenDark)
                    .border(1.dp, DigitalGreenMuted, RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "ISLAMIC OCCASIONS",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = DigitalGreenDim,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (holidays.isEmpty()) {
                    Text(
                        text = "No major occasions recorded this month",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DigitalGreenMuted,
                            fontSize = 11.sp
                        )
                    )
                } else {
                    holidays.forEach { hol ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• ${hol.nameEnglish}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = DigitalGreen,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                )
                            )
                            Text(
                                text = "${hol.hijriDay} ${getHijriMonthShort(hol.hijriMonth)}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = DigitalGreenBright,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

private fun getGregorianMonthName(m: Int): String = when (m) {
    1 -> "January"; 2 -> "February"; 3 -> "March"; 4 -> "April"
    5 -> "May"; 6 -> "June"; 7 -> "July"; 8 -> "August"
    9 -> "September"; 10 -> "October"; 11 -> "November"; 12 -> "December"
    else -> "Month $m"
}

private fun getHijriMonthName(m: Int): String = when (m) {
    1 -> "Muharram"; 2 -> "Safar"; 3 -> "Rabi' al-Awwal"; 4 -> "Rabi' al-Thani"
    5 -> "Jumada al-Ula"; 6 -> "Jumada al-Akhirah"; 7 -> "Rajab"; 8 -> "Sha'ban"
    9 -> "Ramadan"; 10 -> "Shawwal"; 11 -> "Dhu al-Qi'dah"; 12 -> "Dhu al-Hijjah"
    else -> "Month $m"
}

private fun getHijriMonthShort(m: Int): String = when (m) {
    1 -> "MUH"; 2 -> "SAF"; 3 -> "RAB I"; 4 -> "RAB II"
    5 -> "JUM I"; 6 -> "JUM II"; 7 -> "RAJ"; 8 -> "SHA"
    9 -> "RAM"; 10 -> "SHAW"; 11 -> "DHU-Q"; 12 -> "DHU-H"
    else -> "M$m"
}
