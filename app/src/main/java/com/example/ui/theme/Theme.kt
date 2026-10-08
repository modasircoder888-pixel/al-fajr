package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AlFajrDigitalColorScheme = darkColorScheme(
    primary = DigitalGreen,
    onPrimary = AmoledBlack,
    primaryContainer = DigitalGreenDark,
    onPrimaryContainer = DigitalGreenBright,
    secondary = DigitalGreenDim,
    onSecondary = AmoledBlack,
    secondaryContainer = DigitalGreenDark,
    onSecondaryContainer = DigitalGreen,
    tertiary = DigitalGreenBright,
    onTertiary = AmoledBlack,
    background = AmoledBlack,
    onBackground = DigitalGreen,
    surface = DarkLcdBackground,
    onSurface = DigitalGreen,
    surfaceVariant = DigitalGreenDark,
    onSurfaceVariant = DigitalGreenDim,
    outline = DigitalGreenMuted,
    outlineVariant = DigitalGreenMuted
)

@Composable
fun AlFajrTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AlFajrDigitalColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    AlFajrTheme(content = content)
}
