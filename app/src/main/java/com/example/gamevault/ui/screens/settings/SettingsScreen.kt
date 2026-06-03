package com.example.gamevault.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.gamevault.ui.components.GameVaultTopBar
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
            // Top Bar cu icon controller
            GameVaultTopBar()

            Spacer(modifier = Modifier.height(8.dp))

            // System Preferences
            SettingsSectionCard {
                SettingsSectionHeader(
                    icon = Icons.Default.Settings,
                    title = "SYSTEM PREFERENCES"
                )

                // Theme selector
                ThemeSelector(
                    currentTheme = uiState.appTheme,
                    onThemeSelect = viewModel::onSelectTheme
                )

                HorizontalDivider(
                    color = BorderCyan.copy(alpha = 0.3f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Light mode toggle
                SettingsRow(
                    label = "Light Mode",
                    value = if (!uiState.isDarkTheme) "Enabled" else "Disabled"
                ) {
                    Switch(
                        checked = !uiState.isDarkTheme,
                        onCheckedChange = { viewModel.onToggleLightMode(it) },
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
                LanguageSelector(
                    currentLanguage = uiState.language,
                    onLanguageSelect = viewModel::onLanguageChange
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Privacy & Data
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

            // Account Control
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
                    onClick = { viewModel.onDeleteAccount(loggedInUserId, onAccountDeleted) },
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

data class ThemeOption(
    val theme: AppTheme,
    val label: String,
    val primaryColor: Color,
    val secondaryColor: Color
)

@Composable
private fun ThemeSelector(
    currentTheme: AppTheme,
    onThemeSelect: (AppTheme) -> Unit
) {
    val themes = listOf(
        ThemeOption(AppTheme.CYBER_DARK, "Cyber Dark", NeonPurple, NeonCyan),
        ThemeOption(AppTheme.OCEAN_BLUE, "Ocean Blue", OceanBlueLight, NeonCyan),
        ThemeOption(AppTheme.FOREST_GREEN, "Forest", ForestGreenLight, StatusGreen),
        ThemeOption(AppTheme.SUNSET, "Sunset", SunsetOrangeLight, StatusYellow),
        ThemeOption(AppTheme.MIDNIGHT_RED, "Midnight Red", MidnightRedLight, StatusRed)
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Display Theme",
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(themes) { option ->
                val isSelected = currentTheme == option.theme
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onThemeSelect(option.theme) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(option.primaryColor, option.secondaryColor)
                                )
                            )
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) TextPrimary else Color.Transparent,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) TextPrimary else TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageSelector(
    currentLanguage: String,
    onLanguageSelect: (String) -> Unit
) {
    val languages = listOf(
        "en" to "English (US)",
        "ro" to "Română"
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "System Language",
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            languages.forEach { (code, name) ->
                val isSelected = currentLanguage == code
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) NeonPurple.copy(alpha = 0.2f)
                            else DarkNavySecondary
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) NeonPurple else BorderCyan,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onLanguageSelect(code) }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) NeonPurple else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
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
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
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
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            Text(text = value, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        }
        action()
    }
}