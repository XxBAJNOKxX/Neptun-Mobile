package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.FinanceItem
import com.example.domain.model.FinanceStatus
import com.example.domain.repository.NeptunRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FinancesUiState(
    val allFinances: List<FinanceItem> = emptyList(),
    val filteredFinances: List<FinanceItem> = emptyList(),
    val selectedStatusFilter: FinanceStatus? = null,
    val totalPendingHuf: Int = 0,
    val totalCompletedHuf: Int = 0,
    val isRefreshing: Boolean = false
)

class FinancesViewModel(
    private val neptunRepository: NeptunRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinancesUiState())
    val uiState: StateFlow<FinancesUiState> = _uiState.asStateFlow()

    init {
        observeFinances()
        refreshFinances()
    }

    private fun observeFinances() {
        viewModelScope.launch {
            neptunRepository.getFinances().collect { list ->
                val pending = list.filter { it.status == FinanceStatus.PENDING || it.status == FinanceStatus.OVERDUE }
                    .sumOf { it.amountHuf }
                val completed = list.filter { it.status == FinanceStatus.COMPLETED }
                    .sumOf { it.amountHuf }

                _uiState.update { state ->
                    val filtered = if (state.selectedStatusFilter != null) {
                        list.filter { it.status == state.selectedStatusFilter }
                    } else {
                        list
                    }
                    state.copy(
                        allFinances = list,
                        filteredFinances = filtered,
                        totalPendingHuf = pending,
                        totalCompletedHuf = completed
                    )
                }
            }
        }
    }

    fun setFilter(status: FinanceStatus?) {
        _uiState.update { state ->
            val filtered = if (status != null) {
                state.allFinances.filter { it.status == status }
            } else {
                state.allFinances
            }
            state.copy(selectedStatusFilter = status, filteredFinances = filtered)
        }
    }

    fun refreshFinances() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            neptunRepository.refreshFinances()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    companion object {
        fun provideFactory(
            neptunRepository: NeptunRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FinancesViewModel(neptunRepository) as T
            }
        }
    }
}
