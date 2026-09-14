package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.notification.AlarmScheduler
import com.example.core.security.EncryptedPreferencesManager
import com.example.domain.model.CalendarEvent
import com.example.domain.repository.NeptunRepository
import com.example.domain.usecase.GetTodayClassesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Calendar
import android.os.LocaleList
import com.example.core.locale.AppLocale
import java.util.Locale

data class WeekDayInfo(
    val dayOfWeek: Int, // 1 = Monday .. 7 = Sunday
    val dayName: String,
    val dateFormatted: String,
    val isoDate: String,
    val isToday: Boolean
)

data class TimetableUiState(
    val events: List<CalendarEvent> = emptyList(),
    val selectedDayOfWeek: Int = currentOrNextSchoolDay(),
    val isWeekView: Boolean = false,
    val selectedWeekOffset: Int = 0,
    val weekLabel: String = "",
    val weekDays: List<WeekDayInfo> = emptyList(),
    val ongoingEvent: CalendarEvent? = null,
    val nextUpcomingEvent: CalendarEvent? = null,
    val isRefreshing: Boolean = false,
    val notificationScheduledId: String? = null,
    val scheduledReminderMins: Int = 15,
    val showWeekend: Boolean = false
)

private fun currentOrNextSchoolDay(): Int {
    val dayOfWeek = LocalDate.now().dayOfWeek
    return when (dayOfWeek) {
        DayOfWeek.MONDAY -> 1
        DayOfWeek.TUESDAY -> 2
        DayOfWeek.WEDNESDAY -> 3
        DayOfWeek.THURSDAY -> 4
        DayOfWeek.FRIDAY -> 5
        DayOfWeek.SATURDAY, DayOfWeek.SUNDAY -> 1
    }
}

