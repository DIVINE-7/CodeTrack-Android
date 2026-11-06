package com.example.codetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codetrack.ui.screens.auth.AuthValidationUtils
import com.example.codetrack.ui.theme.CodeTrackTheme

private val RegisterNavy = Color(0xFF10182D)
private val RegisterCard = Color(0xFF1E293B)
private val RegisterAccent = Color(0xFF5145E5)
private val RegisterWhite = Color(0xFFF8FAFC)
private val RegisterMuted = Color(0xFF94A3B8)

@Composable
fun RegisterScreen(
    onRegister: (String, String, String) -> Unit,
    onLoginClick: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmVisible by rememberSaveable { mutableStateOf(false) }
    var localError by remember { mutableStateOf("") }

    val activeError = if (!errorMessage.isNullOrBlank()) errorMessage else localError

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = RegisterNavy
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(18.dp),
                color = RegisterAccent
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "</>",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Create Account",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = RegisterWhite
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Start your coding journey with CodeTrack.",
                color = RegisterMuted,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = RegisterCard
                )
            ) {
                Column(Modifier.padding(20.dp)) {

                    AuthRegisterField(
                        label = "Full Name",
                        value = name,
                        placeholder = "Enter your full name",
                        enabled = !isLoading,
                        onValueChange = {
                            name = it
                            localError = ""
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    AuthRegisterField(
                        label = "Email Address",
                        value = email,
                        placeholder = "Enter your email",
                        enabled = !isLoading,
                        onValueChange = {
                            email = it
                            localError = ""
                        },
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(Modifier.height(16.dp))

                    AuthRegisterField(
                        label = "Password",
                        value = password,
                        placeholder = "Create a password (8–72 characters)",
                        enabled = !isLoading,
                        onValueChange = {
                            password = it
                            localError = ""
                        },
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onVisibilityChange = {
                            passwordVisible = !passwordVisible
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    AuthRegisterField(
                        label = "Confirm Password",
                        value = confirmPassword,
                        placeholder = "Confirm your password",
                        enabled = !isLoading,
                        onValueChange = {
                            confirmPassword = it
                            localError = ""
                        },
                        isPassword = true,
                        passwordVisible = confirmVisible,
                        onVisibilityChange = {
                            confirmVisible = !confirmVisible
                        }
                    )

                    Spacer(Modifier.height(14.dp))

                    if (activeError.isNotEmpty()) {
                        Text(
                            activeError,
                            color = Color(0xFFFF6B6B),
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(10.dp))
                    }

                    Button(
                        onClick = {
                            if (isLoading) return@Button
                            when {
                                name.isBlank() ->
                                    localError = "Please enter your name."

                                email.isBlank() ->
                                    localError = "Please enter your email."

                                !AuthValidationUtils.isValidEmail(email) ->
                                    localError = "Enter a valid email."

                                password.length < 8 ->
                                    localError = "Password must be at least 8 characters."

                                password.length > 72 ->
                                    localError = "Password cannot exceed 72 characters."

                                confirmPassword.isBlank() ->
                                    localError = "Please confirm your password."

                                password != confirmPassword ->
                                    localError = "Passwords do not match."

                                else -> {
                                    localError = ""
                                    onRegister(name.trim(), email.trim(), password)
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RegisterAccent
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                "Create Account",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Already have an account? ",
                            color = RegisterMuted,
                            fontSize = 12.sp
                        )

                        TextButton(
                            onClick = onLoginClick,
                            enabled = !isLoading
                        ) {
                            Text(
                                "Login",
                                color = Color(0xFF60A5FA),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthRegisterField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    enabled: Boolean = true,
    onVisibilityChange: (() -> Unit)? = null
) {
    Column {
        Text(
            label,
            color = RegisterWhite,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(placeholder, color = RegisterMuted)
            },
            singleLine = true,
            visualTransformation = if (isPassword && !passwordVisible)
                PasswordVisualTransformation()
            else
                VisualTransformation.None,
            trailingIcon = if (isPassword) {
                {
                    IconButton(
                        onClick = { onVisibilityChange?.invoke() },
                        enabled = enabled
                    ) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Default.VisibilityOff
                            else
                                Icons.Default.Visibility,
                            contentDescription = "Toggle password visibility",
                            tint = RegisterMuted
                        )
                    }
                }
            } else null,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType
            ),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = RegisterWhite,
                unfocusedTextColor = RegisterWhite,
                focusedBorderColor = RegisterAccent,
                unfocusedBorderColor = Color.DarkGray,
                cursorColor = RegisterAccent,
                disabledTextColor = RegisterWhite.copy(alpha = 0.6f),
                disabledBorderColor = Color.DarkGray.copy(alpha = 0.4f)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    CodeTrackTheme {
        RegisterScreen(
            onRegister = { _, _, _ -> },
            onLoginClick = {}
        )
    }
}
