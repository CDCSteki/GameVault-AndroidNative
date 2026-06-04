// SettingsScreen.kt
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gamevault.R
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

            SettingsSectionCard(borderColor = colors.border) {
                SettingsSectionHeader(
                    icon = Icons.Default.Settings,
                    title = stringResource(R.string.settings_system_prefs),
                    iconTint = colors.accent
                )

                ThemeSelector(
                    currentTheme = uiState.appTheme,
                    onThemeSelect = viewModel::onSelectTheme,
                    colors = colors
                )

                HorizontalDivider(
                    color = colors.border.copy(alpha = 0.3f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                LanguageSelector(
                    currentLanguage = uiState.language,
                    onLanguageSelect = viewModel::onLanguageChange,
                    colors = colors
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            SettingsSectionCard(borderColor = colors.border) {
                SettingsSectionHeader(
                    icon = Icons.Default.Lock,
                    title = stringResource(R.string.settings_privacy),
                    iconTint = colors.accent
                )
                SettingsRow(
                    label = stringResource(R.string.settings_search_history),
                    value = if (uiState.historyCleared) stringResource(R.string.settings_history_cleared) else stringResource(R.string.settings_history_never),
                    colors = colors
                ) {
                    TextButton(onClick = viewModel::onClearSearchHistory) {
                        Text(
                            text = stringResource(R.string.settings_clear_now),
                            color = colors.accentSecondary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            SettingsSectionCard(borderColor = StatusRed.copy(alpha = 0.4f)) {
                SettingsSectionHeader(
                    icon = Icons.Default.Warning,
                    title = stringResource(R.string.settings_account_control),
                    iconTint = StatusRed
                )
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_deactivate_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.settings_deactivate_desc),
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
                            text = stringResource(R.string.settings_delete_profile),
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

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDeleteDialog,
            containerColor = colors.card,
            title = {
                Text(stringResource(R.string.delete_dialog_title), color = StatusRed, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    stringResource(R.string.delete_dialog_message),
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onDeleteAccount(loggedInUserId, onAccountDeleted) },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                ) {
                    Text(stringResource(R.string.delete_dialog_confirm), color = colors.textPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissDeleteDialog) {
                    Text(stringResource(R.string.delete_dialog_cancel), color = colors.textSecondary)
                }
            }
        )
    }
}

data class ThemeOption(
    val theme: AppTheme,
    val labelResId: Int,
    val primaryColor: Color,
    val secondaryColor: Color,
    val emoji: String
)

@Composable
private fun getThemeOptions(): List<ThemeOption> = listOf(
    ThemeOption(AppTheme.CYBER_DARK,    R.string.theme_cyber,    NeonPurple,          NeonCyan,            "🌌"),
    ThemeOption(AppTheme.OCEAN_BLUE,    R.string.theme_ocean,    Color(0xFF48CAE4),   Color(0xFF90E0EF),   "🌊"),
    ThemeOption(AppTheme.FOREST_GREEN,  R.string.theme_forest,   Color(0xFF52B788),   Color(0xFFB7E4C7),   "🌿"),
    ThemeOption(AppTheme.SUNSET,        R.string.theme_sunset,   Color(0xFFFF6B35),   Color(0xFFFFD166),   "🌅"),
    ThemeOption(AppTheme.MIDNIGHT_RED,  R.string.theme_blood,    Color(0xFFE63946),   Color(0xFFFF6B6B),   "🔴"),
    ThemeOption(AppTheme.NEON_GREEN,    R.string.theme_matrix,   Color(0xFF39FF14),   Color(0xFF00FFFF),   "💚"),
    ThemeOption(AppTheme.ROSE_GOLD,     R.string.theme_rose,     Color(0xFFE8A598),   Color(0xFFF7D6CB),   "🌸"),
)

@Composable
private fun ThemeSelector(
    currentTheme: AppTheme,
    onThemeSelect: (AppTheme) -> Unit,
    colors: GameVaultColors
) {
    val themeOptions = getThemeOptions()

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = stringResource(R.string.settings_display_theme),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(themeOptions) { option ->
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
                        text = stringResource(option.labelResId),
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
    val languages = listOf(
        "en" to ("🇬🇧" to stringResource(R.string.settings_language_en)),
        "ro" to ("🇷🇴" to stringResource(R.string.settings_language_ro)),
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = stringResource(R.string.settings_language),
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