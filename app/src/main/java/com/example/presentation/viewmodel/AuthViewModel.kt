package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Neptun2FASession
import com.example.domain.model.StudentCredentials
import com.example.domain.model.TwoFactorMethod
import com.example.R
import com.example.core.locale.StringProvider
import com.example.domain.model.University
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.LanguageRepository
import com.example.domain.repository.NeptunRepository
import com.example.domain.repository.TwoFactorRequiredException
import com.example.domain.repository.TwoFactorSessionRequiredException
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
    // 2FA State
    val isTwoFactorRequired: Boolean = false,
    val twoFactorSession: Neptun2FASession? = null,
    val twoFactorMethod: TwoFactorMethod = TwoFactorMethod.EMAIL,
    val isEmailCodeRequested: Boolean = false,
    val codePrefix: String = "",
    val twoFactorCode: String = "",
    val twoFactorSuccessMessage: String? = null,
    val twoFactorErrorMessage: String? = null,
    val isTwoFactorLoading: Boolean = false,
    // Neptun szervernyelv-választó
    val serverLanguage: ServerLanguageUiState = ServerLanguageUiState()
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val neptunRepository: NeptunRepository,
    private val languageRepository: LanguageRepository,
    private val strings: StringProvider
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
            val savedUniId = authRepository.getSavedUniversityId()
            val savedUniUrl = authRepository.getSavedUniversityUrl()
            val savedUniName = authRepository.getSavedUniversityName()
            val savedNeptunCode = authRepository.getSavedNeptunCode()

            // Match previously selected / saved university
            val matchedUni = list.firstOrNull { uni ->
                (savedUniId.isNotEmpty() && uni.id.equals(savedUniId, ignoreCase = true)) ||
                (savedUniUrl.isNotEmpty() && uni.neptunUrl.trimEnd('/').equals(savedUniUrl.trimEnd('/'), ignoreCase = true)) ||
                (savedUniName.isNotEmpty() && uni.name.equals(savedUniName, ignoreCase = true))
            } ?: list.firstOrNull { uni -> uni.id == "etvslorndtud" || uni.id == "elte" || uni.neptunUrl.contains("elte.hu") }
              ?: list.firstOrNull()

            _uiState.update {
                it.copy(
                    universities = list,
                    filteredUniversities = list,
                    selectedUniversity = matchedUni,
                    neptunCode = it.neptunCode.ifEmpty { savedNeptunCode },
                    isOfflineModeAvailable = offlineAvailable
                )
            }
            loadServerLanguages(matchedUni)
        }

        viewModelScope.launch {
            authRepository.getCredentials().collect { creds ->
                _uiState.update { state ->
                    val matchedUni = if (creds != null) {
                        state.universities.firstOrNull { uni ->
                            (creds.universityId.isNotEmpty() && uni.id.equals(creds.universityId, ignoreCase = true)) ||
                            (creds.neptunUrl.isNotEmpty() && uni.neptunUrl.trimEnd('/').equals(creds.neptunUrl.trimEnd('/'), ignoreCase = true)) ||
                            (creds.universityName.isNotEmpty() && uni.name.equals(creds.universityName, ignoreCase = true))
                        } ?: state.selectedUniversity
                    } else state.selectedUniversity

                    state.copy(
                        credentials = creds,
                        selectedUniversity = matchedUni ?: state.selectedUniversity,
                        neptunCode = creds?.neptunCode ?: state.neptunCode.ifEmpty { authRepository.getSavedNeptunCode() }
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
        authRepository.saveSelectedUniversity(university)
        _uiState.update {
            it.copy(
                selectedUniversity = university,
                isUniversityDropdownOpen = false,
                errorMessage = null
            )
        }
        loadServerLanguages(university)
    }

    /**
     * Az intézmény támogatott nyelveinek betöltése: először a gyorsítótárból
     * (azonnal), majd háttérben a szerverről frissítve. A bejelentkezés sosem
     * blokkolódik emiatt – hiba esetén a beépített hu/en/de lista látszik.
     */
    fun loadServerLanguages(university: University?) {
        if (university == null) return
        val cached = languageRepository.getCachedLanguages(university.neptunUrl)
        _uiState.update {
            it.copy(
                serverLanguage = ServerLanguageUiState(
                    languages = cached.languages,
                    selectedLcid = languageRepository.getSelectedLcid(),
                    isLoading = true,
                    isFallback = cached.isFallback
                )
            )
        }
        viewModelScope.launch {
            val fresh = languageRepository.refreshLanguages(university.neptunUrl)
            _uiState.update {
                it.copy(
                    serverLanguage = ServerLanguageUiState(
                        languages = fresh.languages,
                        selectedLcid = languageRepository.getSelectedLcid(),
                        isLoading = false,
                        isFallback = fresh.isFallback
                    )
                )
            }
        }
    }

    fun refreshServerLanguages() {
        loadServerLanguages(_uiState.value.selectedUniversity)
    }

    fun selectServerLanguage(lcid: Int) {
        viewModelScope.launch {
            languageRepository.setSelectedLcid(lcid)
            _uiState.update {
                it.copy(
                    serverLanguage = it.serverLanguage.copy(
                        selectedLcid = languageRepository.getSelectedLcid()
                    )
                )
            }
        }
    }

    fun setUniversityDropdownOpen(open: Boolean) {
        _uiState.update { it.copy(isUniversityDropdownOpen = open) }
    }

    fun onNeptunCodeChange(code: String) {
        if (code.length <= 6) {
            val upper = code.uppercase()
            authRepository.saveNeptunCode(upper)
            _uiState.update { it.copy(neptunCode = upper, errorMessage = null) }
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onTwoFactorCodeChange(code: String) {
        if (code.length <= 6) {
            _uiState.update { it.copy(twoFactorCode = code, twoFactorErrorMessage = null) }
        }
    }

    fun onTwoFactorMethodChange(method: TwoFactorMethod) {
        _uiState.update {
            it.copy(
                twoFactorMethod = method,
                twoFactorCode = "",
                twoFactorErrorMessage = null
            )
        }
    }

    fun cancelTwoFactor() {
        _uiState.update {
            it.copy(
                isTwoFactorRequired = false,
                twoFactorSession = null,
                twoFactorCode = "",
                isEmailCodeRequested = false,
                codePrefix = "",
                twoFactorSuccessMessage = null,
                twoFactorErrorMessage = null,
                isTwoFactorLoading = false,
                errorMessage = null
            )
        }
    }

    fun quickDemoFill() {
        authRepository.saveNeptunCode("DEMO01")
        _uiState.update {
            it.copy(
                neptunCode = "DEMO01",
                password = "demo",
                errorMessage = null
            )
        }
    }

    fun requestEmailCode() {
        val session = _uiState.value.twoFactorSession ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isTwoFactorLoading = true, twoFactorErrorMessage = null, twoFactorSuccessMessage = null) }
            val result = authRepository.request2FAEmailCode(session)
            result.fold(
                onSuccess = { updatedSession ->
                    val prefixText = if (updatedSession.codePrefix.isNotEmpty()) "${updatedSession.codePrefix}-" else ""
                    _uiState.update {
                        it.copy(
                            isTwoFactorLoading = false,
                            twoFactorSession = updatedSession,
                            isEmailCodeRequested = true,
                            codePrefix = updatedSession.codePrefix,
                            twoFactorSuccessMessage = if (prefixText.isNotEmpty()) {
                                strings.getString(R.string.auth_2fa_sent_prefix, prefixText)
                            } else {
                                strings.getString(R.string.auth_2fa_sent)
                            }
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isTwoFactorLoading = false,
                            twoFactorErrorMessage = error.message ?: strings.getString(R.string.auth_err_email_send)
                        )
                    }
                }
            )
        }
    }

    fun login() {
        val state = _uiState.value
        val university = state.selectedUniversity
        if (university == null) {
            _uiState.update { it.copy(errorMessage = strings.getString(R.string.auth_err_no_uni)) }
            return
        }
        if (state.neptunCode.length != 6) {
            _uiState.update { it.copy(errorMessage = strings.getString(R.string.auth_err_code_len)) }
            return
        }
        if (state.password.isEmpty()) {
            _uiState.update { it.copy(errorMessage = strings.getString(R.string.auth_err_no_pass)) }
            return
        }

        authRepository.saveSelectedUniversity(university)
        authRepository.saveNeptunCode(state.neptunCode)

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
                            twoFactorSession = null,
                            errorMessage = null
                        )
                    }
                    neptunRepository.syncAllData(creds.neptunCode, "")
                },
                onFailure = { error ->
                    when (error) {
                        is TwoFactorSessionRequiredException -> {
                            val session = error.session
                            val defaultMethod = if (session.hasEmail) TwoFactorMethod.EMAIL else TwoFactorMethod.TOTP
                            val isEmailReq = session.codePrefix.isNotEmpty() || session.phase.equals("RequestEmailCode", ignoreCase = true)
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    isTwoFactorRequired = true,
                                    twoFactorSession = session,
                                    twoFactorMethod = defaultMethod,
                                    isEmailCodeRequested = isEmailReq,
                                    codePrefix = session.codePrefix,
                                    twoFactorSuccessMessage = if (isEmailReq && session.codePrefix.isNotEmpty()) strings.getString(R.string.auth_2fa_prefix_only, "${session.codePrefix}-") else null,
                                    twoFactorErrorMessage = null,
                                    errorMessage = null
                                )
                            }
                        }
                        is TwoFactorRequiredException -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    isTwoFactorRequired = true,
                                    twoFactorSession = null,
                                    twoFactorMethod = TwoFactorMethod.TOTP,
                                    errorMessage = null
                                )
                            }
                        }
                        else -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = error.message ?: strings.getString(R.string.auth_err_login_failed)
                                )
                            }
                        }
                    }
                }
            )
        }
    }

    fun submitTwoFactor() {
        val state = _uiState.value
        val session = state.twoFactorSession

        if (session != null) {
            val code = state.twoFactorCode.trim()
            if (code.isEmpty()) {
                _uiState.update { it.copy(twoFactorErrorMessage = strings.getString(R.string.auth_err_2fa_code_empty)) }
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(isTwoFactorLoading = true, twoFactorErrorMessage = null) }
                val isTotp = state.twoFactorMethod == TwoFactorMethod.TOTP
                val result = authRepository.verify2FACode(session, code, isTotp)

                result.fold(
                    onSuccess = { creds ->
                        _uiState.update {
                            it.copy(
                                isTwoFactorLoading = false,
                                isTwoFactorRequired = false,
                                twoFactorSession = null,
                                credentials = creds,
                                twoFactorCode = "",
                                errorMessage = null
                            )
                        }
                        neptunRepository.syncAllData(creds.neptunCode, "")
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isTwoFactorLoading = false,
                                twoFactorErrorMessage = error.message ?: strings.getString(R.string.auth_err_2fa_failed)
                            )
                        }
                    }
                )
            }
        } else {
            login()
        }
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
                    twoFactorSession = null,
                    twoFactorCode = "",
                    twoFactorSuccessMessage = null,
                    twoFactorErrorMessage = null
                )
            }
        }
    }

    companion object {
        fun provideFactory(
            authRepository: AuthRepository,
            neptunRepository: NeptunRepository,
            languageRepository: LanguageRepository,
            strings: StringProvider
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(authRepository, neptunRepository, languageRepository, strings) as T
            }
        }
    }
}

