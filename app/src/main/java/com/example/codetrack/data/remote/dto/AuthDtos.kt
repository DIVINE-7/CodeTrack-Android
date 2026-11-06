package com.example.codetrack.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Request payload for POST /api/auth/register
 */
data class RegisterRequestDto(
    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)

/**
 * Request payload for POST /api/auth/login
 */
data class LoginRequestDto(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)

/**
 * Request payload for POST /api/auth/forgot-password
 */
data class ForgotPasswordRequestDto(
    @SerializedName("email")
    val email: String
)

/**
 * Request payload for POST /api/auth/reset-password
 */
data class ResetPasswordRequestDto(
    @SerializedName("email")
    val email: String,

    @SerializedName("token")
    val token: String,

    @SerializedName("newPassword")
    val newPassword: String
)

/**
 * Response payload for successful authentication from:
 * - POST /api/auth/register (201 Created)
 * - POST /api/auth/login (200 OK)
 */
data class AuthResponseDto(
    @SerializedName("message")
    val message: String,

    @SerializedName("token")
    val token: String? = null,

    @SerializedName("tokenType")
    val tokenType: String? = null,

    @SerializedName("userId")
    val userId: Long? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("email")
    val email: String? = null
)

/**
 * Response payload for generic message responses from:
 * - POST /api/auth/forgot-password (200 OK)
 * - POST /api/auth/reset-password (200 OK)
 */
data class MessageResponseDto(
    @SerializedName("message")
    val message: String
)

/**
 * Response payload for GET /api/auth/me (200 OK)
 *
 * NOTE: The backend's /me endpoint returns `mapOf("userId" to jwt.subject, ...)`
 * where `jwt.subject` is serialized as a JSON string (e.g., "1"), whereas
 * AuthResponseDto serializes `userId` as a Long. This DTO represents the actual
 * JSON contract and exposes [userIdAsLong] for convenient type conversion.
 */
data class CurrentUserResponseDto(
    @SerializedName("userId")
    val userId: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("name")
    val name: String? = null
) {
    val userIdAsLong: Long?
        get() = userId?.toLongOrNull()
}

/**
 * Structured error response matching backend's:
 * 1. GlobalExceptionHandler validation errors (status 400 with `messages` list)
 * 2. Spring Boot ResponseStatusException (status 400, 401, 409 with `message` string)
 */
data class ApiErrorResponseDto(
    @SerializedName("timestamp")
    val timestamp: String? = null,

    @SerializedName("status")
    val status: Int? = null,

    @SerializedName("error")
    val error: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("messages")
    val messages: List<FieldErrorDto>? = null,

    @SerializedName("path")
    val path: String? = null
) {
    /**
     * Resolves the most descriptive, user-facing error message available.
     */
    fun userFriendlyMessage(): String {
        if (!message.isNullOrBlank()) {
            return message
        }
        val fieldErrors = messages?.mapNotNull { it.message }?.filter { it.isNotBlank() }
        if (!fieldErrors.isNullOrEmpty()) {
            return fieldErrors.joinToString("; ")
        }
        return error ?: "An unexpected error occurred"
    }
}

/**
 * Field-level validation detail returned by backend MethodArgumentNotValidException.
 */
data class FieldErrorDto(
    @SerializedName("field")
    val field: String? = null,

    @SerializedName("message")
    val message: String? = null
)
