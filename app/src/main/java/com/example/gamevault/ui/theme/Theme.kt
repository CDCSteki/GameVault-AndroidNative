package com.example.gamevault.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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

@Composable
fun GameVaultTheme(
    appTheme: AppTheme = AppTheme.CYBER_DARK,
    content: @Composable () -> Unit
) {
    val gvColors = appThemeColors(appTheme)

    val colorScheme = darkM3Scheme(gvColors.accent, gvColors.accentSecondary, gvColors.background, gvColors.backgroundSecondary, gvColors.card, gvColors.border)


    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
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