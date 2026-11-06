package com.example.codetrack.ui.screens.auth

/**
 * Common validation utilities for authentication forms.
 * Provides fallback regex matching for unit test environments where Android framework
 * classes like [android.util.Patterns] are unmocked.
 */
object AuthValidationUtils {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        if (trimmed.isBlank()) return false
        return try {
            android.util.Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()
        } catch (_: Throwable) {
            EMAIL_REGEX.matches(trimmed)
        }
    }
}
