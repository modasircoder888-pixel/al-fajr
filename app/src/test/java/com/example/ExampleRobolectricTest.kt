package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.alfajr.ui.watchface.WatchTab
import com.example.alfajr.ui.watchface.WatchfaceViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read app_name string resource matches Al Fajr`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Al Fajr", appName)
  }

  @Test
  fun `verify WatchfaceViewModel creates via AndroidViewModelFactory and initializes all tabs`() {
    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    val factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    val viewModel = factory.create(WatchfaceViewModel::class.java)

    assertNotNull(viewModel)
    val state = viewModel.uiState.value
    assertNotNull(state)
    assertEquals(WatchTab.WATCH, state.currentTab)
    assertNotNull(state.prayerSchedule.nextPrayer)
    assertNotNull(state.qibla)
    assertNotNull(state.settings)

    // Test tab selection across all 4 tabs
    viewModel.selectTab(WatchTab.CALENDAR)
    assertEquals(WatchTab.CALENDAR, viewModel.uiState.value.currentTab)

    viewModel.selectTab(WatchTab.QIBLA)
    assertEquals(WatchTab.QIBLA, viewModel.uiState.value.currentTab)

    viewModel.selectTab(WatchTab.SETTINGS)
    assertEquals(WatchTab.SETTINGS, viewModel.uiState.value.currentTab)

    viewModel.selectTab(WatchTab.WATCH)
    assertEquals(WatchTab.WATCH, viewModel.uiState.value.currentTab)
  }

  @Test
  fun `verify Qibla manual rotation and reset`() {
    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    val factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    val viewModel = factory.create(WatchfaceViewModel::class.java)

    val initialHeading = viewModel.uiState.value.qibla.compassHeading
    viewModel.rotateQibla(15f)
    assertEquals((initialHeading + 15f) % 360f, viewModel.uiState.value.qibla.compassHeading, 0.1f)
    assertTrue(viewModel.uiState.value.qibla.isManualMode)

    viewModel.resetQiblaSensor()
    assertEquals(false, viewModel.uiState.value.qibla.isManualMode)
  }

  @Test
  fun `verify settings persistence and prayer recalculation`() {
    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    val factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    val viewModel = factory.create(WatchfaceViewModel::class.java)

    // Test time format toggle
    val originalFormat = viewModel.uiState.value.settings.is24HourFormat
    viewModel.toggleTimeFormat()
    assertEquals(!originalFormat, viewModel.uiState.value.settings.is24HourFormat)

    // Test Hijri offset adjustment
    viewModel.setHijriAdjustment(1)
    assertEquals(1, viewModel.uiState.value.settings.hijriAdjustment)

    // Test location coordinates change
    viewModel.setCoordinates(com.example.alfajr.data.prayer.PrayerCoordinates.DUBAI)
    assertEquals("Dubai", viewModel.uiState.value.settings.coordinates.locationName)
    assertEquals(25.2048, viewModel.uiState.value.settings.coordinates.latitude, 0.001)

    // Next prayer should still be validly calculated
    assertNotNull(viewModel.uiState.value.prayerSchedule.nextPrayer)
  }
}
