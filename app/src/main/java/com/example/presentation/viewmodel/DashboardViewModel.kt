package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.CalendarEvent
import com.example.domain.model.FinanceItem
import com.example.domain.model.FinanceStatus
import com.example.domain.model.NeptunMessage
import com.example.domain.model.SubjectGrade
import com.example.domain.repository.NeptunRepository
import com.example.domain.usecase.CalculateAveragesUseCase
import com.example.domain.usecase.GetTodayClassesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

data class DashboardUiState(
    val studentName: String = "",
    val todayClasses: List<CalendarEvent> = emptyList(),
    val ongoingEvent: CalendarEvent? = null,
    val nextEvent: CalendarEvent? = null,
    val unreadMessages: Int = 0,
    val latestGrades: List<SubjectGrade> = emptyList(),
    val gradedCountThisTerm: Int = 0,
    val pendingFinanceHuf: Int = 0,
    val nextDueFinance: FinanceItem? = null,
    val isRefreshing: Boolean = false,
    val greeting: String = ""
)

class DashboardViewModel(
    private val neptunRepository: NeptunRepository,
    private val calculateAveragesUseCase: CalculateAveragesUseCase
) : ViewModel() {

    private val getTodayClasses = GetTodayClassesUseCase()

    private val _uiState = MutableStateFlow(DashboardUiState(greeting = buildGreeting()))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            neptunRepository.getCalendarEvents().collect { events ->
                val today = getTodayClasses(events, LocalDate.now(), LocalTime.now())
                _uiState.update {
                    it.copy(
                        todayClasses = today,
                        ongoingEvent = getTodayClasses.ongoingClass(today),
                        nextEvent = getTodayClasses.nextClass(today)
                    )
                }
            }
        }
        viewModelScope.launch {
            neptunRepository.getMessages().collect { messages ->
                _uiState.update {
                    it.copy(unreadMessages = messages.count { msg -> !msg.isRead })
                }
            }
        }
        viewModelScope.launch {
            neptunRepository.getSubjectGrades().collect { grades ->
                val latest = grades
                    .filter { it.grade != null }
                    .sortedByDescending { it.termId }
                    .take(3)
                _uiState.update {
                    it.copy(latestGrades = latest)
                }
            }
        }
        viewModelScope.launch {
            neptunRepository.getFinances().collect { finances ->
                val pending = finances.filter {
                    it.status == FinanceStatus.PENDING || it.status == FinanceStatus.OVERDUE
                }
                _uiState.update {
                    it.copy(
                        pendingFinanceHuf = pending.sumOf { item -> item.amountHuf },
                        nextDueFinance = pending.minByOrNull { item -> item.dueDate }
                    )
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            try {
                neptunRepository.syncAllData("", "")
            } catch (e: Exception) {
                // A képernyők a helyi adatokat mutatják, hiba esetén is
            }
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    private fun buildGreeting(): String {
        val hour = LocalTime.now().hour
        return when {
            hour < 5 -> "Jó éjszakát"
            hour < 9 -> "Jó reggelt"
            hour < 18 -> "Szia"
            else -> "Jó estét"
        }
    }

    companion object {
        fun provideFactory(
            neptunRepository: NeptunRepository,
            calculateAveragesUseCase: CalculateAveragesUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DashboardViewModel(neptunRepository, calculateAveragesUseCase) as T
            }
        }
    }
}
