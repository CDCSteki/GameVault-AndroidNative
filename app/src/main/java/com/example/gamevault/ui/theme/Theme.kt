package com.example.gamevault.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
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

@Composable
fun GameVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        GameVaultDarkColorScheme
    } else {
        GameVaultLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkNavy.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}