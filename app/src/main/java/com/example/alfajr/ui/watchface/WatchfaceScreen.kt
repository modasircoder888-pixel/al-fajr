package com.example.alfajr.ui.watchface

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.alfajr.ui.components.ClassicReferenceHome
import com.example.ui.theme.AmoledBlack

/**
 * Al Fajr HOME screen.
 *
 * Visual direction follows the user's supplied reference:
 * dark mosque/crescent night background, large white digital clock,
 * Gregorian/Hijri date, all five prayer times, active-prayer highlight,
 * green progress accent, and battery at the bottom.
 *
 * Navigation remains gesture-based so the HOME screen stays visually clean.
 */
@Composable
fun WatchfaceScreen(
    uiState: WatchfaceUiState,
    onTabSelected: (WatchTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .testTag("watchface_main_screen")
    ) {
        Image(
            painter = painterResource(id = com.example.R.drawable.alfajr_night_mosque),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
            alpha = 0.96f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 7.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ClassicReferenceHome(
                uiState = uiState,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(1.dp))
        }
    }
}
