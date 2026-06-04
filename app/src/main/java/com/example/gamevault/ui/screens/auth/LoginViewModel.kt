package com.example.gamevault.ui.screens.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gamevault.R
import com.example.gamevault.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val emailOrUsername: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    @param: StringRes val errorMessageRes: Int? = null
)

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailOrUsernameChange(value: String) {
        _uiState.value = _uiState.value.copy(
            emailOrUsername = value,
            errorMessageRes = null
        )
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(
            password = value,
            errorMessageRes = null
        )
    }

    fun onTogglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isPasswordVisible = !_uiState.value.isPasswordVisible
        )
    }

    fun onLoginClick(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (state.emailOrUsername.isBlank()) {
            _uiState.value = state.copy(errorMessageRes = R.string.error_empty_email)
            return
        }
        if (state.password.isBlank()) {
            _uiState.value = state.copy(errorMessageRes = R.string.error_empty_password)
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessageRes = null)

            val result = authRepository.login(
                emailOrUsername = state.emailOrUsername.trim(),
                password = state.password
            )

            when (result) {
                is AuthRepository.LoginResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                }
                is AuthRepository.LoginResult.InvalidCredentials -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessageRes = R.string.error_invalid_credentials
                    )
                }
            }
        }
    }

    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(authRepository) as T
        }
    }
}