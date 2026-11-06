package com.example.codetrack.ui.screens

import androidx.compose.runtime.Composable
import com.example.codetrack.ui.screens.auth.LoginScreen as AuthLoginScreen

@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    AuthLoginScreen(
        onLogin = onLogin,
        onRegisterClick = onRegisterClick,
        onForgotPasswordClick = onForgotPasswordClick,
        isLoading = isLoading,
        errorMessage = errorMessage
    )
}
