package com.example.gamevault.ui.screens.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "splash_alpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
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
                        Color(0xFF1A0A3D),
                        DarkNavy
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alphaAnim)
        ) {
            Text(
                text = "GAMEVAULT",
                style = MaterialTheme.typography.displayLarge.copy(
                    brush = Brush.linearGradient(
                        colors = listOf(NeonPurple, NeonCyan)
                    ),
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your Gaming Universe",
                style = MaterialTheme.typography.titleMedium,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(48.dp))
            androidx.compose.material3.CircularProgressIndicator(
                color = NeonPurple,
                modifier = Modifier.size(32.dp),
                strokeWidth = 2.dp
            )
        }
    }
}