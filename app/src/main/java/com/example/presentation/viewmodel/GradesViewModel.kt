package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.GradeCalculation
import com.example.domain.model.SubjectGrade
import com.example.domain.repository.NeptunRepository
import com.example.domain.usecase.CalculateAveragesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GradesUiState(
    val allGrades: List<SubjectGrade> = emptyList(),
    val availableTerms: List<String> = emptyList(),
    val selectedTerm: String = "",
    val termGrades: List<SubjectGrade> = emptyList(),
    val calculation: GradeCalculation? = null,
    val isRefreshing: Boolean = false,
    val ghostMarkDialogSubject: SubjectGrade? = null
)

class GradesViewModel(
    private val neptunRepository: NeptunRepository,
    private val calculateAveragesUseCase: CalculateAveragesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GradesUiState())
    val uiState: StateFlow<GradesUiState> = _uiState.asStateFlow()

    init {
        observeGrades()
        refreshGrades()
    }

    private fun observeGrades() {
        viewModelScope.launch {
            neptunRepository.getSubjectGrades().collect { grades ->
                val cleanedGrades = grades.map { grade ->
                    val cleanTerm = cleanTermString(grade.termId)
                    grade.copy(termId = cleanTerm, termName = cleanTermString(grade.termName).ifEmpty { "$cleanTerm félév" })
                }
                val terms = cleanedGrades.map { it.termId }.distinct().sortedDescending()
                val currentTerm = _uiState.value.selectedTerm.ifEmpty {
                    terms.firstOrNull() ?: "2025/26/1"
                }
                recalculateState(cleanedGrades, terms, currentTerm)
            }
        }
    }

    private fun cleanTermString(raw: String): String {
        if (raw.isBlank()) return "2025/26/1"
        val trimmed = raw.trim()
        if (trimmed.startsWith("{") || trimmed.contains("{")) {
            val regex = Regex(""""(?:id|termId|value|name|termName|text)"\s*:\s*"([^"]+)"""")
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

        _uiState.update {
            it.copy(
                allGrades = allGrades,
                availableTerms = terms,
                selectedTerm = selectedTerm,
                termGrades = filtered,
                calculation = calc
            )
        }
    }

    fun selectTerm(term: String) {
        val state = _uiState.value
        recalculateState(state.allGrades, state.availableTerms, term)
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

    companion object {
        fun provideFactory(
            neptunRepository: NeptunRepository,
            calculateAveragesUseCase: CalculateAveragesUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GradesViewModel(neptunRepository, calculateAveragesUseCase) as T
            }
        }
    }
}
