package com.example.gamevault.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gamevault.data.local.preferences.AppPreferences
import com.example.gamevault.data.repository.AuthRepository
import com.example.gamevault.data.repository.SearchRepository
import com.example.gamevault.ui.theme.*

@Composable
fun SettingsScreen(
    appPreferences: AppPreferences,
    authRepository: AuthRepository,
    searchRepository: SearchRepository,
    onAccountDeleted: () -> Unit
) {
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(appPreferences, authRepository, searchRepository)
    )
    val uiState by viewModel.uiState.collectAsState()
    val loggedInUserId by authRepository.loggedInUserId.collectAsState(initial = -1)

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
            // Top Bar
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
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // System Preferences Section
            SettingsSectionCard {
                SettingsSectionHeader(
                    icon = Icons.Default.Settings,
                    title = "SYSTEM PREFERENCES"
                )

                // Dark Theme Toggle
                SettingsRow(
                    label = "Display Theme",
                    value = if (uiState.isDarkTheme) "Cyber Dark (Default)" else "Light Mode"
                ) {
                    Switch(
                        checked = uiState.isDarkTheme,
                        onCheckedChange = viewModel::onToggleTheme,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextPrimary,
                            checkedTrackColor = NeonPurple,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = DarkCard
                        )
                    )
                }

                HorizontalDivider(
                    color = BorderCyan.copy(alpha = 0.3f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Language
                SettingsRow(
                    label = "System Language",
                    value = when (uiState.language) {
                        "en" -> "English (US)"
                        "ro" -> "Romanian"
                        else -> "English (US)"
                    }
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        TextButton(onClick = { expanded = true }) {
                            Text(
                                text = when (uiState.language) {
                                    "en" -> "English (US)"
                                    "ro" -> "Romanian"
                                    else -> "English (US)"
                                },
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = TextMuted
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(DarkCard)
                        ) {
                            listOf("en" to "English (US)", "ro" to "Romanian").forEach { (code, name) ->
                                DropdownMenuItem(
                                    text = { Text(name, color = TextPrimary) },
                                    onClick = {
                                        viewModel.onLanguageChange(code)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Privacy & Data Section
            SettingsSectionCard {
                SettingsSectionHeader(
                    icon = Icons.Default.Lock,
                    title = "PRIVACY & DATA"
                )

                SettingsRow(
                    label = "Search History",
                    value = if (uiState.historyCleared) "Cleared!" else "Last cleared: Never"
                ) {
                    TextButton(onClick = viewModel::onClearSearchHistory) {
                        Text(
                            text = "Clear Now",
                            color = NeonCyan,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Account Control Section
            SettingsSectionCard(borderColor = StatusRed.copy(alpha = 0.4f)) {
                SettingsSectionHeader(
                    icon = Icons.Default.Warning,
                    title = "ACCOUNT CONTROL",
                    iconTint = StatusRed
                )

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = viewModel::onShowDeleteDialog,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusRed),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = StatusRed
                        )
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

            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // Delete Dialog
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDeleteDialog,
            containerColor = DarkCard,
            title = {
                Text("Delete Account", color = StatusRed, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure? All your data will be permanently deleted.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onDeleteAccount(loggedInUserId, onAccountDeleted)
                    },
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

@Composable
private fun SettingsSectionCard(
    borderColor: Color = BorderCyan,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .background(DarkCard),
        content = content
    )
}

@Composable
private fun SettingsSectionHeader(
    icon: ImageVector,
    title: String,
    iconTint: Color = NeonPurple
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = iconTint,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun SettingsRow(
    label: String,
    value: String,
    action: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
        action()
    }
}