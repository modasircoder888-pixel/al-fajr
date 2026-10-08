package com.example.alfajr.ui.settings

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
import androidx.compose.foundation.layout.width
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
import com.example.alfajr.data.prayer.AsrMethod
import com.example.alfajr.data.prayer.CalculationMethod
import com.example.alfajr.data.prayer.HighLatitudeRule
import com.example.alfajr.data.prayer.PrayerCoordinates
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
fun SettingsScreen(
    uiState: WatchfaceUiState,
    onTabSelected: (WatchTab) -> Unit,
    onToggleTimeFormat: () -> Unit,
    onSetHijriAdjustment: (Int) -> Unit,
    onSetCalculationMethod: (CalculationMethod) -> Unit,
    onSetAsrMethod: (AsrMethod) -> Unit,
    onSetHighLatitudeRule: (HighLatitudeRule) -> Unit,
    onSetCoordinates: (PrayerCoordinates) -> Unit,
    modifier: Modifier = Modifier
) {
    val settings = uiState.settings
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("settings_screen")
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

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "SETTINGS • الإعدادات",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = DigitalGreenDim,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 1. TIME FORMAT SECTION (Large bold toggle)
            SettingsCard(title = "TIME FORMAT") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (settings.is24HourFormat) "24-HOUR" else "12-HOUR (AM/PM)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = DigitalGreenBright,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DigitalGreen)
                            .clickable { onToggleTimeFormat() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "TOGGLE",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = AmoledBlack,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 2. CALENDAR HIJRI ADJUSTMENT SECTION
            SettingsCard(title = "HIJRI DATE ADJUSTMENT") {
                Column {
                    Text(
                        text = "OFFSET: ${if (settings.hijriAdjustment > 0) "+${settings.hijriAdjustment}" else "${settings.hijriAdjustment}"} DAY(S)",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = DigitalGreenBright,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(-2, -1, 0, 1, 2).forEach { offset ->
                            val isSelected = settings.hijriAdjustment == offset
                            Box(
                                modifier = Modifier
                                    .size(width = 38.dp, height = 32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) DigitalGreen else AmoledBlack)
                                    .border(1.dp, DigitalGreenMuted, RoundedCornerShape(6.dp))
                                    .clickable { onSetHijriAdjustment(offset) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (offset > 0) "+$offset" else "$offset",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = if (isSelected) AmoledBlack else DigitalGreenDim,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 3. PRAYER CALCULATION METHOD
            SettingsCard(title = "CALCULATION METHOD") {
                Column {
                    Text(
                        text = settings.calculationMethod.title.uppercase(),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = DigitalGreenBright,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val methods = listOf(
                            CalculationMethod.UMM_AL_QURA,
                            CalculationMethod.MUSLIM_WORLD_LEAGUE,
                            CalculationMethod.EGYPTIAN,
                            CalculationMethod.ISNA
                        )
                        methods.forEach { m ->
                            val isSelected = settings.calculationMethod == m
                            val shortLabel = when (m) {
                                CalculationMethod.UMM_AL_QURA -> "UMM"
                                CalculationMethod.MUSLIM_WORLD_LEAGUE -> "MWL"
                                CalculationMethod.EGYPTIAN -> "EGY"
                                CalculationMethod.ISNA -> "ISNA"
                                else -> m.name.take(3)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) DigitalGreen else AmoledBlack)
                                    .border(1.dp, DigitalGreenMuted, RoundedCornerShape(6.dp))
                                    .clickable { onSetCalculationMethod(m) }
                                    .padding(horizontal = 8.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = shortLabel,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (isSelected) AmoledBlack else DigitalGreenDim,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 4. ASR JURISPRUDENCE METHOD
            SettingsCard(title = "ASR SHADOW RATIO") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val isStandard = settings.asrMethod == AsrMethod.STANDARD
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isStandard) DigitalGreen else AmoledBlack)
                            .border(1.dp, DigitalGreenMuted, RoundedCornerShape(6.dp))
                            .clickable { onSetAsrMethod(AsrMethod.STANDARD) }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "STANDARD (1X)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isStandard) AmoledBlack else DigitalGreenDim,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (!isStandard) DigitalGreen else AmoledBlack)
                            .border(1.dp, DigitalGreenMuted, RoundedCornerShape(6.dp))
                            .clickable { onSetAsrMethod(AsrMethod.HANAFI) }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "HANAFI (2X)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (!isStandard) AmoledBlack else DigitalGreenDim,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 5. HIGH LATITUDE RULE
            SettingsCard(title = "HIGH LATITUDE RULE") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val rules = listOf(
                        Pair(HighLatitudeRule.ANGLE_BASED, "ANGLE"),
                        Pair(HighLatitudeRule.MIDDLE_OF_NIGHT, "MIDNIGHT"),
                        Pair(HighLatitudeRule.ONE_SEVENTH, "1/7TH")
                    )
                    rules.forEach { (r, label) ->
                        val isSelected = settings.highLatitudeRule == r
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) DigitalGreen else AmoledBlack)
                                .border(1.dp, DigitalGreenMuted, RoundedCornerShape(6.dp))
                                .clickable { onSetHighLatitudeRule(r) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) AmoledBlack else DigitalGreenDim,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 9.sp
                                )
                            )
                        }
                        if (r != rules.last().first) {
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 6. LOCATION PRESET SECTION
            SettingsCard(title = "CITY LOCATION") {
                Column {
                    Text(
                        text = "${settings.coordinates.locationName.uppercase()} (${String.format("%.2f", settings.coordinates.latitude)}°N, ${String.format("%.2f", settings.coordinates.longitude)}°E)",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = DigitalGreenBright,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val cityPresets = listOf(
                            PrayerCoordinates.MAKKAH,
                            PrayerCoordinates.MADINAH,
                            PrayerCoordinates.CAIRO,
                            PrayerCoordinates.DUBAI
                        )
                        cityPresets.forEach { preset ->
                            val isSelected = settings.coordinates.locationName == preset.locationName
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) DigitalGreen else AmoledBlack)
                                    .border(1.dp, DigitalGreenMuted, RoundedCornerShape(6.dp))
                                    .clickable { onSetCoordinates(preset) }
                                    .padding(horizontal = 7.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = preset.locationName.take(4).uppercase(),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (isSelected) AmoledBlack else DigitalGreenDim,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 7. SYSTEM / ABOUT SECTION
            SettingsCard(title = "ABOUT AL FAJR") {
                Column {
                    Text(
                        text = "AL FAJR SMARTWATCH OS • V1.0.0",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = DigitalGreenBright,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "TARGET: PGD 320x386 • ANDROID 8.1",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DigitalGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = "OFFLINE ASTRONOMICAL ISLAMIC ENGINE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = DigitalGreenDim,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DigitalGreenDark)
            .border(1.dp, DigitalGreenMuted, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                color = DigitalGreenDim,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp,
                fontSize = 12.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        content()
    }
}
