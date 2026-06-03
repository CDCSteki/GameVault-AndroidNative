package com.example.gamevault.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gamevault.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    isLoggedIn: Boolean,
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit
) {

    LaunchedEffect(Unit) {
        delay(2000)
        if (isLoggedIn) {
            onNavigateToHome()
        } else {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GVTheme.colors.backgroundSecondary,
                        GVTheme.colors.accent
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "GAMEVAULT",
                style = MaterialTheme.typography.displayLarge.copy(
                    brush = Brush.linearGradient(
                        colors = listOf(GVTheme.colors.accent, GVTheme.colors.accent)
                    ),
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your Gaming Universe",
                style = MaterialTheme.typography.titleMedium,
                color = GVTheme.colors.textMuted
            )
            Spacer(modifier = Modifier.height(48.dp))
            androidx.compose.material3.CircularProgressIndicator(
                color = GVTheme.colors.accent,
                modifier = Modifier.size(32.dp),
                strokeWidth = 2.dp
            )
        }
    }
}