package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.notification.AlarmScheduler
import com.example.core.security.EncryptedPreferencesManager
import com.example.domain.model.CalendarEvent
import com.example.domain.model.WeekFilterMode
import com.example.domain.repository.NeptunRepository
import com.example.domain.usecase.GetTodayClassesUseCase
import com.example.domain.usecase.WeekParityUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Calendar
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
    // Új: A/B hét és megjelenítési beállítások
    val weekFilterMode: WeekFilterMode = WeekFilterMode.ALL,
    val currentWeekType: Int = 0, // 1 = A (páratlan), 2 = B (páros) – a kiválasztott héten
    val showWeekend: Boolean = false
) {
    /** A weekFilter és a hét típusa alapján szűrt események. */
    val filteredEvents: List<CalendarEvent>
        get() = filterEvents(events, weekFilterMode, selectedWeekOffset)

    private fun filterEvents(events: List<CalendarEvent>, filter: WeekFilterMode, weekOffset: Int): List<CalendarEvent> {
        if (filter == WeekFilterMode.ALL) return events
        val weekTypeNow = WeekParityUtil.weekTypeForOffset(weekOffset)
        return events.filter { event ->
            event.weekType == 0 || event.dateString.isNotBlank() || event.weekType == weekTypeNow
        }
    }
}

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
            weekFilterMode = personalization?.let { WeekFilterMode.fromName(it.weekFilterMode) } ?: WeekFilterMode.ALL,
            currentWeekType = WeekParityUtil.weekTypeForOffset(0),
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
                        weekFilterMode = WeekFilterMode.fromName(personalization.weekFilterMode),
                        weekDays = weekDays,
                        weekLabel = weekLabel
                    )
                }
            }
        }
    }

    private fun calculateWeekInfo(offset: Int, includeWeekend: Boolean): Pair<List<WeekDayInfo>, String> {
        val today = LocalDate.now()
        val isWeekend = today.dayOfWeek == DayOfWeek.SATURDAY || today.dayOfWeek == DayOfWeek.SUNDAY
        val baseMonday = if (isWeekend) {
            today.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
        } else {
            today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        }
        val monday = baseMonday.plusWeeks(offset.toLong())
        val dayNames = listOf("Hétfő", "Kedd", "Szerda", "Csütörtök", "Péntek", "Szombat", "Vasárnap")

        val dayCount = if (includeWeekend) 7 else 5
        val days = (0 until dayCount).map { i ->
            val date = monday.plusDays(i.toLong())
            val dateFormatted = date.format(DateTimeFormatter.ofPattern("MM.dd"))
            val isoDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val isToday = date == today
            WeekDayInfo(
                dayOfWeek = i + 1,
                dayName = dayNames[i],
                dateFormatted = dateFormatted,
                isoDate = isoDate,
                isToday = isToday
            )
        }

        val lastDay = monday.plusDays((dayCount - 1).toLong())
        val monthFormatter = DateTimeFormatter.ofPattern("yyyy. MMMM d.", Locale("hu"))
        val endDayFormatter = DateTimeFormatter.ofPattern("d.", Locale("hu"))
        val weekLabel = if (monday.month == lastDay.month) {
            "${monday.format(monthFormatter)} – ${lastDay.format(endDayFormatter)}"
        } else {
            "${monday.format(DateTimeFormatter.ofPattern("yyyy. MMM d.", Locale("hu")))} – ${lastDay.format(DateTimeFormatter.ofPattern("MMM d.", Locale("hu")))}"
        }

        return days to weekLabel
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

    fun setWeekFilter(mode: WeekFilterMode) {
        _uiState.update { it.copy(weekFilterMode = mode) }
        prefsManager?.setWeekFilterMode(mode.name)
    }

    fun previousWeek() {
        _uiState.update { state ->
            val newOffset = state.selectedWeekOffset - 1
            val (weekDays, weekLabel) = calculateWeekInfo(newOffset, state.showWeekend)
            state.copy(
                selectedWeekOffset = newOffset,
                weekDays = weekDays,
                weekLabel = weekLabel,
                currentWeekType = WeekParityUtil.weekTypeForOffset(newOffset)
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
                weekLabel = weekLabel,
                currentWeekType = WeekParityUtil.weekTypeForOffset(newOffset)
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
                currentWeekType = WeekParityUtil.weekTypeForOffset(0),
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
        _uiState.update { it.copy(notificationScheduledId = event.id) }
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
