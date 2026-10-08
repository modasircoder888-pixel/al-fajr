package com.example

import com.example.alfajr.core.background.BackgroundAsset
import com.example.alfajr.core.background.BackgroundThemeId
import com.example.alfajr.core.background.DefaultBackgroundProvider
import com.example.alfajr.core.calendar.GregorianDate
import com.example.alfajr.core.calendar.HijriCalendarEngine
import com.example.alfajr.core.calendar.HijriDate
import com.example.alfajr.core.calendar.IslamicHolidayRegistry
import com.example.alfajr.core.qibla.QiblaEngine
import com.example.alfajr.data.prayer.AstronomicalPrayerCalculator
import com.example.alfajr.data.prayer.AstronomicalPrayerRepository
import com.example.alfajr.data.prayer.CalculationMethod
import com.example.alfajr.data.prayer.PrayerCalculationSettings
import com.example.alfajr.data.prayer.PrayerCoordinates
import com.example.alfajr.data.prayer.PrayerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class ExampleUnitTest {

  @Test
  fun testHijriEngineCalculatesExpectedDates() {
    val engine = HijriCalendarEngine(adjustmentDays = 0)
    // 7 October 2026 -> 25 or 26 Rabi' al-Thani 1448
    val greg = GregorianDate(year = 2026, month = 10, day = 7, dayOfWeek = Calendar.WEDNESDAY)
    val hijri = engine.toHijri(greg)

    assertEquals(1448, hijri.year)
    assertEquals(4, hijri.month) // Rabi' al-Thani is 4th month
    assertTrue("Day should be between 24 and 27", hijri.day in 24..27)
    assertEquals("Rabi' al-Thani", hijri.monthNameEnglish)
  }

  @Test
  fun testHijriAdjustmentOffset() {
    val engine = HijriCalendarEngine(adjustmentDays = 0)
    val greg = GregorianDate(year = 2026, month = 10, day = 7, dayOfWeek = Calendar.WEDNESDAY)
    val baseHijri = engine.toHijri(greg)

    val enginePlus1 = HijriCalendarEngine(adjustmentDays = 1)
    val hijriPlus1 = enginePlus1.toHijri(greg)

    assertEquals(baseHijri.day + 1, hijriPlus1.day)
  }

  @Test
  fun testBidirectionalHijriConversion() {
    val engine = HijriCalendarEngine(adjustmentDays = 0)
    val gregOriginal = GregorianDate(year = 2026, month = 10, day = 7, dayOfWeek = Calendar.WEDNESDAY)
    val hijri = engine.toHijri(gregOriginal)
    val convertedBack = engine.toGregorian(hijri)

    assertEquals(gregOriginal.year, convertedBack.year)
    assertEquals(gregOriginal.month, convertedBack.month)
    assertEquals(gregOriginal.day, convertedBack.day)
  }

  @Test
  fun testIslamicHolidayDetection() {
    val engine = HijriCalendarEngine()
    val eidDay = engine.today().copy(month = 10, day = 1) // 1 Shawwal = Eid al-Fitr
    val holiday = IslamicHolidayRegistry.getHoliday(eidDay)

    assertNotNull(holiday)
    assertEquals("Eid al-Fitr", holiday?.nameEnglish)

    val ashuraDay = HijriDate(1448, 1, 10, Calendar.FRIDAY)
    val ashura = IslamicHolidayRegistry.getHoliday(ashuraDay)
    assertNotNull(ashura)
    assertEquals("Day of Ashura", ashura?.nameEnglish)
  }

  @Test
  fun testAstronomicalPrayerCalculation() {
    val settings = PrayerCalculationSettings(
        coordinates = PrayerCoordinates.MAKKAH,
        calculationMethod = CalculationMethod.UMM_AL_QURA
    )

    val times = AstronomicalPrayerCalculator.calculateTimes(
        year = 2026,
        month = 10,
        day = 7,
        settings = settings,
        timeZone = TimeZone.getTimeZone("Asia/Riyadh")
    )

    assertNotNull(times[PrayerType.FAJR])
    assertNotNull(times[PrayerType.DHUHR])
    assertNotNull(times[PrayerType.ASR])
    assertNotNull(times[PrayerType.MAGHRIB])
    assertNotNull(times[PrayerType.ISHA])

    // Verify chronological order of prayer times
    val fajr = times[PrayerType.FAJR]!!
    val sunrise = times[PrayerType.SUNRISE]!!
    val dhuhr = times[PrayerType.DHUHR]!!
    val asr = times[PrayerType.ASR]!!
    val maghrib = times[PrayerType.MAGHRIB]!!
    val isha = times[PrayerType.ISHA]!!

    assertTrue(fajr < sunrise)
    assertTrue(sunrise < dhuhr)
    assertTrue(dhuhr < asr)
    assertTrue(asr < maghrib)
    assertTrue(maghrib < isha)
  }

  @Test
  fun testNextPrayerCountdownFormatting() {
    val repo = AstronomicalPrayerRepository()
    val cal = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, 9)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
    }
    val snapshot = repo.getPrayerSchedule(cal)

    assertEquals(6, snapshot.items.size)
    assertNotNull(snapshot.nextPrayer)
    // Next prayer is Dhuhr (at midday)
    assertEquals("DHUHR", snapshot.nextPrayer?.type?.englishName)
    // Countdown format must match HH:MM:SS (e.g. 03:00:00)
    assertTrue(snapshot.countdownFormatted.matches(Regex("\\d{2}:\\d{2}:\\d{2}")))
  }

  @Test
  fun testQiblaGeodesicCalculation() {
    // Cairo coordinates: 30.0444° N, 31.2357° E
    // Bearing to Makkah (21.4225° N, 39.8262° E) is approx 135°-137° SE
    val bearing = QiblaEngine.calculateQiblaBearing(30.0444, 31.2357)
    assertTrue("Bearing from Cairo to Makkah should be around 135°-137°", bearing in 134f..138f)

    val distance = QiblaEngine.calculateDistanceToKaaba(30.0444, 31.2357)
    assertTrue("Distance from Cairo to Makkah approx 1280 km", distance in 1200..1400)
  }

  @Test
  fun testClassicBlackBackgroundTheme() {
    val provider = DefaultBackgroundProvider()
    val defaultTheme = provider.getDefaultTheme()
    assertNotNull(defaultTheme)
    assertEquals(BackgroundThemeId.CLASSIC_BLACK, defaultTheme.id)
    assertTrue(defaultTheme.asset is BackgroundAsset.PureColor)
    assertEquals(0xFF000000.toLong(), (defaultTheme.asset as BackgroundAsset.PureColor).colorHex)
  }

  @Test
  fun testAfterIshaTransitionsToTomorrowFajr() {
    val repo = AstronomicalPrayerRepository()
    val cal = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, 23) // 11:00 PM - past Isha
      set(Calendar.MINUTE, 30)
      set(Calendar.SECOND, 0)
    }
    val snapshot = repo.getPrayerSchedule(cal)

    assertNotNull(snapshot.nextPrayer)
    // After Isha, next prayer MUST be Fajr
    assertEquals("FAJR", snapshot.nextPrayer?.type?.englishName)
    assertEquals(true, snapshot.nextPrayer?.isNext)
    // Timestamp must be in the future (tomorrow)
    assertTrue(snapshot.nextPrayer!!.timestampMs > cal.timeInMillis)
    assertTrue(snapshot.countdownSeconds > 0)
    assertTrue(snapshot.countdownFormatted.matches(Regex("\\d{2}:\\d{2}:\\d{2}")))
  }

  @Test
  fun testQiblaManualAdjustmentAndCardinalDirections() {
    val bearing = QiblaEngine.calculateQiblaBearing(30.0444, 31.2357) // Cairo
    val cardinal = QiblaEngine.getCardinalDirection(bearing)
    assertEquals("SOUTH-EAST", cardinal)

    val engine = com.example.alfajr.core.qibla.QiblaEngine(androidx.test.core.app.ApplicationProvider.getApplicationContext())
    engine.updateLocation(30.0444, 31.2357)
    assertEquals(bearing, engine.qiblaState.value.qiblaBearing, 0.5f)

    engine.rotateManual(20f)
    assertEquals(true, engine.qiblaState.value.isManualMode)
    assertEquals(20f, engine.qiblaState.value.compassHeading, 0.1f)

    engine.resetToSensors()
    assertEquals(false, engine.qiblaState.value.isManualMode)
  }
}
