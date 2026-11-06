package com.example.codetrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codetrack.ui.MainScreen
import com.example.codetrack.ui.screens.ForgotPasswordScreen
import com.example.codetrack.ui.screens.LoginScreen
import com.example.codetrack.ui.screens.RegisterScreen
import com.example.codetrack.ui.theme.CodeTrackTheme
import com.example.codetrack.ui.viewmodel.AuthUiState
import com.example.codetrack.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModel.provideFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var darkTheme by rememberSaveable {
                mutableStateOf(false)
            }

            var currentScreen by rememberSaveable {
                mutableStateOf("splash")
            }

            val loginState by authViewModel.loginState.collectAsState()
            val registerState by authViewModel.registerState.collectAsState()
            val forgotPasswordState by authViewModel.forgotPasswordState.collectAsState()
            val resetPasswordState by authViewModel.resetPasswordState.collectAsState()
            val sessionState by authViewModel.sessionState.collectAsState()

            // App Startup: Restore saved session through /api/auth/me
            LaunchedEffect(Unit) {
                if (currentScreen == "splash") {
                    authViewModel.restoreSession()
                }
            }

            // React to session restoration state
            LaunchedEffect(sessionState) {
                when (sessionState) {
                    is AuthUiState.Success -> {
                        if (currentScreen == "splash") {
                            currentScreen = "main"
                        }
                    }
                    is AuthUiState.Error -> {
                        if (currentScreen == "splash") {
                            // If no session exists or 401 cleared the session, transition to login
                            if (!authViewModel.isLoggedIn) {
                                currentScreen = "login"
                            }
                            // If user IS still logged in locally (network error during /me),
                            // keep on "splash" to present the recoverable retry state.
                        }
                    }
                    is AuthUiState.Loading, AuthUiState.Idle -> {
                        // Check in flight
                    }
                }
            }

            // React to login success
            LaunchedEffect(loginState) {
                if (loginState is AuthUiState.Success) {
                    authViewModel.resetLoginState()
                    currentScreen = "main"
                }
            }

            // React to registration success
            LaunchedEffect(registerState) {
                if (registerState is AuthUiState.Success) {
                    authViewModel.resetRegisterState()
                    if (authViewModel.isLoggedIn) {
                        currentScreen = "main"
                    } else {
                        currentScreen = "login"
                    }
                }
            }

            CodeTrackTheme(darkTheme = darkTheme) {
                when (currentScreen) {
                    "splash" -> {
                        SplashScreen(
                            sessionState = sessionState,
                            isLoggedIn = authViewModel.isLoggedIn,
                            onRetry = {
                                authViewModel.restoreSession()
                            },
                            onGoToLogin = {
                                authViewModel.logout()
                                currentScreen = "login"
                            }
                        )
                    }

                    "login" -> {
                        LoginScreen(
                            onLogin = { email, password ->
                                authViewModel.login(email, password)
                            },
                            onRegisterClick = {
                                authViewModel.resetLoginState()
                                currentScreen = "register"
                            },
                            onForgotPasswordClick = {
                                authViewModel.resetLoginState()
                                currentScreen = "forgot"
                            },
                            isLoading = loginState is AuthUiState.Loading,
                            errorMessage = (loginState as? AuthUiState.Error)?.message
                        )
                    }

                    "register" -> {
                        BackHandler {
                            authViewModel.resetRegisterState()
                            currentScreen = "login"
                        }
                        RegisterScreen(
                            onRegister = { name, email, password ->
                                authViewModel.register(name, email, password)
                            },
                            onLoginClick = {
                                authViewModel.resetRegisterState()
                                currentScreen = "login"
                            },
                            isLoading = registerState is AuthUiState.Loading,
                            errorMessage = (registerState as? AuthUiState.Error)?.message
                        )
                    }

                    "forgot" -> {
                        BackHandler {
                            authViewModel.resetForgotPasswordState()
                            authViewModel.resetResetPasswordState()
                            currentScreen = "login"
                        }
                        ForgotPasswordScreen(
                            onBackClick = {
                                authViewModel.resetForgotPasswordState()
                                authViewModel.resetResetPasswordState()
                                currentScreen = "login"
                            },
                            onSendCode = { email ->
                                authViewModel.forgotPassword(email)
                            },
                            onResetPassword = { email, token, newPassword ->
                                authViewModel.resetPassword(email, token, newPassword)
                            },
                            isSendingCode = forgotPasswordState is AuthUiState.Loading,
                            isResetting = resetPasswordState is AuthUiState.Loading,
                            sendCodeSuccessMessage = (forgotPasswordState as? AuthUiState.Success)?.data,
                            resetSuccessMessage = (resetPasswordState as? AuthUiState.Success)?.data,
                            errorMessage = (forgotPasswordState as? AuthUiState.Error)?.message
                                ?: (resetPasswordState as? AuthUiState.Error)?.message
                        )
                    }

                    "main", "home" -> {
                        BackHandler {
                            // Root authenticated view: back exits the app instead of navigating to login
                            finish()
                        }
                        MainScreen(
                            darkTheme = darkTheme,
                            onDarkThemeChange = {
                                darkTheme = it
                            },
                            onLogout = {
                                authViewModel.logout()
                                currentScreen = "login"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SplashScreen(
    sessionState: AuthUiState<*>,
    isLoggedIn: Boolean,
    onRetry: () -> Unit,
    onGoToLogin: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF10182D)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF5145E5),
                modifier = Modifier.size(76.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "</>",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "CodeTrack",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF8FAFC)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your DSA preparation companion",
                fontSize = 14.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (sessionState is AuthUiState.Error && isLoggedIn) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Connection Error",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = sessionState.message,
                            color = Color(0xFFFF6B6B),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onRetry,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5145E5))
                        ) {
                            Text("Retry Connection")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = onGoToLogin,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Sign In With Another Account", color = Color(0xFF60A5FA))
                        }
                    }
                }
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp),
                    color = Color(0xFF5145E5),
                    strokeWidth = 3.dp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Checking session...",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
            }
        }
    }
}
