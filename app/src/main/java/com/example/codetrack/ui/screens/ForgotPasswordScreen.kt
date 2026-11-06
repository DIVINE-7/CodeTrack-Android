package com.example.codetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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

private val ForgotNavy = Color(0xFF10182D)
private val ForgotCard = Color(0xFF1E293B)
private val ForgotAccent = Color(0xFF5145E5)
private val ForgotWhite = Color(0xFFF8FAFC)
private val ForgotMuted = Color(0xFF94A3B8)
private val SuccessGreen = Color(0xFF4ADE80)

@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit,
    onSendCode: (String) -> Unit,
    onResetPassword: (String, String, String) -> Unit,
    isSendingCode: Boolean = false,
    isResetting: Boolean = false,
    sendCodeSuccessMessage: String? = null,
    resetSuccessMessage: String? = null,
    errorMessage: String? = null
) {
    var email by rememberSaveable { mutableStateOf("") }
    var token by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmVisible by rememberSaveable { mutableStateOf(false) }
    var localError by remember { mutableStateOf("") }

    val activeError = if (!errorMessage.isNullOrBlank()) errorMessage else localError

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

                    // Step 1: Email Address
                    ForgotField(
                        label = "Email Address",
                        value = email,
                        placeholder = "Enter your registered email",
                        enabled = !isSendingCode && !isResetting,
                        onValueChange = {
                            email = it
                            localError = ""
                        },
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = {
                            if (isSendingCode || isResetting) return@OutlinedButton
                            when {
                                email.isBlank() ->
                                    localError = "Please enter your email."

                                !AuthValidationUtils.isValidEmail(email) ->
                                    localError = "Enter a valid email."

                                else -> {
                                    localError = ""
                                    onSendCode(email.trim())
                                }
                            }
                        },
                        enabled = !isSendingCode && !isResetting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSendingCode) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFF60A5FA),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Send Reset Token",
                                color = Color(0xFF60A5FA)
                            )
                        }
                    }

                    // Success banner for token dispatch
                    if (!sendCodeSuccessMessage.isNullOrBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SuccessGreen.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = sendCodeSuccessMessage,
                                    color = SuccessGreen,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(22.dp))

                    // Step 2: Reset Token (alphanumeric URL-safe token received via email)
                    ForgotField(
                        label = "Reset Token",
                        value = token,
                        placeholder = "Paste the reset token from your email",
                        enabled = !isResetting,
                        onValueChange = {
                            token = it
                            localError = ""
                        },
                        keyboardType = KeyboardType.Text
                    )

                    Spacer(Modifier.height(18.dp))

                    ForgotField(
                        label = "New Password",
                        value = password,
                        placeholder = "Create a new password (8–72 characters)",
                        enabled = !isResetting,
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

                    Spacer(Modifier.height(18.dp))

                    ForgotField(
                        label = "Confirm New Password",
                        value = confirmPassword,
                        placeholder = "Re-enter new password",
                        enabled = !isResetting,
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

                    Spacer(Modifier.height(18.dp))

                    // Error banner
                    if (activeError.isNotEmpty()) {
                        Text(
                            activeError,
                            color = Color(0xFFFF6B6B),
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    // Success banner for password reset completion
                    if (!resetSuccessMessage.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SuccessGreen.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = resetSuccessMessage,
                                    color = SuccessGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    Button(
                        onClick = {
                            if (isResetting) return@Button
                            when {
                                email.isBlank() ->
                                    localError = "Enter your email."

                                !AuthValidationUtils.isValidEmail(email) ->
                                    localError = "Enter a valid email."

                                token.isBlank() ->
                                    localError = "Enter your reset token."

                                password.length < 8 ->
                                    localError = "Password must be at least 8 characters."

                                password.length > 72 ->
                                    localError = "Password cannot exceed 72 characters."

                                password != confirmPassword ->
                                    localError = "Passwords do not match."

                                else -> {
                                    localError = ""
                                    onResetPassword(
                                        email.trim(),
                                        token.trim(),
                                        password
                                    )
                                }
                            }
                        },
                        enabled = !isResetting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ForgotAccent
                        )
                    ) {
                        if (isResetting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                "Reset Password",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    TextButton(
                        onClick = onBackClick,
                        enabled = !isResetting,
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
    enabled: Boolean = true,
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
            enabled = enabled,
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
                        onClick = { onVisibilityChange?.invoke() },
                        enabled = enabled
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
                cursorColor = ForgotAccent,
                disabledTextColor = ForgotWhite.copy(alpha = 0.6f),
                disabledBorderColor = Color.DarkGray.copy(alpha = 0.4f)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    CodeTrackTheme {
        ForgotPasswordScreen(
            onBackClick = {},
            onSendCode = {},
            onResetPassword = { _, _, _ -> }
        )
    }
}
