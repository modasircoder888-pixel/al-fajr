package com.example.alfajr.ui.watchface

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alfajr.core.battery.BatteryInfo
import com.example.alfajr.core.battery.BatteryMonitor
import com.example.alfajr.core.calendar.GregorianDate
import com.example.alfajr.core.calendar.HijriCalendarEngine
import com.example.alfajr.core.calendar.HijriDate
import com.example.alfajr.core.calendar.IslamicHoliday
import com.example.alfajr.core.calendar.IslamicHolidayRegistry
import com.example.alfajr.core.qibla.QiblaEngine
import com.example.alfajr.core.qibla.QiblaState
import com.example.alfajr.core.time.TimeTicker
import com.example.alfajr.core.time.WatchTime
import com.example.alfajr.data.prayer.AstronomicalPrayerRepository
import com.example.alfajr.data.prayer.CalculationMethod
import com.example.alfajr.data.prayer.PrayerCoordinates
import com.example.alfajr.data.prayer.PrayerRepository
import com.example.alfajr.data.prayer.PrayerScheduleSnapshot
import com.example.alfajr.data.settings.AlFajrSettings
import com.example.alfajr.data.settings.AlFajrSettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

enum class WatchTab(val title: String) {
    WATCH("WATCH"),
    CALENDAR("CALENDAR"),
    QIBLA("QIBLA"),
    SETTINGS("SETTINGS")
}

data class CalendarViewState(
    val displayedGregorianMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val displayedGregorianYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val displayedHijriMonth: Int = 1,
    val displayedHijriYear: Int = 1448,
    val isHijriView: Boolean = false,
    val monthHolidays: List<IslamicHoliday> = emptyList()
)

data class WatchfaceUiState(
    val currentTab: WatchTab = WatchTab.WATCH,
    val time: WatchTime = WatchTime.now(),
    val gregorianDate: GregorianDate = GregorianDate.today(),
    val hijriDate: HijriDate = HijriCalendarEngine().today(),
    val holiday: IslamicHoliday? = null,
    val battery: BatteryInfo = BatteryInfo(),
    val prayerSchedule: PrayerScheduleSnapshot = AstronomicalPrayerRepository().getPrayerSchedule(Calendar.getInstance()),
    val settings: AlFajrSettings = AlFajrSettings(),
    val qibla: QiblaState = QiblaState(),
    val calendarView: CalendarViewState = CalendarViewState()
)

