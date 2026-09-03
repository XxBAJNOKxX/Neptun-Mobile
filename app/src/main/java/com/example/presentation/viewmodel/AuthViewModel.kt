package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.StudentCredentials
import com.example.domain.model.University
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.NeptunRepository
import com.example.domain.repository.TwoFactorRequiredException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val universities: List<University> = emptyList(),
    val filteredUniversities: List<University> = emptyList(),
    val selectedUniversity: University? = null,
    val searchQuery: String = "",
    val neptunCode: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val credentials: StudentCredentials? = null,
    val isOfflineModeAvailable: Boolean = false,
    val isUniversityDropdownOpen: Boolean = false,
    val isTwoFactorRequired: Boolean = false,
    val twoFactorCode: String = ""
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val neptunRepository: NeptunRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val list = authRepository.loadUniversitiesFromAssets()
            val offlineAvailable = authRepository.isOfflineModeAvailable()
            _uiState.update {
                it.copy(
                    universities = list,
                    filteredUniversities = list,
                    selectedUniversity = list.firstOrNull(),
                    neptunCode = it.neptunCode.ifEmpty { "ZM3I1I" },
                    isOfflineModeAvailable = offlineAvailable
                )
            }
        }

        viewModelScope.launch {
            authRepository.getCredentials().collect { creds ->
                _uiState.update {
                    it.copy(
                        credentials = creds,
                        neptunCode = creds?.neptunCode ?: it.neptunCode
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            val filtered = if (query.isBlank()) {
                state.universities
            } else {
                state.universities.filter { uni ->
                    uni.name.contains(query, ignoreCase = true) ||
                            uni.shortName.contains(query, ignoreCase = true) ||
                            uni.city.contains(query, ignoreCase = true)
                }
            }
            state.copy(searchQuery = query, filteredUniversities = filtered)
        }
    }

    fun onSelectUniversity(university: University) {
        _uiState.update {
            it.copy(
                selectedUniversity = university,
                isUniversityDropdownOpen = false,
                errorMessage = null
            )
        }
    }

    fun setUniversityDropdownOpen(open: Boolean) {
        _uiState.update { it.copy(isUniversityDropdownOpen = open) }
    }

    fun onNeptunCodeChange(code: String) {
        if (code.length <= 6) {
            _uiState.update { it.copy(neptunCode = code.uppercase(), errorMessage = null) }
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onTwoFactorCodeChange(code: String) {
        if (code.length <= 6) {
            _uiState.update { it.copy(twoFactorCode = code, errorMessage = null) }
        }
    }

    fun cancelTwoFactor() {
        _uiState.update { it.copy(isTwoFactorRequired = false, twoFactorCode = "", errorMessage = null) }
    }

    fun quickDemoFill() {
        _uiState.update {
            it.copy(
                neptunCode = "DEMO01",
                password = "demo",
                errorMessage = null
            )
        }
    }

    fun login() {
        val state = _uiState.value
        val university = state.selectedUniversity
        if (university == null) {
            _uiState.update { it.copy(errorMessage = "Kérjük, válassz egy egyetemet!") }
            return
        }
        if (state.neptunCode.length != 6) {
            _uiState.update { it.copy(errorMessage = "A Neptun kódnak 6 karakternek kell lennie!") }
            return
        }
        if (state.password.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Kérjük, add meg a jelszavadat!") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.login(
                university = university,
                neptunCode = state.neptunCode,
                password = state.password,
                twoFactorCode = state.twoFactorCode
            )

            result.fold(
                onSuccess = { creds ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            credentials = creds,
                            isTwoFactorRequired = false,
                            errorMessage = null
                        )
                    }
                    // Trigger real data sync
                    neptunRepository.syncAllData(creds.neptunCode, "")
                },
                onFailure = { error ->
                    if (error is TwoFactorRequiredException) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isTwoFactorRequired = true,
                                errorMessage = null
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "Sikertelen bejelentkezés!"
                            )
                        }
                    }
                }
            )
        }
    }

    fun submitTwoFactor() {
        login()
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            neptunRepository.clearLocalData()
            _uiState.update {
                it.copy(
                    credentials = null,
                    password = "",
                    isOfflineModeAvailable = false,
                    isTwoFactorRequired = false,
                    twoFactorCode = ""
                )
            }
        }
    }

    companion object {
        fun provideFactory(
            authRepository: AuthRepository,
            neptunRepository: NeptunRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(authRepository, neptunRepository) as T
            }
        }
    }
}
