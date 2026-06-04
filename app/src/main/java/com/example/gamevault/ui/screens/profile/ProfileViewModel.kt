package com.example.gamevault.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gamevault.R
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
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmNewPassword: String = "",
    val isCurrentPasswordVisible: Boolean = false,
    val isNewPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val profilePictureUri: String? = null,
    val showImagePickerDialog: Boolean = false,
    val isLoading: Boolean = false,
    @param:StringRes val successMessageRes: Int? = null,
    @param:StringRes val errorMessageRes: Int? = null,
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

    fun onUsernameChange(value: String) {
        _uiState.value = _uiState.value.copy(
            username = value,
            errorMessageRes = null,
            successMessageRes = null
        )
    }

    fun onCurrentPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(
            currentPassword = value,
            errorMessageRes = null,
            successMessageRes = null
        )
    }

    fun onNewPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(
            newPassword = value,
            errorMessageRes = null,
            successMessageRes = null
        )
    }

    fun onConfirmNewPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(
            confirmNewPassword = value,
            errorMessageRes = null,
            successMessageRes = null
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

    fun onSaveUsername() {
        val state = _uiState.value
        val user = state.user ?: return

        if (state.username.isBlank()) {
            _uiState.value = state.copy(errorMessageRes = R.string.profile_error_empty_username)
            return
        }
        if (state.username.length < 3) {
            _uiState.value = state.copy(errorMessageRes = R.string.error_username_too_short)
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
                successMessageRes = when (result) {
                    AuthRepository.UpdateProfileResult.Success -> R.string.profile_updated
                    else -> null
                },
                errorMessageRes = when (result) {
                    AuthRepository.UpdateProfileResult.UsernameAlreadyExists -> R.string.error_username_exists
                    AuthRepository.UpdateProfileResult.UserNotFound -> R.string.error_user_not_found
                    else -> null
                }
            )
        }
    }

    fun onSavePassword() {
        val state = _uiState.value
        val user = state.user ?: return

        if (state.currentPassword.isBlank()) {
            _uiState.value = state.copy(errorMessageRes = R.string.error_empty_current_password)
            return
        }
        if (state.newPassword.length < 6) {
            _uiState.value = state.copy(errorMessageRes = R.string.error_password_too_short)
            return
        }
        if (state.newPassword != state.confirmNewPassword) {
            _uiState.value = state.copy(errorMessageRes = R.string.error_passwords_no_match)
            return
        }
        if (state.currentPassword == state.newPassword) {
            _uiState.value = state.copy(errorMessageRes = R.string.error_password_same)
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
                successMessageRes = when (result) {
                    AuthRepository.UpdatePasswordResult.Success -> R.string.profile_password_updated
                    else -> null
                },
                errorMessageRes = when (result) {
                    AuthRepository.UpdatePasswordResult.WrongCurrentPassword -> R.string.error_wrong_current_password
                    AuthRepository.UpdatePasswordResult.UserNotFound -> R.string.error_user_not_found
                    else -> null
                }
            )
        }
    }

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

    fun onLogout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onSuccess()
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessageRes = null,
            errorMessageRes = null
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