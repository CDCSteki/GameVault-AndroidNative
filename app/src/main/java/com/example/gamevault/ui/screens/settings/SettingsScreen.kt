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

    // Accesăm culorile temei curente
    val colors = GVTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            GameVaultTopBar()

            Spacer(modifier = Modifier.height(8.dp))

            // System Preferences
            SettingsSectionCard(borderColor = colors.border) {
                SettingsSectionHeader(
                    icon = Icons.Default.Settings,
                    title = "SYSTEM PREFERENCES",
                    iconTint = colors.accent
                )

                // Theme selector
                ThemeSelector(
                    currentTheme = uiState.appTheme,
                    onThemeSelect = viewModel::onSelectTheme,
                    colors = colors
                )

                HorizontalDivider(
                    color = colors.border.copy(alpha = 0.3f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Language
                LanguageSelector(
                    currentLanguage = uiState.language,
                    onLanguageSelect = viewModel::onLanguageChange,
                    colors = colors
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Privacy & Data
            SettingsSectionCard(borderColor = colors.border) {
                SettingsSectionHeader(
                    icon = Icons.Default.Lock,
                    title = "PRIVACY & DATA",
                    iconTint = colors.accent
                )
                SettingsRow(
                    label = "Search History",
                    value = if (uiState.historyCleared) "Cleared!" else "Last cleared: Never",
                    colors = colors
                ) {
                    TextButton(onClick = viewModel::onClearSearchHistory) {
                        Text(
                            text = "Clear Now",
                            color = colors.accentSecondary,
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
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Permanently deletes your profile, game library, and all associated data. This action is irreversible.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
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
            containerColor = colors.card,
            title = {
                Text("Delete Account", color = StatusRed, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure? All your data will be permanently deleted.",
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onDeleteAccount(loggedInUserId, onAccountDeleted) },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                ) {
                    Text("DELETE", color = colors.textPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissDeleteDialog) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        )
    }
}

// ---- Datele pentru fiecare temă afișată în selector ----
data class ThemeOption(
    val theme: AppTheme,
    val label: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val emoji: String
)

private val allThemeOptions = listOf(
    ThemeOption(AppTheme.CYBER_DARK,    "Cyber",    NeonPurple,          NeonCyan,            "🌌"),
    ThemeOption(AppTheme.OCEAN_BLUE,    "Ocean",    Color(0xFF48CAE4),   Color(0xFF90E0EF),   "🌊"),
    ThemeOption(AppTheme.FOREST_GREEN,  "Forest",   Color(0xFF52B788),   Color(0xFFB7E4C7),   "🌿"),
    ThemeOption(AppTheme.SUNSET,        "Sunset",   Color(0xFFFF6B35),   Color(0xFFFFD166),   "🌅"),
    ThemeOption(AppTheme.MIDNIGHT_RED,  "Blood",    Color(0xFFE63946),   Color(0xFFFF6B6B),   "🔴"),
    ThemeOption(AppTheme.NEON_GREEN,    "Matrix",   Color(0xFF39FF14),   Color(0xFF00FFFF),   "💚"),
    ThemeOption(AppTheme.ROSE_GOLD,     "Rose",     Color(0xFFE8A598),   Color(0xFFF7D6CB),   "🌸"),
)

@Composable
private fun ThemeSelector(
    currentTheme: AppTheme,
    onThemeSelect: (AppTheme) -> Unit,
    colors: GameVaultColors
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Display Theme",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(allThemeOptions) { option ->
                val isSelected = currentTheme == option.theme
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onThemeSelect(option.theme) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(option.primaryColor, option.secondaryColor)
                                )
                            )
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) colors.textPrimary
                                else Color.Transparent,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(
                                text = option.emoji,
                                fontSize = 22.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) colors.accent else colors.textMuted,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageSelector(
    currentLanguage: String,
    onLanguageSelect: (String) -> Unit,
    colors: GameVaultColors
) {
    // Perechi: cod ISO -> (numeFlagEmoji, numeAfisat)
    val languages = listOf(
        "en" to ("🇬🇧" to "English"),
        "ro" to ("🇷🇴" to "Română"),
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "System Language",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            languages.forEach { (code, meta) ->
                val (flag, name) = meta
                val isSelected = currentLanguage == code
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) colors.accent.copy(alpha = 0.2f)
                            else colors.backgroundSecondary
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) colors.accent else colors.border,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onLanguageSelect(code) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = flag, fontSize = 18.sp)
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) colors.accent else colors.textSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    borderColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = GVTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
            .background(colors.card),
        content = content
    )
}

@Composable
private fun SettingsSectionHeader(
    icon: ImageVector,
    title: String,
    iconTint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
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
    colors: GameVaultColors,
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
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
            Text(text = value, style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
        }
        action()
    }
}