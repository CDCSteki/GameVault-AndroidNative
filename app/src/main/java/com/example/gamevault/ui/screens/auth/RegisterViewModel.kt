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

data class RegisterUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    @param: StringRes val errorMessageRes: Int? = null
)

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onUsernameChange(value: String) {
        _uiState.value = _uiState.value.copy(username = value, errorMessageRes = null)
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errorMessageRes = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessageRes = null)
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value, errorMessageRes = null)
    }

    fun onTogglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isPasswordVisible = !_uiState.value.isPasswordVisible
        )
    }

    fun onToggleConfirmPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isConfirmPasswordVisible = !_uiState.value.isConfirmPasswordVisible
        )
    }

    fun onRegisterClick(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (state.username.isBlank()) {
            _uiState.value = state.copy(errorMessageRes = R.string.error_empty_username)
            return
        }
        if (state.username.length < 3) {
            _uiState.value = state.copy(errorMessageRes = R.string.error_username_too_short)
            return
        }
        if (state.email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _uiState.value = state.copy(errorMessageRes = R.string.error_invalid_email)
            return
        }
        if (state.password.length < 6) {
            _uiState.value = state.copy(errorMessageRes = R.string.error_password_too_short)
            return
        }
        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(errorMessageRes = R.string.error_passwords_no_match)
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessageRes = null)

            val result = authRepository.register(
                username = state.username.trim(),
                email = state.email.trim().lowercase(),
                password = state.password
            )

            when (result) {
                is AuthRepository.RegisterResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                }
                is AuthRepository.RegisterResult.EmailAlreadyExists -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessageRes = R.string.error_email_exists
                    )
                }
                is AuthRepository.RegisterResult.UsernameAlreadyExists -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessageRes = R.string.error_username_exists
                    )
                }
            }
        }
    }

    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(authRepository) as T
        }
    }
}