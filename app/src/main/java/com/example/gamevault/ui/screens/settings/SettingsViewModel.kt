package com.example.gamevault.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gamevault.data.local.preferences.AppPreferences
import com.example.gamevault.data.repository.AuthRepository
import com.example.gamevault.data.repository.SearchRepository
import com.example.gamevault.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val appTheme: AppTheme = AppTheme.CYBER_DARK,
    val language: String = "en",
    val showDeleteDialog: Boolean = false,
    val historyCleared: Boolean = false
)

class SettingsViewModel(
    private val appPreferences: AppPreferences,
    private val authRepository: AuthRepository,
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appPreferences.appTheme.collect { theme ->
                _uiState.value = _uiState.value.copy(appTheme = theme)
            }
        }
        viewModelScope.launch {
            appPreferences.language.collect { lang ->
                _uiState.value = _uiState.value.copy(language = lang)
            }
        }
    }

    fun onSelectTheme(theme: AppTheme) {
        viewModelScope.launch {
            appPreferences.setAppTheme(theme)
        }
    }

    fun onLanguageChange(lang: String) {
        viewModelScope.launch {
            appPreferences.setLanguage(lang)
        }
    }

    fun onClearSearchHistory() {
        viewModelScope.launch {
            searchRepository.clearAllHistory()
            _uiState.value = _uiState.value.copy(historyCleared = true)
        }
    }

    fun onShowDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = true)
    }

    fun onDismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = false)
    }

    fun onDeleteAccount(userId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.deleteAccount(userId)
            onSuccess()
        }
    }

    class Factory(
        private val appPreferences: AppPreferences,
        private val authRepository: AuthRepository,
        private val searchRepository: SearchRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(appPreferences, authRepository, searchRepository) as T
        }
    }
}