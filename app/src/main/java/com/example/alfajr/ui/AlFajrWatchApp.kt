package com.example.alfajr.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.alfajr.ui.calendar.CalendarScreen
import com.example.alfajr.ui.qibla.QiblaScreen
import com.example.alfajr.ui.settings.SettingsScreen
import com.example.alfajr.ui.watchface.WatchTab
import com.example.alfajr.ui.watchface.WatchfaceScreen
import com.example.alfajr.ui.watchface.WatchfaceViewModel

/**
 * Root container for the Al Fajr Smartwatch interface.
 * Implements swipe-left/swipe-right navigation and tab switching across:
 * WATCH (HOME) | CALENDAR | QIBLA | SETTINGS
 * Handles hardware/system back button to always return to the main watchface.
 */
@Composable
fun AlFajrWatchApp(
    viewModel: WatchfaceViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Back handler: any non-watch screen returns directly to the watch face
    BackHandler(enabled = uiState.currentTab != WatchTab.WATCH) {
        viewModel.selectTab(WatchTab.WATCH)
    }

    var totalDrag by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(uiState.currentTab) {
                detectHorizontalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onDragEnd = {
                        val tabs = WatchTab.entries
                        val currentIndex = tabs.indexOf(uiState.currentTab)
                        if (totalDrag < -60f && currentIndex < tabs.size - 1) {
                            // Swiped left -> Next tab
                            viewModel.selectTab(tabs[currentIndex + 1])
                        } else if (totalDrag > 60f && currentIndex > 0) {
                            // Swiped right -> Previous tab
                            viewModel.selectTab(tabs[currentIndex - 1])
                        }
                        totalDrag = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        totalDrag += dragAmount
                    }
                )
            }
    ) {
        Crossfade(targetState = uiState.currentTab, label = "tab_crossfade") { tab ->
            when (tab) {
                WatchTab.WATCH -> WatchfaceScreen(
                    uiState = uiState,
                    onTabSelected = { viewModel.selectTab(it) }
                )

                WatchTab.CALENDAR -> CalendarScreen(
                    uiState = uiState,
                    onTabSelected = { viewModel.selectTab(it) },
                    onNextMonth = { viewModel.nextCalendarMonth() },
                    onPrevMonth = { viewModel.previousCalendarMonth() },
                    onToggleCalendarMode = { viewModel.toggleCalendarMode() }
                )

                WatchTab.QIBLA -> QiblaScreen(
                    uiState = uiState,
                    onTabSelected = { viewModel.selectTab(it) },
                    onRotateManual = { viewModel.rotateQibla(it) },
                    onResetSensor = { viewModel.resetQiblaSensor() }
                )

                WatchTab.SETTINGS -> SettingsScreen(
                    uiState = uiState,
                    onTabSelected = { viewModel.selectTab(it) },
                    onToggleTimeFormat = { viewModel.toggleTimeFormat() },
                    onSetHijriAdjustment = { viewModel.setHijriAdjustment(it) },
                    onSetCalculationMethod = { viewModel.setCalculationMethod(it) },
                    onSetAsrMethod = { viewModel.setAsrMethod(it) },
                    onSetHighLatitudeRule = { viewModel.setHighLatitudeRule(it) },
                    onSetCoordinates = { viewModel.setCoordinates(it) }
                )
            }
        }
    }
}
