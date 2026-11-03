
package com.example.codetrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.codetrack.ui.MainScreen
import com.example.codetrack.ui.screens.LoginScreen
import com.example.codetrack.ui.screens.RegisterScreen
import com.example.codetrack.ui.screens.ForgotPasswordScreen
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
                                // TODO: Connect to backend login API.
                                // Navigate to home only after successful authentication.
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
                        RegisterScreen(
                            onRegister = { name, email, password ->
                                // TODO: Connect to backend registration API.
                            },
                            onLoginClick = {
                                currentScreen = "login"
                            }
                        )
                    }

                    "forgot" -> {
                        ForgotPasswordScreen(
                            onBackClick = {
                                currentScreen = "login"
                            },
                            onSendCode = { email ->
                                // TODO: Connect to backend verification-code API.
                            },
                            onResetPassword = { email, code, password ->
                                // TODO: Connect to backend password-reset API.
                            }
                        )
                    }

                    "home" -> {
                        MainScreen(
                            darkTheme = darkTheme,
                            onDarkThemeChange = {
                                darkTheme = it
                            }
                        )
                    }
                }
            }
        }
    }
}