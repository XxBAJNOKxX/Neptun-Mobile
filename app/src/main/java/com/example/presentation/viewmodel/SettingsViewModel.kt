package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.security.EncryptedPreferencesManager
import com.example.domain.model.StudentCredentials
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.NeptunRepository
import com.example.ui.theme.AppAccentColor
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ThemeSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val credentials: StudentCredentials? = null,
    val themeSettings: ThemeSettings = ThemeSettings(),
    val isSyncing: Boolean = false,
    val syncSuccessMessage: String? = null
)

class SettingsViewModel(
    private val prefsManager: EncryptedPreferencesManager,
    private val authRepository: AuthRepository,
    private val neptunRepository: NeptunRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            credentials = prefsManager.loadCredentials(),
            themeSettings = prefsManager.loadThemeSettings()
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefsManager.credentialsFlow.collect { creds ->
                _uiState.update { it.copy(credentials = creds) }
            }
        }
        viewModelScope.launch {
            prefsManager.themeSettingsFlow.collect { theme ->
                _uiState.update { it.copy(themeSettings = theme) }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefsManager.setThemeMode(mode)
    }

    fun setDynamicColor(enabled: Boolean) {
        prefsManager.setDynamicColor(enabled)
    }

    fun setAccentColor(accent: AppAccentColor) {
        prefsManager.setAccentColor(accent)
        // If user manually picks an accent, we turn off dynamic color so the chosen accent takes effect
        if (_uiState.value.themeSettings.useDynamicColor) {
            prefsManager.setDynamicColor(false)
        }
    }

    companion object {
        fun provideFactory(
            prefsManager: EncryptedPreferencesManager,
            authRepository: AuthRepository,
            neptunRepository: NeptunRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(prefsManager, authRepository, neptunRepository) as T
            }
        }
    }
}
