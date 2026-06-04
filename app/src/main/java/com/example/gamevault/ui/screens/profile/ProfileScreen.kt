package com.example.gamevault.ui.screens.profile

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.gamevault.R
import com.example.gamevault.data.repository.AuthRepository
import com.example.gamevault.ui.components.GameVaultTextField
import com.example.gamevault.ui.components.GameVaultTopBar
import com.example.gamevault.ui.components.PasswordStrengthIndicator
import com.example.gamevault.ui.theme.*
import com.example.gamevault.ui.util.createImageUri
import kotlinx.coroutines.delay
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authRepository: AuthRepository,
    onLogout: () -> Unit
) {
    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.Factory(authRepository)
    )
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.successMessageRes, uiState.errorMessageRes) {
        if (uiState.successMessageRes != null || uiState.errorMessageRes != null) {
            delay(5000)
            viewModel.clearMessages()
        }
    }

    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.onImageSelected(context, it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraUri?.let { viewModel.onImageSelected(context, it) }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createImageUri(context)
            cameraUri = uri
            cameraLauncher.launch(uri)
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GVTheme.colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            GameVaultTopBar()

            AvatarSection(
                profilePictureUri = uiState.profilePictureUri,
                username = uiState.user?.username ?: "",
                level = uiState.user?.level ?: 1,
                tier = uiState.user?.tier ?: "ROOKIE",
                onEditClick = viewModel::onShowImagePickerDialog
            )

            Spacer(modifier = Modifier.height(8.dp))

            uiState.successMessageRes?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(StatusGreen.copy(alpha = 0.15f))
                        .border(1.dp, StatusGreen, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StatusGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(it),
                            color = StatusGreen,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            uiState.errorMessageRes?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(StatusRed.copy(alpha = 0.15f))
                        .border(1.dp, StatusRed, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = StatusRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(it),
                            color = StatusRed,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            ProfileSectionCard(title = stringResource(R.string.profile_username_label).uppercase()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GameVaultTextField(
                        value = uiState.username,
                        onValueChange = viewModel::onUsernameChange,
                        placeholder = stringResource(R.string.enter_username),
                        leadingIcon = Icons.Default.Person
                    )
                    GradientButton(
                        text = stringResource(R.string.update_username),
                        onClick = viewModel::onSaveUsername,
                        isLoading = uiState.isLoading
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ProfileSectionCard(title = stringResource(R.string.profile_change_password)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.profile_current_password_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = GVTheme.colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        GameVaultTextField(
                            value = uiState.currentPassword,
                            onValueChange = viewModel::onCurrentPasswordChange,
                            placeholder = stringResource(R.string.profile_current_password_placeholder),
                            leadingIcon = Icons.Default.Lock,
                            isPassword = true,
                            isPasswordVisible = uiState.isCurrentPasswordVisible,
                            onTogglePasswordVisibility = viewModel::onToggleCurrentPasswordVisibility
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.profile_new_password_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = GVTheme.colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        GameVaultTextField(
                            value = uiState.newPassword,
                            onValueChange = viewModel::onNewPasswordChange,
                            placeholder = stringResource(R.string.register_password_placeholder),
                            leadingIcon = Icons.Default.Lock,
                            isPassword = true,
                            isPasswordVisible = uiState.isNewPasswordVisible,
                            onTogglePasswordVisibility = viewModel::onToggleNewPasswordVisibility
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.profile_confirm_new_password_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = GVTheme.colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        GameVaultTextField(
                            value = uiState.confirmNewPassword,
                            onValueChange = viewModel::onConfirmNewPasswordChange,
                            placeholder = stringResource(R.string.library_confirm_password_placeholder),
                            leadingIcon = Icons.Default.Lock,
                            isPassword = true,
                            isPasswordVisible = uiState.isConfirmPasswordVisible,
                            onTogglePasswordVisibility = viewModel::onToggleConfirmPasswordVisibility
                        )
                    }

                    if (uiState.newPassword.isNotEmpty()) {
                        PasswordStrengthIndicator(password = uiState.newPassword)
                    }

                    GradientButton(
                        text = stringResource(R.string.profile_update_password),
                        onClick = viewModel::onSavePassword,
                        isLoading = uiState.isLoading
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.onLogout(onLogout) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GVTheme.colors.border)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = GVTheme.colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.profile_logout),
                            style = MaterialTheme.typography.labelLarge,
                            color = GVTheme.colors.textSecondary,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    if (uiState.showImagePickerDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissImagePickerDialog,
            containerColor = GVTheme.colors.card,
            title = {
                Text(
                    stringResource(R.string.profile_change_picture),
                    color = GVTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(GVTheme.colors.backgroundSecondary)
                            .clickable {
                                viewModel.onDismissImagePickerDialog()
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = GVTheme.colors.accentSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.take_photo),
                                style = MaterialTheme.typography.titleSmall,
                                color = GVTheme.colors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.use_your_camera),
                                style = MaterialTheme.typography.bodySmall,
                                color = GVTheme.colors.textMuted
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(GVTheme.colors.backgroundSecondary)
                            .clickable {
                                viewModel.onDismissImagePickerDialog()
                                galleryLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = GVTheme.colors.accent,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.choose_from_gallery),
                                style = MaterialTheme.typography.titleSmall,
                                color = GVTheme.colors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.pick_from_your_photos),
                                style = MaterialTheme.typography.bodySmall,
                                color = GVTheme.colors.textMuted
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = viewModel::onDismissImagePickerDialog) {
                    Text(stringResource(R.string.delete_dialog_cancel), color = GVTheme.colors.textSecondary)
                }
            }
        )
    }
}
@Composable
private fun AvatarSection(
    profilePictureUri: String?,
    username: String,
    level: Int,
    tier: String,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(
                        width = 3.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(GVTheme.colors.accent, GVTheme.colors.accentSecondary)
                        ),
                        shape = CircleShape
                    )
                    .background(GVTheme.colors.card),
                contentAlignment = Alignment.Center
            ) {
                if (profilePictureUri != null) {
                    val imageModel = if (profilePictureUri.startsWith("/")) {
                        File(profilePictureUri)
                    } else {
                        profilePictureUri
                    }
                    AsyncImage(
                        model = imageModel,
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = GVTheme.colors.textSecondary,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(GVTheme.colors.accent, GVTheme.colors.accentSecondary)
                        ),
                        shape = CircleShape
                    )
                    .border(2.dp, GVTheme.colors.background, CircleShape)
                    .clickable(onClick = onEditClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Change Photo",
                    tint = GVTheme.colors.textPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = username,
            style = MaterialTheme.typography.headlineSmall,
            color = GVTheme.colors.textPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .background(
                    color = GVTheme.colors.accent.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    1.dp,
                    GVTheme.colors.accent.copy(alpha = 0.5f),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = "LVL $level  •  $tier",
                style = MaterialTheme.typography.labelSmall,
                color = GVTheme.colors.accent,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, GVTheme.colors.border, RoundedCornerShape(12.dp))
            .background(GVTheme.colors.card)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(16.dp)
                    .background(GVTheme.colors.accent, RoundedCornerShape(2.dp))
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = GVTheme.colors.accent,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
        HorizontalDivider(color = GVTheme.colors.border.copy(alpha = 0.3f))
        content()
    }
}

@Composable
private fun GradientButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp),
        enabled = !isLoading
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(GVTheme.colors.accent, GVTheme.colors.accentSecondary)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = GVTheme.colors.textPrimary,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = GVTheme.colors.textPrimary,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}