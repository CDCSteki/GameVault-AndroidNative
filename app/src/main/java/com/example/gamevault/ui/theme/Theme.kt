package com.example.gamevault.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val GameVaultDarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = TextPrimary,
    primaryContainer = DarkCard,
    onPrimaryContainer = TextPrimary,
    secondary = NeonCyan,
    onSecondary = DarkNavy,
    secondaryContainer = DarkCardSecondary,
    onSecondaryContainer = TextPrimary,
    tertiary = NeonPurpleLight,
    onTertiary = DarkNavy,
    background = DarkNavy,
    onBackground = TextPrimary,
    surface = DarkNavySecondary,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = BorderCyan,
    error = StatusRed,
    onError = TextPrimary
)

private val GameVaultLightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = TextPrimary,
    background = LightBackground,
    onBackground = DarkNavy,
    surface = LightSurface,
    onSurface = DarkNavy,
    secondary = NeonCyanDark,
    onSecondary = TextPrimary,
    error = StatusRed,
    onError = TextPrimary
)

private val OceanBlueColorScheme = darkColorScheme(
    primary = OceanBlueLight,
    onPrimary = TextPrimary,
    primaryContainer = DarkCard,
    onPrimaryContainer = TextPrimary,
    secondary = NeonCyan,
    onSecondary = DarkNavy,
    background = DarkNavy,
    onBackground = TextPrimary,
    surface = DarkNavySecondary,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = OceanBlue,
    error = StatusRed,
    onError = TextPrimary
)

private val ForestGreenColorScheme = darkColorScheme(
    primary = ForestGreenLight,
    onPrimary = TextPrimary,
    primaryContainer = DarkCard,
    onPrimaryContainer = TextPrimary,
    secondary = StatusGreen,
    onSecondary = DarkNavy,
    background = DarkNavy,
    onBackground = TextPrimary,
    surface = DarkNavySecondary,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = ForestGreen,
    error = StatusRed,
    onError = TextPrimary
)

private val SunsetColorScheme = darkColorScheme(
    primary = SunsetOrangeLight,
    onPrimary = TextPrimary,
    primaryContainer = DarkCard,
    onPrimaryContainer = TextPrimary,
    secondary = StatusYellow,
    onSecondary = DarkNavy,
    background = DarkNavy,
    onBackground = TextPrimary,
    surface = DarkNavySecondary,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = SunsetOrange,
    error = StatusRed,
    onError = TextPrimary
)

private val MidnightRedColorScheme = darkColorScheme(
    primary = MidnightRedLight,
    onPrimary = TextPrimary,
    primaryContainer = DarkCard,
    onPrimaryContainer = TextPrimary,
    secondary = StatusRed,
    onSecondary = DarkNavy,
    background = DarkNavy,
    onBackground = TextPrimary,
    surface = DarkNavySecondary,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = MidnightRed,
    error = StatusRed,
    onError = TextPrimary
)

@Composable
fun GameVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    appTheme: AppTheme = AppTheme.CYBER_DARK,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        !darkTheme -> GameVaultLightColorScheme
        else -> when (appTheme) {
            AppTheme.CYBER_DARK -> GameVaultDarkColorScheme
            AppTheme.OCEAN_BLUE -> OceanBlueColorScheme
            AppTheme.FOREST_GREEN -> ForestGreenColorScheme
            AppTheme.SUNSET -> SunsetColorScheme
            AppTheme.MIDNIGHT_RED -> MidnightRedColorScheme
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}