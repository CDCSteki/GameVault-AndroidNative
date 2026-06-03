package com.example.gamevault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gamevault.ui.theme.*

@Composable
fun PasswordStrengthIndicator(password: String) {
    val strength = when {
        password.length >= 12 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isDigit() } &&
                password.any { !it.isLetterOrDigit() } -> 3
        password.length >= 8 &&
                (password.any { it.isUpperCase() } || password.any { it.isDigit() }) -> 2
        password.length >= 6 -> 1
        else -> 0
    }

    val strengthLabel = when (strength) {
        0 -> "Too weak"
        1 -> "Weak"
        2 -> "Good"
        3 -> "Strong"
        else -> ""
    }

    val strengthColor = when (strength) {
        0 -> StatusRed
        1 -> StatusOrange
        2 -> StatusYellow
        3 -> StatusGreen
        else -> GVTheme.colors.textMuted
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Password strength",
                style = MaterialTheme.typography.labelSmall,
                color = GVTheme.colors.textMuted
            )
            Text(
                text = strengthLabel,
                style = MaterialTheme.typography.labelSmall,
                color = strengthColor,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (index < strength) strengthColor
                            else GVTheme.colors.backgroundSecondary
                        )
                )
            }
        }
    }
}