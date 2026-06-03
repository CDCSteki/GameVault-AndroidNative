package com.example.gamevault.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ---- Material3 color schemes (folosite de componente M3 standard) ----

private fun darkM3Scheme(accent: Color, accentSec: Color, bg: Color, bgSec: Color, card: Color, border: Color) =
    darkColorScheme(
        primary = accent,
        onPrimary = TextPrimary,
        primaryContainer = card,
        onPrimaryContainer = TextPrimary,
        secondary = accentSec,
        onSecondary = bg,
        secondaryContainer = card,
        onSecondaryContainer = TextPrimary,
        tertiary = accent.copy(alpha = 0.7f),
        onTertiary = bg,
        background = bg,
        onBackground = TextPrimary,
        surface = bgSec,
        onSurface = TextPrimary,
        surfaceVariant = card,
        onSurfaceVariant = TextSecondary,
        outline = border,
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
    appTheme: AppTheme = AppTheme.CYBER_DARK,
    content: @Composable () -> Unit
) {
    val gvColors = if (!darkTheme) CyberDarkColors else appThemeColors(appTheme)

    val colorScheme = if (!darkTheme) {
        GameVaultLightColorScheme
    } else {
        val c = gvColors
        darkM3Scheme(c.accent, c.accentSecondary, c.background, c.backgroundSecondary, c.card, c.border)
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

    CompositionLocalProvider(LocalGameVaultColors provides gvColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object GVTheme {
    val colors: GameVaultColors
        @Composable get() = LocalGameVaultColors.current
}