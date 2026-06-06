package com.example.gamevault.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class GameVaultColors(
    val accent: Color,
    val accentSecondary: Color,
    val background: Color,
    val backgroundSecondary: Color,
    val card: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val statusGreen: Color = StatusGreen,
    val statusOrange: Color = StatusOrange,
    val statusRed: Color = StatusRed,
    val statusYellow: Color = StatusYellow,
)


val CyberDarkColors = GameVaultColors(
    accent = NeonPurple,
    accentSecondary = NeonCyan,
    background = DarkNavy,
    backgroundSecondary = DarkNavySecondary,
    card = DarkCard,
    border = BorderCyan,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    textMuted = TextMuted,
)

val OceanBlueColors = GameVaultColors(
    accent = Color(0xFF48CAE4),
    accentSecondary = Color(0xFF90E0EF),
    background = Color(0xFF03045E),
    backgroundSecondary = Color(0xFF023E8A),
    card = Color(0xFF0077B6),
    border = Color(0xFF0096C7),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFADE8F4),
    textMuted = Color(0xFF90E0EF),
)

val ForestGreenColors = GameVaultColors(
    accent = Color(0xFF52B788),
    accentSecondary = Color(0xFFB7E4C7),
    background = Color(0xFF081C15),
    backgroundSecondary = Color(0xFF1B4332),
    card = Color(0xFF2D6A4F),
    border = Color(0xFF40916C),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFD8F3DC),
    textMuted = Color(0xFF95D5B2),
)

val SunsetColors = GameVaultColors(
    accent = Color(0xFFFF6B35),
    accentSecondary = Color(0xFFFFD166),
    background = Color(0xFF1A0A00),
    backgroundSecondary = Color(0xFF2D1500),
    card = Color(0xFF3D1F00),
    border = Color(0xFF7C3A00),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFFFD6B3),
    textMuted = Color(0xFFFF9A6C),
)

val MidnightRedColors = GameVaultColors(
    accent = Color(0xFFE63946),
    accentSecondary = Color(0xFFFF6B6B),
    background = Color(0xFF0D0305),
    backgroundSecondary = Color(0xFF1A0608),
    card = Color(0xFF2D0A0E),
    border = Color(0xFF6B1520),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFFFB3B8),
    textMuted = Color(0xFFFF6B6B),
)

val NeonGreenColors = GameVaultColors(
    accent = Color(0xFF39FF14),
    accentSecondary = Color(0xFF00FFFF),
    background = Color(0xFF050A05),
    backgroundSecondary = Color(0xFF0A140A),
    card = Color(0xFF0F1F0F),
    border = Color(0xFF1A3A1A),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFB0FFB0),
    textMuted = Color(0xFF39FF14),
)

val RoseGoldColors = GameVaultColors(
    accent = Color(0xFFE8A598),
    accentSecondary = Color(0xFFF7D6CB),
    background = Color(0xFF1A0D0A),
    backgroundSecondary = Color(0xFF2D1510),
    card = Color(0xFF3D1F18),
    border = Color(0xFF6B3028),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFFFD6CC),
    textMuted = Color(0xFFE8A598),
)

val LocalGameVaultColors = staticCompositionLocalOf { CyberDarkColors }

fun appThemeColors(theme: AppTheme): GameVaultColors = when (theme) {
    AppTheme.CYBER_DARK -> CyberDarkColors
    AppTheme.OCEAN_BLUE -> OceanBlueColors
    AppTheme.FOREST_GREEN -> ForestGreenColors
    AppTheme.SUNSET -> SunsetColors
    AppTheme.MIDNIGHT_RED -> MidnightRedColors
    AppTheme.NEON_GREEN -> NeonGreenColors
    AppTheme.ROSE_GOLD -> RoseGoldColors
}