class TimetableViewModel(
    private val neptunRepository: NeptunRepository,
    private val alarmScheduler: AlarmScheduler,
    private val prefsManager: EncryptedPreferencesManager? = null
) : ViewModel() {

    private val getTodayClasses = GetTodayClassesUseCase()

    private val _uiState = MutableStateFlow(buildInitialUiState())
    val uiState: StateFlow<TimetableUiState> = _uiState.asStateFlow()

    init {
        observeCalendar()
        observePreferences()
        observeAppLocale()
        refreshCalendar()
    }

    private fun buildInitialUiState(): TimetableUiState {
        val personalization = prefsManager?.loadPersonalization()
        val showWeekend = personalization?.showWeekend ?: false
        val (weekDays, weekLabel) = calculateWeekInfo(0, showWeekend)
        return TimetableUiState(
            selectedDayOfWeek = currentOrNextSchoolDay(),
            selectedWeekOffset = 0,
            weekLabel = weekLabel,
            weekDays = weekDays,
            showWeekend = showWeekend
        )
    }

    private fun observePreferences() {
        viewModelScope.launch {
            prefsManager?.personalizationFlow?.collect { personalization ->
                _uiState.update { state ->
                    val (weekDays, weekLabel) = calculateWeekInfo(state.selectedWeekOffset, personalization.showWeekend)
                    state.copy(
                        showWeekend = personalization.showWeekend,
                        weekDays = weekDays,
                        weekLabel = weekLabel
                    )
                }
            }
        }
    }

    private fun calculateWeekInfo(
        offset: Int,
        includeWeekend: Boolean,
        locale: Locale = Locale.getDefault()
    ): Pair<List<WeekDayInfo>, String> {
        val today = LocalDate.now()
        val isWeekend = today.dayOfWeek == DayOfWeek.SATURDAY || today.dayOfWeek == DayOfWeek.SUNDAY
        val baseMonday = if (isWeekend) {
            today.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
        } else {
            today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        }
        val monday = baseMonday.plusWeeks(offset.toLong())

        val dayCount = if (includeWeekend) 7 else 5
        val days = (0 until dayCount).map { i ->
            val date = monday.plusDays(i.toLong())
            val dateFormatted = date.format(DateTimeFormatter.ofPattern(shortDayPattern(locale), locale))
            val isoDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val isToday = date == today
            WeekDayInfo(
                dayOfWeek = i + 1,
                dayName = DayOfWeek.of(i + 1)
                    .getDisplayName(TextStyle.FULL, locale)
                    .replaceFirstChar { it.uppercase(locale) },
                dateFormatted = dateFormatted,
                isoDate = isoDate,
                isToday = isToday
            )
        }

        val lastDay = monday.plusDays((dayCount - 1).toLong())
        return days to formatWeekLabel(monday, lastDay, locale)
    }

    /**
     * Napkártyákon használt rövid dátum: nyelvenként a megszokott sorrenddel.
     */
    private fun shortDayPattern(locale: Locale): String = when (locale.language) {
        "hu" -> "MM.dd"
        "de" -> "d.M."
        else -> "M/d"
    }

    /**
     * Hét-tartomány felirat az app nyelvén ("2026. szeptember 14. – 18." /
     * "September 14 – 18, 2026" / "14. – 18. September 2026").
     */
    private fun formatWeekLabel(monday: LocalDate, lastDay: LocalDate, locale: Locale): String {
        fun fmt(date: LocalDate, pattern: String): String =
            date.format(DateTimeFormatter.ofPattern(pattern, locale))
        return if (monday.month == lastDay.month) {
            when (locale.language) {
                "hu" -> "${fmt(monday, "yyyy. MMMM d.")} – ${fmt(lastDay, "d.")}"
                "de" -> "${fmt(monday, "d.")} – ${fmt(lastDay, "d. MMMM yyyy")}"
                else -> "${fmt(monday, "MMMM d")} – ${fmt(lastDay, "d, yyyy")}"
            }
        } else {
            when (locale.language) {
                "hu" -> "${fmt(monday, "yyyy. MMM d.")} – ${fmt(lastDay, "MMM d.")}"
                "de" -> "${fmt(monday, "d. MMMM")} – ${fmt(lastDay, "d. MMMM yyyy")}"
                else -> "${fmt(monday, "MMMM d")} – ${fmt(lastDay, "MMMM d, yyyy")}"
            }
        }
    }

    /**
     * Az app nyelvének futásidejű váltásakor (a ViewModel túléli az Activity
     * újralétrehozását) újraszámolja a honosított napneveket és hétfeliratot.
     */
    private fun observeAppLocale() {
        val flow = prefsManager?.appLocaleFlow ?: return
        var first = true
        viewModelScope.launch {
            flow.collect { appLocale ->
                // Az első (aktuális) értékre nincs teendő: az init állapot már kész.
                if (first) {
                    first = false
                    return@collect
                }
                val locale = localeFor(appLocale)
                _uiState.update { state ->
                    val (weekDays, weekLabel) = calculateWeekInfo(state.selectedWeekOffset, state.showWeekend, locale)
                    state.copy(weekDays = weekDays, weekLabel = weekLabel)
                }
            }
        }
    }

    /**
     * A flow hamarabb jelez, mint hogy az Activity újrakészülne (és ezzel a
     * [Locale.getDefault] frissülne), ezért a nyelvet az emitált értékből
     * oldjuk fel determinisztikusan, nem a még elavult alapértelmezettből.
     */
    private fun localeFor(appLocale: AppLocale): Locale {
        appLocale.languageTag?.let { return Locale.forLanguageTag(it) }
        return try {
            LocaleList.getDefault().get(0) ?: Locale.getDefault()
        } catch (e: Exception) {
            Locale.getDefault()
        }
    }

    private fun observeCalendar() {
        viewModelScope.launch {
            neptunRepository.getCalendarEvents().collect { events ->
                updateOngoingAndUpcoming(events)
            }
        }
    }

    private fun updateOngoingAndUpcoming(events: List<CalendarEvent>) {
        val now = Calendar.getInstance()
        val currentDayOfWeek = when (now.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)
        val currentTotalMinutes = currentHour * 60 + currentMinute

        val todayIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val todayEvents = events.filter {
            it.isActualAttendedClass &&
                (if (it.dateString.isNotBlank()) it.dateString.startsWith(todayIso) else it.dayOfWeek == currentDayOfWeek)
        }

        val ongoing = todayEvents.firstOrNull { event ->
            val startTotal = event.startHour * 60 + event.startMinute
            val endTotal = event.endHour * 60 + event.endMinute
            currentTotalMinutes in startTotal..endTotal
        }

        val upcoming = todayEvents.firstOrNull { event ->
            val startTotal = event.startHour * 60 + event.startMinute
            startTotal > currentTotalMinutes
        }

        _uiState.update {
            it.copy(
                events = events,
                ongoingEvent = ongoing,
                nextUpcomingEvent = upcoming
            )
        }
    }

    fun selectDay(dayOfWeek: Int) {
        _uiState.update { it.copy(selectedDayOfWeek = dayOfWeek) }
    }

    fun previousWeek() {
        _uiState.update { state ->
            val newOffset = state.selectedWeekOffset - 1
            val (weekDays, weekLabel) = calculateWeekInfo(newOffset, state.showWeekend)
            state.copy(
                selectedWeekOffset = newOffset,
                weekDays = weekDays,
                weekLabel = weekLabel
            )
        }
    }

    fun nextWeek() {
        _uiState.update { state ->
            val newOffset = state.selectedWeekOffset + 1
            val (weekDays, weekLabel) = calculateWeekInfo(newOffset, state.showWeekend)
            state.copy(
                selectedWeekOffset = newOffset,
                weekDays = weekDays,
                weekLabel = weekLabel
            )
        }
    }

    fun currentWeek() {
        _uiState.update { state ->
            val (weekDays, weekLabel) = calculateWeekInfo(0, state.showWeekend)
            state.copy(
                selectedWeekOffset = 0,
                weekDays = weekDays,
                weekLabel = weekLabel,
                selectedDayOfWeek = currentOrNextSchoolDay()
            )
        }
    }

    fun toggleWeekView() {
        _uiState.update { it.copy(isWeekView = !it.isWeekView) }
    }

    fun refreshCalendar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            neptunRepository.refreshCalendar()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun scheduleClassReminder(event: CalendarEvent) {
        val reminderMins = prefsManager?.loadNotificationPreferences()?.reminderMinutesBefore ?: 15
        alarmScheduler.scheduleClassAlarm(event, reminderMins)
        _uiState.update { it.copy(notificationScheduledId = event.id, scheduledReminderMins = reminderMins) }
    }

    companion object {
        fun provideFactory(
            neptunRepository: NeptunRepository,
            alarmScheduler: AlarmScheduler,
            prefsManager: EncryptedPreferencesManager? = null
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TimetableViewModel(neptunRepository, alarmScheduler, prefsManager) as T
            }
        }
    }
}
