package com.example.gamevault.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gamevault.data.local.entity.UserEntity
import com.example.gamevault.data.repository.AuthRepository
import com.example.gamevault.ui.util.copyImageToInternalStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: UserEntity? = null,
    val username: String = "",
    // Password fields
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmNewPassword: String = "",
    val isCurrentPasswordVisible: Boolean = false,
    val isNewPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    // Profile picture
    val profilePictureUri: String? = null,
    val showImagePickerDialog: Boolean = false,
    // Loading / messages
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
                            username = user?.username ?: "",
                            profilePictureUri = user?.profilePictureUri
                        )
                    }
                }
            }
        }
    }

    // --- USERNAME ---
    fun onUsernameChange(value: String) {
        _uiState.value = _uiState.value.copy(
            username = value,
            errorMessage = null,
            successMessage = null
        )
    }

    // --- PASSWORD ---
    fun onCurrentPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(
            currentPassword = value,
            errorMessage = null,
            successMessage = null
        )
    }

    fun onNewPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(
            newPassword = value,
            errorMessage = null,
            successMessage = null
        )
    }

    fun onConfirmNewPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(
            confirmNewPassword = value,
            errorMessage = null,
            successMessage = null
        )
    }

    fun onToggleCurrentPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isCurrentPasswordVisible = !_uiState.value.isCurrentPasswordVisible
        )
    }

    fun onToggleNewPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isNewPasswordVisible = !_uiState.value.isNewPasswordVisible
        )
    }

    fun onToggleConfirmPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isConfirmPasswordVisible = !_uiState.value.isConfirmPasswordVisible
        )
    }

    // --- SAVE USERNAME ---
    fun onSaveUsername() {
        val state = _uiState.value
        val user = state.user ?: return

        if (state.username.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Username cannot be empty")
            return
        }
        if (state.username.length < 3) {
            _uiState.value = state.copy(errorMessage = "Username must be at least 3 characters")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            val result = authRepository.updateProfile(
                userId = user.id,
                username = state.username.trim(),
                profilePictureUri = state.profilePictureUri
            )
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                successMessage = when (result) {
                    AuthRepository.UpdateProfileResult.Success -> "Username updated!"
                    else -> null
                },
                errorMessage = when (result) {
                    AuthRepository.UpdateProfileResult.UsernameAlreadyExists -> "Username already taken"
                    AuthRepository.UpdateProfileResult.UserNotFound -> "User not found"
                    else -> null
                }
            )
        }
    }

    // --- SAVE PASSWORD ---
    fun onSavePassword() {
        val state = _uiState.value
        val user = state.user ?: return

        if (state.currentPassword.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Enter your current password")
            return
        }
        if (state.newPassword.length < 6) {
            _uiState.value = state.copy(errorMessage = "New password must be at least 6 characters")
            return
        }
        if (state.newPassword != state.confirmNewPassword) {
            _uiState.value = state.copy(errorMessage = "New passwords do not match")
            return
        }
        if (state.currentPassword == state.newPassword) {
            _uiState.value = state.copy(errorMessage = "New password must be different")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            val result = authRepository.updatePassword(
                userId = user.id,
                currentPassword = state.currentPassword,
                newPassword = state.newPassword
            )
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                currentPassword = "",
                newPassword = "",
                confirmNewPassword = "",
                successMessage = when (result) {
                    AuthRepository.UpdatePasswordResult.Success -> "Password updated successfully!"
                    else -> null
                },
                errorMessage = when (result) {
                    AuthRepository.UpdatePasswordResult.WrongCurrentPassword -> "Current password is incorrect"
                    AuthRepository.UpdatePasswordResult.UserNotFound -> "User not found"
                    else -> null
                }
            )
        }
    }

    // --- PROFILE PICTURE ---
    fun onShowImagePickerDialog() {
        _uiState.value = _uiState.value.copy(showImagePickerDialog = true)
    }

    fun onDismissImagePickerDialog() {
        _uiState.value = _uiState.value.copy(showImagePickerDialog = false)
    }

    fun onImageSelected(context: Context, uri: Uri) {
        viewModelScope.launch {
            val savedPath = copyImageToInternalStorage(context, uri)
            if (savedPath != null) {
                _uiState.value = _uiState.value.copy(
                    profilePictureUri = savedPath,
                    showImagePickerDialog = false
                )
                val user = _uiState.value.user ?: return@launch
                authRepository.updateProfile(
                    userId = user.id,
                    username = user.username,
                    profilePictureUri = savedPath
                )
            }
        }
    }

    // --- LOGOUT ---
    fun onLogout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onSuccess()
        }
    }

    // --- CLEAR MESSAGE ---
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            errorMessage = null
        )
    }

    class Factory(
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(authRepository) as T
        }
    }
}