class WatchfaceViewModel @JvmOverloads constructor(
    application: Application,
    private val calendarEngine: HijriCalendarEngine = HijriCalendarEngine(),
    private val prayerRepository: PrayerRepository = AstronomicalPrayerRepository(),
    private val settingsManager: AlFajrSettingsManager = AlFajrSettingsManager(application)
) : AndroidViewModel(application) {

    private val batteryMonitor = BatteryMonitor(application)
    private val qiblaEngine = QiblaEngine(application)

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<WatchfaceUiState> = _uiState.asStateFlow()

    private var lastRecordedDay = -1

    init {
        batteryMonitor.startListening()
        qiblaEngine.startListening()

        // Apply persistent settings to engines
        val initSettings = settingsManager.settings.value
        calendarEngine.setAdjustment(initSettings.hijriAdjustment)
        qiblaEngine.updateLocation(initSettings.coordinates.latitude, initSettings.coordinates.longitude)

        // Observe settings changes
        viewModelScope.launch {
            settingsManager.settings.collect { newSettings ->
                calendarEngine.setAdjustment(newSettings.hijriAdjustment)
                qiblaEngine.updateLocation(newSettings.coordinates.latitude, newSettings.coordinates.longitude)

                // Recalculate dates and prayers with new settings
                val currentCal = _uiState.value.time.calendar
                val greg = GregorianDate.fromCalendar(currentCal)
                val hij = calendarEngine.toHijri(greg)
                val hol = IslamicHolidayRegistry.getHoliday(hij)
                val schedule = prayerRepository.getPrayerSchedule(
                    currentCalendar = currentCal,
                    settings = newSettings.toPrayerSettings(),
                    is24HourFormat = newSettings.is24HourFormat
                )

                _uiState.value = _uiState.value.copy(
                    settings = newSettings,
                    hijriDate = hij,
                    holiday = hol,
                    prayerSchedule = schedule
                )
            }
        }

        // Observe battery
        viewModelScope.launch {
            batteryMonitor.batteryState.collect { batteryInfo ->
                _uiState.value = _uiState.value.copy(battery = batteryInfo)
            }
        }

        // Observe Qibla sensor state
        viewModelScope.launch {
            qiblaEngine.qiblaState.collect { qState ->
                _uiState.value = _uiState.value.copy(qibla = qState)
            }
        }

        // Second-accurate clock ticker for clock numerals & next prayer countdown
        viewModelScope.launch {
            TimeTicker.tickerFlow().collect { newTime ->
                val currentDay = newTime.calendar.get(Calendar.DAY_OF_YEAR)

                val (gregorian, hijri, holiday) = if (currentDay != lastRecordedDay) {
                    lastRecordedDay = currentDay
                    val greg = GregorianDate.fromCalendar(newTime.calendar)
                    val hij = calendarEngine.toHijri(greg)
                    val hol = IslamicHolidayRegistry.getHoliday(hij)
                    Triple(greg, hij, hol)
                } else {
                    Triple(
                        _uiState.value.gregorianDate,
                        _uiState.value.hijriDate,
                        _uiState.value.holiday
                    )
                }

                // Recalculate prayer countdown with active 12/24h format setting
                val schedule = prayerRepository.getPrayerSchedule(
                    currentCalendar = newTime.calendar,
                    settings = _uiState.value.settings.toPrayerSettings(),
                    is24HourFormat = _uiState.value.settings.is24HourFormat
                )

                _uiState.value = _uiState.value.copy(
                    time = newTime,
                    gregorianDate = gregorian,
                    hijriDate = hijri,
                    holiday = holiday,
                    prayerSchedule = schedule
                )
            }
        }
    }

    private fun createInitialState(): WatchfaceUiState {
        val now = WatchTime.now()
        lastRecordedDay = now.calendar.get(Calendar.DAY_OF_YEAR)
        val greg = GregorianDate.fromCalendar(now.calendar)
        val hij = calendarEngine.toHijri(greg)
        val hol = IslamicHolidayRegistry.getHoliday(hij)
        val currentSettings = settingsManager.settings.value
        qiblaEngine.updateLocation(currentSettings.coordinates.latitude, currentSettings.coordinates.longitude)

        val schedule = prayerRepository.getPrayerSchedule(
            currentCalendar = now.calendar,
            settings = currentSettings.toPrayerSettings(),
            is24HourFormat = currentSettings.is24HourFormat
        )

        return WatchfaceUiState(
            time = now,
            gregorianDate = greg,
            hijriDate = hij,
            holiday = hol,
            battery = BatteryInfo(),
            prayerSchedule = schedule,
            settings = currentSettings,
            qibla = qiblaEngine.qiblaState.value,
            calendarView = CalendarViewState(
                displayedGregorianMonth = greg.month,
                displayedGregorianYear = greg.year,
                displayedHijriMonth = hij.month,
                displayedHijriYear = hij.year,
                monthHolidays = IslamicHolidayRegistry.getHolidaysInMonth(hij.month)
            )
        )
    }

    fun selectTab(tab: WatchTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun toggleTimeFormat() {
        val newFormat = !_uiState.value.settings.is24HourFormat
        settingsManager.setTimeFormat(newFormat)
    }

    fun setHijriAdjustment(adjustment: Int) {
        settingsManager.setHijriAdjustment(adjustment)
    }

    fun setCalculationMethod(method: CalculationMethod) {
        settingsManager.setCalculationMethod(method)
    }

    fun setAsrMethod(asr: com.example.alfajr.data.prayer.AsrMethod) {
        settingsManager.setAsrMethod(asr)
    }

    fun setHighLatitudeRule(rule: com.example.alfajr.data.prayer.HighLatitudeRule) {
        settingsManager.setHighLatitudeRule(rule)
    }

    fun setCoordinates(coords: PrayerCoordinates) {
        settingsManager.setCoordinates(coords)
    }

    // Qibla heading manual adjustment and sensor controls
    fun rotateQibla(deltaDegrees: Float) {
        qiblaEngine.rotateManual(deltaDegrees)
    }

    fun resetQiblaSensor() {
        qiblaEngine.resetToSensors()
    }

    // Calendar screen navigation
    fun nextCalendarMonth() {
        val cv = _uiState.value.calendarView
        if (cv.isHijriView) {
            val nextM = if (cv.displayedHijriMonth == 12) 1 else cv.displayedHijriMonth + 1
            val nextY = if (cv.displayedHijriMonth == 12) cv.displayedHijriYear + 1 else cv.displayedHijriYear
            _uiState.value = _uiState.value.copy(
                calendarView = cv.copy(
                    displayedHijriMonth = nextM,
                    displayedHijriYear = nextY,
                    monthHolidays = IslamicHolidayRegistry.getHolidaysInMonth(nextM)
                )
            )
        } else {
            val nextM = if (cv.displayedGregorianMonth == 12) 1 else cv.displayedGregorianMonth + 1
            val nextY = if (cv.displayedGregorianMonth == 12) cv.displayedGregorianYear + 1 else cv.displayedGregorianYear
            _uiState.value = _uiState.value.copy(
                calendarView = cv.copy(
                    displayedGregorianMonth = nextM,
                    displayedGregorianYear = nextY
                )
            )
        }
    }

    fun previousCalendarMonth() {
        val cv = _uiState.value.calendarView
        if (cv.isHijriView) {
            val prevM = if (cv.displayedHijriMonth == 1) 12 else cv.displayedHijriMonth - 1
            val prevY = if (cv.displayedHijriMonth == 1) cv.displayedHijriYear - 1 else cv.displayedHijriYear
            _uiState.value = _uiState.value.copy(
                calendarView = cv.copy(
                    displayedHijriMonth = prevM,
                    displayedHijriYear = prevY,
                    monthHolidays = IslamicHolidayRegistry.getHolidaysInMonth(prevM)
                )
            )
        } else {
            val prevM = if (cv.displayedGregorianMonth == 1) 12 else cv.displayedGregorianMonth - 1
            val prevY = if (cv.displayedGregorianMonth == 1) cv.displayedGregorianYear - 1 else cv.displayedGregorianYear
            _uiState.value = _uiState.value.copy(
                calendarView = cv.copy(
                    displayedGregorianMonth = prevM,
                    displayedGregorianYear = prevY
                )
            )
        }
    }

    fun toggleCalendarMode() {
        val cv = _uiState.value.calendarView
        _uiState.value = _uiState.value.copy(
            calendarView = cv.copy(isHijriView = !cv.isHijriView)
        )
    }

    override fun onCleared() {
        super.onCleared()
        batteryMonitor.stopListening()
        qiblaEngine.stopListening()
    }
}
