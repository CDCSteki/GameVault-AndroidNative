package com.example.gamevault.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gamevault.data.repository.AuthRepository
import com.example.gamevault.ui.screens.auth.GameVaultTextField
import com.example.gamevault.ui.theme.*

@Composable
fun ProfileScreen(
    authRepository: AuthRepository,
    onLogout: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.Factory(authRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF1A0A3D), DarkNavy)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "← PROFILE",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Avatar + Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(NeonPurple, NeonCyan)
                                )
                            )
                            .border(3.dp, NeonPurple, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(NeonPurple, CircleShape)
                            .border(2.dp, DarkNavy, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = TextPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = uiState.user?.username ?: "Loading...",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                // Level badge
                Box(
                    modifier = Modifier
                        .background(
                            color = NeonPurple.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            NeonPurple.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "LVL ${uiState.user?.level ?: 1}  ${uiState.user?.tier ?: "ROOKIE"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonPurple,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Edit Form
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Username field
                Column {
                    Text(
                        text = "Username",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    GameVaultTextField(
                        value = uiState.username,
                        onValueChange = viewModel::onUsernameChange,
                        placeholder = "Enter username",
                        leadingIcon = Icons.Default.Person
                    )
                }

                // Password field
                Column {
                    Text(
                        text = "Password",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    GameVaultTextField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChange,
                        placeholder = "••••••••••",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        isPasswordVisible = uiState.isPasswordVisible,
                        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility
                    )
                }

                // Messages
                uiState.successMessage?.let {
                    Text(
                        text = it,
                        color = StatusGreen,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                uiState.errorMessage?.let {
                    Text(
                        text = it,
                        color = StatusRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Save Button
                Button(
                    onClick = viewModel::onSaveChanges,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                    enabled = !uiState.isLoading
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(NeonPurple, Color(0xFF6A0DAD))
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = TextPrimary,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "SAVE CHANGES",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextPrimary,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                // Logout Button
                OutlinedButton(
                    onClick = { viewModel.onLogout(onLogout) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderCyan)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "LOGOUT",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Delete Account section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = 1.dp,
                            color = StatusRed.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(StatusRed.copy(alpha = 0.05f))
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = StatusRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "ACCOUNT CONTROL",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusRed,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Deactivate Account",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Permanently deletes your profile, game library, and all associated data. This action is irreversible.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = viewModel::onShowDeleteDialog,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusRed),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed)
                    ) {
                        Text(
                            text = "DELETE PROFILE",
                            style = MaterialTheme.typography.labelMedium,
                            color = StatusRed,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Delete Account Dialog
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDeleteDialog,
            containerColor = DarkCard,
            title = {
                Text("Delete Account", color = StatusRed, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure? This will permanently delete your profile and all data. This action is irreversible.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onDeleteAccount(onAccountDeleted) },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                ) {
                    Text("DELETE", color = TextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissDeleteDialog) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}