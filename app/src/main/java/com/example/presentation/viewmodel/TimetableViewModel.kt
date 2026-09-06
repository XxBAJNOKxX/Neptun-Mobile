package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.notification.AlarmScheduler
import com.example.domain.model.CalendarEvent
import com.example.domain.repository.NeptunRepository
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
    val dayOfWeek: Int, // 1 = Monday .. 5 = Friday
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
    val notificationScheduledId: String? = null
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
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(buildInitialUiState())
    val uiState: StateFlow<TimetableUiState> = _uiState.asStateFlow()

    init {
        observeCalendar()
        refreshCalendar()
    }

    private fun buildInitialUiState(): TimetableUiState {
        val (weekDays, weekLabel) = calculateWeekInfo(0)
        return TimetableUiState(
            selectedDayOfWeek = currentOrNextSchoolDay(),
            selectedWeekOffset = 0,
            weekLabel = weekLabel,
            weekDays = weekDays
        )
    }

    private fun calculateWeekInfo(offset: Int): Pair<List<WeekDayInfo>, String> {
        val today = LocalDate.now()
        val isWeekend = today.dayOfWeek == DayOfWeek.SATURDAY || today.dayOfWeek == DayOfWeek.SUNDAY
        val baseMonday = if (isWeekend) {
            today.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
        } else {
            today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        }
        val monday = baseMonday.plusWeeks(offset.toLong())
        val dayNames = listOf("Hétfő", "Kedd", "Szerda", "Csütörtök", "Péntek")

        val days = (0..4).map { i ->
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

        val friday = monday.plusDays(4)
        val monthFormatter = DateTimeFormatter.ofPattern("yyyy. MMMM d.", Locale("hu"))
        val endDayFormatter = DateTimeFormatter.ofPattern("d.", Locale("hu"))
        val weekLabel = if (monday.month == friday.month) {
            "${monday.format(monthFormatter)} – ${friday.format(endDayFormatter)}"
        } else {
            "${monday.format(DateTimeFormatter.ofPattern("yyyy. MMM d.", Locale("hu")))} – ${friday.format(DateTimeFormatter.ofPattern("MMM d.", Locale("hu")))}"
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
            else -> 1
        }
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)
        val currentTotalMinutes = currentHour * 60 + currentMinute

        val todayIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val todayEvents = events.filter {
            if (it.dateString.isNotBlank()) it.dateString.startsWith(todayIso)
            else it.dayOfWeek == currentDayOfWeek
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
            val (weekDays, weekLabel) = calculateWeekInfo(newOffset)
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
            val (weekDays, weekLabel) = calculateWeekInfo(newOffset)
            state.copy(
                selectedWeekOffset = newOffset,
                weekDays = weekDays,
                weekLabel = weekLabel
            )
        }
    }

    fun currentWeek() {
        _uiState.update { state ->
            val (weekDays, weekLabel) = calculateWeekInfo(0)
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
        alarmScheduler.scheduleClassAlarm(event)
        _uiState.update { it.copy(notificationScheduledId = event.id) }
    }

    companion object {
        fun provideFactory(
            neptunRepository: NeptunRepository,
            alarmScheduler: AlarmScheduler
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TimetableViewModel(neptunRepository, alarmScheduler) as T
            }
        }
    }
}
