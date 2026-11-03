package com.example.codetrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.codetrack.ui.MainScreen
import com.example.codetrack.ui.screens.ForgotPasswordScreen
import com.example.codetrack.ui.screens.LoginScreen
import com.example.codetrack.ui.screens.RegisterScreen
import com.example.codetrack.ui.theme.CodeTrackTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var darkTheme by rememberSaveable {
                mutableStateOf(false)
            }

            var currentScreen by rememberSaveable {
                mutableStateOf("login")
            }

            CodeTrackTheme(darkTheme = darkTheme) {
                when (currentScreen) {
                    "login" -> {
                        LoginScreen(
                            onLogin = {
                                // Navigate to main application upon validated credentials
                                currentScreen = "main"
                            },
                            onRegisterClick = {
                                currentScreen = "register"
                            },
                            onForgotPasswordClick = {
                                currentScreen = "forgot"
                            }
                        )
                    }

                    "register" -> {
                        BackHandler {
                            currentScreen = "login"
                        }
                        RegisterScreen(
                            onRegister = { _, _, _ ->
                                // Client-side validated; returns to login for sign in
                                currentScreen = "login"
                            },
                            onLoginClick = {
                                currentScreen = "login"
                            }
                        )
                    }

                    "forgot" -> {
                        BackHandler {
                            currentScreen = "login"
                        }
                        ForgotPasswordScreen(
                            onBackClick = {
                                currentScreen = "login"
                            },
                            onSendCode = { _ ->
                                // Verification code action
                            },
                            onResetPassword = { _, _, _ ->
                                // Password reset complete; return to login
                                currentScreen = "login"
                            }
                        )
                    }

                    "main", "home" -> {
                        MainScreen(
                            darkTheme = darkTheme,
                            onDarkThemeChange = {
                                darkTheme = it
                            },
                            onLogout = {
                                currentScreen = "login"
                            }
                        )
                    }
                }
            }
        }
    }
}
