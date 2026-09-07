package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.CalendarEvent
import com.example.domain.model.FinanceItem
import com.example.domain.model.FinanceStatus
import com.example.domain.model.NeptunMessage
import com.example.domain.model.SubjectGrade
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.NeptunRepository
import com.example.domain.usecase.BuildStudyProfileUseCase
import com.example.domain.usecase.CalculateAveragesUseCase
import com.example.domain.usecase.StudyProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale

/** A Kezdőlap "élő kártyájának" állapotai. */
enum class LiveCardMode {
    /** Van éppen óránk. */
    IN_CLASS,

    /** Hamarosan kezdődik egy óra. */
    SOON,

    /** Ma van még óra, de messze. */
    LATER_TODAY,

    /** Ma nincs több óra. */
    DONE_TODAY,

    /** Hétvége vagy tanítás nélküli nap. */
    NO_SCHOOL
}

data class HomeUiState(
    val firstName: String = "Hallgató",
    val fullName: String = "",
    val universityName: String = "",
    val liveCardMode: LiveCardMode = LiveCardMode.NO_SCHOOL,
    val liveCardTitle: String = "",
    val liveCardSubtitle: String = "",
    val liveCardProgress: Float = 0f,
    val liveCardCountdown: String = "",
    val todayLessons: List<CalendarEvent> = emptyList(),
    val recentGrades: List<SubjectGrade> = emptyList(),
    val unreadCount: Int = 0,
    val latestMessages: List<NeptunMessage> = emptyList(),
    val pendingAmountHuf: Int = 0,
    val overdueAmountHuf: Int = 0,
    val nextDueItem: FinanceItem? = null,
    val weightedAverage: Double = 0.0,
    val previousTermAverage: Double = 0.0,
    val earnedCredits: Int = 0,
    val profile: StudyProfile = StudyProfile.Empty,
    val isRefreshing: Boolean = false,
    val greeting: String = "Szia",
    val todayLabel: String = ""
) {
    val averageDelta: Double
        get() = if (previousTermAverage <= 0.0) 0.0 else weightedAverage - previousTermAverage

    val hasAnything: Boolean
        get() = todayLessons.isNotEmpty() || recentGrades.isNotEmpty() || latestMessages.isNotEmpty()
}

/**
 * A Kezdőlap aggregált nézete: az összes Neptun flow-t egy állapotba futtatja
 * össze, és 30 másodpercenként frissíti az "élő kártya" visszaszámlálását.
 */
