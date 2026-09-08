package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.security.EncryptedPreferencesManager
import com.example.domain.model.ExamItem
import com.example.domain.model.GradeCalculation
import com.example.domain.model.SubjectGrade
import com.example.domain.repository.NeptunRepository
import com.example.domain.usecase.CalculateAveragesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Egy félév kreditindex statisztikája a diagramhoz. */
data class TermStat(
    val termId: String,
    val weightedAverage: Double,
    val creditIndex: Double,
    val completedCredits: Int
)

data class GradesUiState(
    val allGrades: List<SubjectGrade> = emptyList(),
    val availableTerms: List<String> = emptyList(),
    val selectedTerm: String = "",
    val termGrades: List<SubjectGrade> = emptyList(),
    val calculation: GradeCalculation? = null,
    val isRefreshing: Boolean = false,
    val ghostMarkDialogSubject: SubjectGrade? = null,
    // Új: statisztika és vizsgák
    val termStats: List<TermStat> = emptyList(),
    val totalCompletedCredits: Int = 0,
    val targetCredits: Int = 240,
    val selectedTab: Int = 0, // 0 = Jegyek, 1 = Vizsgák
    val exams: List<ExamItem> = emptyList(),
    val isRefreshingExams: Boolean = false
)

class GradesViewModel(
    private val neptunRepository: NeptunRepository,
    private val calculateAveragesUseCase: CalculateAveragesUseCase,
    private val prefsManager: EncryptedPreferencesManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        GradesUiState(targetCredits = prefsManager?.loadPersonalization()?.targetCredits ?: 240)
    )
    val uiState: StateFlow<GradesUiState> = _uiState.asStateFlow()

    init {
        observeGrades()
        observeExams()
        observeTargetCredits()
        refreshGrades()
        refreshExams()
    }

    private fun observeTargetCredits() {
        viewModelScope.launch {
            prefsManager?.personalizationFlow?.collect { personalization ->
                _uiState.update { it.copy(targetCredits = personalization.targetCredits) }
            }
        }
    }

    private fun observeGrades() {
        viewModelScope.launch {
            neptunRepository.getSubjectGrades().collect { grades ->
                val cleanedGrades = grades.map { grade ->
                    val cleanTermId = cleanTermString(grade.termId)
                    val cleanTermName = cleanTermString(grade.termName)
                    val rawDisplayTerm = if (cleanTermId.isNotEmpty() && !isUuid(cleanTermId)) {
                        cleanTermId
                    } else if (cleanTermName.isNotEmpty() && !isUuid(cleanTermName)) {
                        cleanTermName
                    } else {
                        "2025/26/1"
                    }
                    val displayTerm = rawDisplayTerm.replace(Regex("""(?i)\s*félév.*"""), "").trim()
                    grade.copy(termId = displayTerm, termName = displayTerm)
                }
                val terms = cleanedGrades.map { it.termId }.distinct().sortedDescending()
                val currentTerm = _uiState.value.selectedTerm.ifEmpty {
                    terms.firstOrNull() ?: "2026/27/1"
                }
                recalculateState(cleanedGrades, terms, currentTerm)
            }
        }
    }

    private fun observeExams() {
        viewModelScope.launch {
            neptunRepository.getExams().collect { exams ->
                _uiState.update { it.copy(exams = exams) }
            }
        }
    }

    private fun isUuid(str: String): Boolean {
        return str.length >= 32 && str.contains("-") && str.count { it == '-' } >= 3
    }

    private fun cleanTermString(raw: String): String {
        if (raw.isBlank()) return ""
        val trimmed = raw.trim()
        if (trimmed.startsWith("{") || trimmed.contains("{")) {
            val regex = Regex(""""(?:text|termName|name|id|termId|value)"\s*:\s*"([^"]+)"""")
            val match = regex.find(trimmed)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }
        return trimmed
    }

    private fun recalculateState(
        allGrades: List<SubjectGrade>,
        terms: List<String>,
        selectedTerm: String
    ) {
        val filtered = if (selectedTerm.isEmpty()) {
            allGrades
        } else {
            val res = allGrades.filter { it.termId == selectedTerm || it.termName == selectedTerm }
            if (res.isEmpty()) allGrades else res
        }
        val calc = calculateAveragesUseCase(selectedTerm, filtered)

        // Félévenkénti kreditindex – a diagramhoz (legutóbbi 6 félév)
        val termStats = terms.take(6)
            .reversed()
            .map { term ->
                val termSubjects = allGrades.filter { it.termId == term || it.termName == term }
                val termCalc = calculateAveragesUseCase(term, termSubjects)
                TermStat(
                    termId = term,
                    weightedAverage = termCalc.weightedAverage,
                    creditIndex = termCalc.creditIndex,
                    completedCredits = termCalc.completedCredits
                )
            }

        val totalCompleted = allGrades
            .filter { it.grade != null && it.grade >= 2 }
            .sumOf { it.credit }

        _uiState.update {
            it.copy(
                allGrades = allGrades,
                availableTerms = terms,
                selectedTerm = selectedTerm,
                termGrades = filtered,
                calculation = calc,
                termStats = termStats,
                totalCompletedCredits = totalCompleted
            )
        }
    }

    fun selectTerm(term: String) {
        val state = _uiState.value
        recalculateState(state.allGrades, state.availableTerms, term)
    }

    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab.coerceIn(0, 1)) }
    }

    fun openGhostMarkDialog(subject: SubjectGrade) {
        _uiState.update { it.copy(ghostMarkDialogSubject = subject) }
    }

    fun closeGhostMarkDialog() {
        _uiState.update { it.copy(ghostMarkDialogSubject = null) }
    }

    fun setGhostGrade(subjectId: String, grade: Int?) {
        viewModelScope.launch {
            neptunRepository.setGhostGrade(subjectId, grade)
            closeGhostMarkDialog()
        }
    }

    fun resetAllGhostGrades() {
        viewModelScope.launch {
            neptunRepository.resetAllGhostGrades()
        }
    }

    fun refreshGrades() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            neptunRepository.refreshGrades()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun refreshExams() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshingExams = true) }
            neptunRepository.refreshExams()
            _uiState.update { it.copy(isRefreshingExams = false) }
        }
    }

    companion object {
        fun provideFactory(
            neptunRepository: NeptunRepository,
            calculateAveragesUseCase: CalculateAveragesUseCase,
            prefsManager: EncryptedPreferencesManager? = null
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GradesViewModel(neptunRepository, calculateAveragesUseCase, prefsManager) as T
            }
        }
    }
}
