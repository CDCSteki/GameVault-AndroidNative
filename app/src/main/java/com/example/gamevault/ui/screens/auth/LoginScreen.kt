package com.example.gamevault.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gamevault.data.repository.AuthRepository
import com.example.gamevault.ui.components.GameVaultTextField
import com.example.gamevault.ui.theme.*


@Composable
fun LoginScreen(
    authRepository: AuthRepository,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val viewModel: LoginViewModel = viewModel(
        factory = LoginViewModel.Factory(authRepository)
    )
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GVTheme.colors.backgroundSecondary,
                        GVTheme.colors.background
                    ),
                    center = Offset(0.5f, 0.3f),
                    radius = 1000f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Card container cu border cyan
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(GVTheme.colors.accentSecondary, GVTheme.colors.accent)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .background(
                        color = Color(0xFF0D1726).copy(alpha = 0.95f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 28.dp, vertical = 40.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo
                    Text(
                        text = "GAMEVAULT",
                        style = MaterialTheme.typography.displayMedium.copy(
                            brush = Brush.linearGradient(
                                colors = listOf(GVTheme.colors.accent, GVTheme.colors.accentSecondary)
                            ),
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 3.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Welcome back, Hunter",
                        style = MaterialTheme.typography.titleMedium,
                        color = GVTheme.colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(36.dp))

                    // Email / Username field
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Email or Username",
                            style = MaterialTheme.typography.labelMedium,
                            color = GVTheme.colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        GameVaultTextField(
                            value = uiState.emailOrUsername,
                            onValueChange = viewModel::onEmailOrUsernameChange,
                            placeholder = "Enter your credentials",
                            leadingIcon = Icons.Default.Person,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Password field
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Password",
                                style = MaterialTheme.typography.labelMedium,
                                color = GVTheme.colors.textSecondary
                            )
                            TextButton(
                                onClick = { /* TODO: Forgot password */ },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = "Forgot Password?",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = GVTheme.colors.accent
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        GameVaultTextField(
                            value = uiState.password,
                            onValueChange = viewModel::onPasswordChange,
                            placeholder = "••••••••",
                            leadingIcon = Icons.Default.Lock,
                            isPassword = true,
                            isPasswordVisible = uiState.isPasswordVisible,
                            onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    viewModel.onLoginClick(onLoginSuccess)
                                }
                            )
                        )
                    }

                    // Error message
                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = uiState.errorMessage!!,
                            color = StatusRed,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Login Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.onLoginClick(onLoginSuccess)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp),
                        enabled = !uiState.isLoading
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(GVTheme.colors.accent, GVTheme.colors.accentSecondary)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    color = GVTheme.colors.textPrimary,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "LOGIN",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = GVTheme.colors.textPrimary,
                                    letterSpacing = 2.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Register link
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Don't have an account? ",
                            style = MaterialTheme.typography.bodySmall,
                            color = GVTheme.colors.textSecondary
                        )
                        TextButton(
                            onClick = onNavigateToRegister,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Sign Up",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GVTheme.colors.accent
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}