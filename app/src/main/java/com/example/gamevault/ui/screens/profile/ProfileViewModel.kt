package com.example.gamevault.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gamevault.data.local.entity.UserEntity
import com.example.gamevault.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: UserEntity? = null,
    val username: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val showDeleteDialog: Boolean = false
)

class ProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            authRepository.loggedInUserId.collect { userId ->
                if (userId != -1) {
                    authRepository.getUserById(userId).collect { user ->
                        _uiState.value = _uiState.value.copy(
                            user = user,
                            username = user?.username ?: ""
                        )
                    }
                }
            }
        }
    }

    fun onUsernameChange(value: String) {
        _uiState.value = _uiState.value.copy(
            username = value,
            errorMessage = null,
            successMessage = null
        )
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(
            password = value,
            errorMessage = null,
            successMessage = null
        )
    }

    fun onTogglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isPasswordVisible = !_uiState.value.isPasswordVisible
        )
    }

    fun onSaveChanges() {
        val state = _uiState.value
        val user = state.user ?: return

        if (state.username.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Username cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)

            val result = authRepository.updateProfile(
                userId = user.id,
                username = state.username.trim(),
                profilePictureUri = user.profilePictureUri
            )

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                successMessage = when (result) {
                    AuthRepository.UpdateProfileResult.Success -> "Profile updated successfully!"
                    AuthRepository.UpdateProfileResult.UsernameAlreadyExists -> null
                    AuthRepository.UpdateProfileResult.UserNotFound -> null
                },
                errorMessage = when (result) {
                    AuthRepository.UpdateProfileResult.Success -> null
                    AuthRepository.UpdateProfileResult.UsernameAlreadyExists -> "Username already taken"
                    AuthRepository.UpdateProfileResult.UserNotFound -> "User not found"
                }
            )
        }
    }

    fun onLogout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onSuccess()
        }
    }

    fun onShowDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = true)
    }

    fun onDismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = false)
    }

    fun onDeleteAccount(onSuccess: () -> Unit) {
        val userId = _uiState.value.user?.id ?: return
        viewModelScope.launch {
            authRepository.deleteAccount(userId)
            onSuccess()
        }
    }

    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(authRepository) as T
        }
    }
}