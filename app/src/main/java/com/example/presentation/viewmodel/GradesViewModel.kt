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
                val terms = grades.map { it.termId }.distinct().sortedDescending()
                val currentTerm = _uiState.value.selectedTerm.ifEmpty {
                    terms.firstOrNull() ?: "2025/26/1"
                }
                recalculateState(grades, terms, currentTerm)
            }
        }
    }

    private fun recalculateState(
        allGrades: List<SubjectGrade>,
        terms: List<String>,
        selectedTerm: String
    ) {
        val filtered = allGrades.filter { it.termId == selectedTerm }
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