class HomeViewModel(
    private val neptunRepository: NeptunRepository,
    private val authRepository: AuthRepository,
    private val calculateAveragesUseCase: CalculateAveragesUseCase,
    private val buildStudyProfileUseCase: BuildStudyProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var grades: List<SubjectGrade> = emptyList()
    private var events: List<CalendarEvent> = emptyList()

    init {
        observeCredentials()
        observeGrades()
        observeMessages()
        observeFinances()
        observeCalendar()
        startTicker()
        refreshAll()
    }

    private fun observeCredentials() {
        viewModelScope.launch {
            authRepository.getCredentials().collect { creds ->
                val name = creds?.studentName.orEmpty()
                val first = name.split(" ").filter { it.isNotBlank() }.let {
                    if (it.size >= 2) it[1] else it.firstOrNull().orEmpty()
                }
                _uiState.update {
                    it.copy(
                        fullName = name,
                        firstName = first.ifEmpty { "Hallgató" },
                        universityName = creds?.universityName.orEmpty()
                    )
                }
            }
        }
    }

    private fun observeGrades() {
        viewModelScope.launch {
            neptunRepository.getSubjectGrades().collect { list ->
                grades = list
                val terms = list.map { it.termId }.distinct().sortedDescending()
                val currentTerm = terms.firstOrNull().orEmpty()
                val current = list.filter { it.termId == currentTerm }
                val calculation = calculateAveragesUseCase(currentTerm, current)
                val previous = terms.getOrNull(1)
                    ?.let { prevTerm ->
                        val prevList = list.filter { it.termId == prevTerm }
                        calculateAveragesUseCase(prevTerm, prevList).weightedAverage
                    }
                    ?: 0.0

                _uiState.update {
                    it.copy(
                        recentGrades = current
                            .filter { g -> g.effectiveGrade != null }
                            .takeLast(4)
                            .reversed(),
                        weightedAverage = calculation.weightedAverage,
                        previousTermAverage = previous,
                        earnedCredits = calculation.completedCredits,
                        profile = buildStudyProfileUseCase(current, events)
                    )
                }
            }
        }
    }

    private fun observeMessages() {
        viewModelScope.launch {
            neptunRepository.getMessages().collect { list ->
                _uiState.update {
                    it.copy(
                        unreadCount = list.count { m -> !m.isRead },
                        latestMessages = list.take(3)
                    )
                }
            }
        }
    }

    private fun observeFinances() {
        viewModelScope.launch {
            neptunRepository.getFinances().collect { list ->
                val open = list.filter { it.status != FinanceStatus.COMPLETED }
                _uiState.update {
                    it.copy(
                        pendingAmountHuf = open.sumOf { item -> item.amountHuf },
                        overdueAmountHuf = list
                            .filter { item -> item.status == FinanceStatus.OVERDUE }
                            .sumOf { item -> item.amountHuf },
                        nextDueItem = open.minByOrNull { item -> item.dueDate }
                    )
                }
            }
        }
    }

    private fun observeCalendar() {
        viewModelScope.launch {
            neptunRepository.getCalendarEvents().collect { list ->
                events = list
                _uiState.update { it.copy(profile = buildStudyProfileUseCase(grades, list)) }
                recomputeLiveCard()
            }
        }
    }

    private fun startTicker() {
        viewModelScope.launch {
            while (true) {
                recomputeLiveCard()
                delay(30_000)
            }
        }
    }

    private fun recomputeLiveCard() {
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val todayIso = today.toString()
        val dayOfWeek = today.dayOfWeek.value

        val todays = events
            .filter { event ->
                event.isActualAttendedClass &&
                    if (event.dateString.isNotBlank()) {
                        event.dateString.startsWith(todayIso)
                    } else {
                        event.dayOfWeek == dayOfWeek && dayOfWeek <= 5
                    }
            }
            .sortedWith(compareBy({ it.startHour }, { it.startMinute }))

        val nowMinutes = now.hour * 60 + now.minute
        val ongoing = todays.firstOrNull { event ->
            nowMinutes in (event.startHour * 60 + event.startMinute)..(event.endHour * 60 + event.endMinute)
        }
        val upcoming = todays.firstOrNull { event ->
            event.startHour * 60 + event.startMinute > nowMinutes
        }

        val isWeekend = today.dayOfWeek == DayOfWeek.SATURDAY || today.dayOfWeek == DayOfWeek.SUNDAY
        val mode: LiveCardMode
        val title: String
        val subtitle: String
        var progress = 0f
        var countdown = ""

        when {
            ongoing != null -> {
                mode = LiveCardMode.IN_CLASS
                title = "Épp óra közben vagy"
                subtitle = "${ongoing.subjectName} · ${ongoing.room}"
                val start = ongoing.startHour * 60 + ongoing.startMinute
                val end = ongoing.endHour * 60 + ongoing.endMinute
                progress = if (end > start) (nowMinutes - start).toFloat() / (end - start) else 0f
                countdown = "${maxOf(0, end - nowMinutes)} perc van hátra"
            }

            upcoming != null -> {
                val minutes = upcoming.startHour * 60 + upcoming.startMinute - nowMinutes
                mode = if (minutes <= 90) LiveCardMode.SOON else LiveCardMode.LATER_TODAY
                title = if (minutes <= 90) "Már készülődsz?" else "Következő óra ma"
                subtitle = "${upcoming.subjectName} · ${upcoming.room}"
                progress = 0f
                countdown = if (minutes < 60) {
                    "$minutes perc múlva"
                } else {
                    "${minutes / 60} óra %02d perc".format(minutes % 60)
                }
            }

            isWeekend -> {
                mode = LiveCardMode.NO_SCHOOL
                title = "Hétvége van"
                subtitle = "Pihenj, a héten is lesz dolgod bőven."
                countdown = ""
            }

            todays.isEmpty() -> {
                mode = LiveCardMode.NO_SCHOOL
                title = if (dayOfWeek <= 5) "Ma nincs órád" else "Tanítás nélküli nap"
                subtitle = "Az órarended szerint szabad a nap – vagy csak üresen hagyott sor."
                countdown = ""
            }

            else -> {
                mode = LiveCardMode.DONE_TODAY
                title = "Kész vagy mára"
                subtitle = "${todays.size} óra lezajlott. Szép munka!"
                countdown = ""
            }
        }

        _uiState.update { state ->
            state.copy(
                greeting = buildGreeting(now.hour),
                todayLabel = buildDateLabel(today),
                liveCardMode = mode,
                liveCardTitle = title,
                liveCardSubtitle = subtitle,
                liveCardProgress = progress,
                liveCardCountdown = countdown,
                todayLessons = todays
            )
        }
    }

    private fun buildGreeting(hour: Int): String = when {
        hour < 5 -> "Jó éjszakát"
        hour < 10 -> "Jó reggelt"
        hour < 18 -> "Szép napot"
        hour < 22 -> "Estét kívánok"
        else -> "Jó pihenést"
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            neptunRepository.refreshCalendar()
            neptunRepository.refreshGrades()
            neptunRepository.refreshMessages()
            neptunRepository.refreshFinances()
            _uiState.update { it.copy(isRefreshing = false) }
            recomputeLiveCard()
        }
    }

    private fun buildDateLabel(date: LocalDate): String {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d.", Locale("hu"))
        return date.format(formatter)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("hu")) else it.toString() }
    }

    companion object {
        fun provideFactory(
            neptunRepository: NeptunRepository,
            authRepository: AuthRepository,
            calculateAveragesUseCase: CalculateAveragesUseCase,
            buildStudyProfileUseCase: BuildStudyProfileUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(
                    neptunRepository = neptunRepository,
                    authRepository = authRepository,
                    calculateAveragesUseCase = calculateAveragesUseCase,
                    buildStudyProfileUseCase = buildStudyProfileUseCase
                ) as T
            }
        }
    }
}
