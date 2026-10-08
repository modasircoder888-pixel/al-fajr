package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// High-contrast, bold classic Islamic digital watch typography for 320x386 display
val DigitalFontFamily = FontFamily.Monospace

val Typography = Typography(
    // Very large digital clock HH:MM (primary focal element)
    displayLarge = TextStyle(
        fontFamily = DigitalFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 52.sp,
        lineHeight = 54.sp,
        letterSpacing = 1.sp
    ),
    // Next prayer name (e.g. FAJR, ASR)
    displayMedium = TextStyle(
        fontFamily = DigitalFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 30.sp,
        lineHeight = 32.sp,
        letterSpacing = 2.5.sp
    ),
    // Prayer time (e.g. 04:37 PM)
    displaySmall = TextStyle(
        fontFamily = DigitalFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        lineHeight = 26.sp,
        letterSpacing = 1.5.sp
    ),
    // Live countdown (e.g. 01:42:18)
    headlineMedium = TextStyle(
        fontFamily = DigitalFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 19.sp,
        lineHeight = 22.sp,
        letterSpacing = 2.sp
    ),
    // Section headers & primary titles (e.g. NEXT PRAYER)
    titleLarge = TextStyle(
        fontFamily = DigitalFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 16.sp,
        lineHeight = 18.sp,
        letterSpacing = 1.5.sp
    ),
    // Clock seconds & primary values
    titleMedium = TextStyle(
        fontFamily = DigitalFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 15.sp,
        lineHeight = 17.sp,
        letterSpacing = 1.sp
    ),
    // Secondary section headers
    titleSmall = TextStyle(
        fontFamily = DigitalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 15.sp,
        letterSpacing = 1.2.sp
    ),
    // Dates & main content (e.g. 07 OCT 2026, 25 RABI II 1448)
    bodyLarge = TextStyle(
        fontFamily = DigitalFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.8.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = DigitalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.5.sp
    ),
    bodySmall = TextStyle(
        fontFamily = DigitalFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.3.sp
    ),
    // Battery, buttons, navigation items
    labelLarge = TextStyle(
        fontFamily = DigitalFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 13.sp,
        lineHeight = 15.sp,
        letterSpacing = 1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = DigitalFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = DigitalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.5.sp
    )
)
