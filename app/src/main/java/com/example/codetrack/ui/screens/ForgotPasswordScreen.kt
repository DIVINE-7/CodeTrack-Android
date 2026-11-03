
package com.example.codetrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ForgotNavy = Color(0xFF10182D)
private val ForgotCard = Color(0xFF1E293B)
private val ForgotAccent = Color(0xFF5145E5)
private val ForgotWhite = Color(0xFFF8FAFC)
private val ForgotMuted = Color(0xFF94A3B8)

@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit,
    onSendCode: (String) -> Unit,
    onResetPassword: (String, String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ForgotNavy
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(18.dp),
                color = ForgotAccent
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

            Spacer(Modifier.height(20.dp))

            Text(
                "Reset Password",
                color = ForgotWhite,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Recover access to your CodeTrack account.",
                color = ForgotMuted,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(30.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ForgotCard
                )
            ) {
                Column(Modifier.padding(20.dp)) {

                    ForgotField(
                        label = "Email Address",
                        value = email,
                        placeholder = "Enter your registered email",
                        onValueChange = {
                            email = it
                            errorMessage = ""
                        },
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = {
                            when {
                                email.isBlank() ->
                                    errorMessage = "Please enter your email."

                                !android.util.Patterns.EMAIL_ADDRESS
                                    .matcher(email).matches() ->
                                    errorMessage = "Enter a valid email."

                                else -> {
                                    errorMessage = ""
                                    onSendCode(email.trim())
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Send Verification Code",
                            color = Color(0xFF60A5FA)
                        )
                    }

                    Spacer(Modifier.height(22.dp))

                    ForgotField(
                        label = "Verification Code",
                        value = code,
                        placeholder = "Enter verification code",
                        onValueChange = {
                            code = it
                            errorMessage = ""
                        },
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(Modifier.height(16.dp))

                    ForgotField(
                        label = "New Password",
                        value = password,
                        placeholder = "Enter new password",
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

                    ForgotField(
                        label = "Confirm New Password",
                        value = confirmPassword,
                        placeholder = "Confirm new password",
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

                    Spacer(Modifier.height(18.dp))

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            errorMessage,
                            color = Color(0xFFFF6B6B),
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    Button(
                        onClick = {
                            when {
                                email.isBlank() ->
                                    errorMessage = "Enter your email."

                                code.isBlank() ->
                                    errorMessage = "Enter your verification code."

                                password.length < 8 ->
                                    errorMessage = "Password must be at least 8 characters."

                                password != confirmPassword ->
                                    errorMessage = "Passwords do not match."

                                else -> {
                                    errorMessage = ""
                                    onResetPassword(
                                        email.trim(),
                                        code.trim(),
                                        password
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ForgotAccent
                        )
                    ) {
                        Text(
                            "Reset Password",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    TextButton(
                        onClick = onBackClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Back to Login",
                            color = Color(0xFF60A5FA)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ForgotField(
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
            color = ForgotWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(placeholder, color = ForgotMuted)
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
                            tint = ForgotMuted
                        )
                    }
                }
            } else null,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType
            ),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ForgotWhite,
                unfocusedTextColor = ForgotWhite,
                focusedBorderColor = ForgotAccent,
                unfocusedBorderColor = Color.DarkGray,
                cursorColor = ForgotAccent
            )
        )
    }
}