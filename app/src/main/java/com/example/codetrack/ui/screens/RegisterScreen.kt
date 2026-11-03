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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codetrack.ui.theme.CodeTrackTheme

private val RegisterNavy = Color(0xFF10182D)
private val RegisterCard = Color(0xFF1E293B)
private val RegisterAccent = Color(0xFF5145E5)
private val RegisterWhite = Color(0xFFF8FAFC)
private val RegisterMuted = Color(0xFF94A3B8)

@Composable
fun RegisterScreen(
    onRegister: (String, String, String) -> Unit,
    onLoginClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

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
                        onValueChange = {
                            name = it
                            errorMessage = ""
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    AuthRegisterField(
                        label = "Email Address",
                        value = email,
                        placeholder = "Enter your email",
                        onValueChange = {
                            email = it
                            errorMessage = ""
                        },
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(Modifier.height(16.dp))

                    AuthRegisterField(
                        label = "Password",
                        value = password,
                        placeholder = "Create a password",
                        onValueChange = {
                            password = it
                            errorMessage = ""
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
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = ""
                        },
                        isPassword = true,
                        passwordVisible = confirmVisible,
                        onVisibilityChange = {
                            confirmVisible = !confirmVisible
                        }
                    )

                    Spacer(Modifier.height(14.dp))

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            errorMessage,
                            color = Color(0xFFFF6B6B),
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(10.dp))
                    }

                    Button(
                        onClick = {
                            when {
                                name.isBlank() ->
                                    errorMessage = "Please enter your name."

                                email.isBlank() ->
                                    errorMessage = "Please enter your email."

                                !android.util.Patterns.EMAIL_ADDRESS
                                    .matcher(email).matches() ->
                                    errorMessage = "Enter a valid email."

                                password.length < 8 ->
                                    errorMessage = "Password must be at least 8 characters."

                                confirmPassword.isBlank() ->
                                    errorMessage = "Please confirm your password."

                                password != confirmPassword ->
                                    errorMessage = "Passwords do not match."

                                else -> {
                                    errorMessage = ""
                                    onRegister(name.trim(), email.trim(), password)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RegisterAccent
                        )
                    ) {
                        Text(
                            "Create Account",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
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

                        TextButton(onClick = onLoginClick) {
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
                        onClick = { onVisibilityChange?.invoke() }
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
                cursorColor = RegisterAccent
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
