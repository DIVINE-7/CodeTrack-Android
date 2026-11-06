package com.example.codetrack.data.repository

import android.content.Context
import com.example.codetrack.data.local.security.SecureStorageEngine
import com.example.codetrack.data.local.security.TokenManager
import com.example.codetrack.data.local.security.UserSession
import com.example.codetrack.data.remote.api.AuthApiService
import com.example.codetrack.data.remote.api.RetrofitClient
import com.example.codetrack.data.remote.dto.ApiErrorResponseDto
import com.example.codetrack.data.remote.dto.AuthResponseDto
import com.example.codetrack.data.remote.dto.CurrentUserResponseDto
import com.example.codetrack.data.remote.dto.ForgotPasswordRequestDto
import com.example.codetrack.data.remote.dto.LoginRequestDto
import com.example.codetrack.data.remote.dto.RegisterRequestDto
import com.example.codetrack.data.remote.dto.ResetPasswordRequestDto
import com.google.gson.Gson
import retrofit2.Response
import java.io.IOException

/**
 * Structured exception for authentication and network API errors.
 * Preserves the HTTP status code (if any) and provides a safe, user-friendly message.
 * Never leaks raw passwords, tokens, or system stack traces.
 */
class AuthApiException(
    override val message: String,
    val statusCode: Int? = null,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Repository responsible for user authentication workflows, JWT credential persistence,
 * and session state management across CodeTrack.
 *
 * @param apiService Retrofit API service interface for CodeTrack Spring Boot auth endpoints.
 * @param tokenManager Android Keystore-backed secure token and session manager.
 */
class AuthRepository(
    private val apiService: AuthApiService = RetrofitClient.authApiService,
    private val tokenManager: TokenManager
) {

    private val gson = Gson()

    /**
     * Authenticates user with email and password.
     * On HTTP 200 with valid JWT, persists the session securely.
     * Passwords are never stored locally.
     */
    suspend fun login(email: String, password: String): Result<AuthResponseDto> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || password.isBlank()) {
            return Result.failure(AuthApiException("Email and password cannot be blank", statusCode = 400))
        }

        return try {
            val response = apiService.login(LoginRequestDto(email = trimmedEmail, password = password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && !body.token.isNullOrBlank()) {
                    tokenManager.saveSession(
                        token = body.token,
                        userId = body.userId,
                        name = body.name,
                        email = body.email ?: trimmedEmail
                    )
                    Result.success(body)
                } else {
                    Result.failure(AuthApiException("Authentication succeeded but no token was provided by server", statusCode = response.code()))
                }
            } else {
                Result.failure(parseApiError(response, "Login failed. Please check your credentials."))
            }
        } catch (e: IOException) {
            Result.failure(IOException("Unable to connect to server. Please check your internet connection.", e))
        } catch (e: Throwable) {
            Result.failure(AuthApiException(e.message ?: "An unexpected error occurred during login", cause = e))
        }
    }

    /**
     * Registers a new user account.
     * On HTTP 201 with valid JWT, persists the session securely.
     */
    suspend fun register(name: String, email: String, password: String): Result<AuthResponseDto> {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()
        if (trimmedName.isBlank() || trimmedEmail.isBlank() || password.isBlank()) {
            return Result.failure(AuthApiException("Name, email, and password cannot be blank", statusCode = 400))
        }

        return try {
            val response = apiService.register(
                RegisterRequestDto(
                    name = trimmedName,
                    email = trimmedEmail,
                    password = password
                )
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    if (!body.token.isNullOrBlank()) {
                        tokenManager.saveSession(
                            token = body.token,
                            userId = body.userId,
                            name = body.name ?: trimmedName,
                            email = body.email ?: trimmedEmail
                        )
                    }
                    Result.success(body)
                } else {
                    Result.failure(AuthApiException("Registration succeeded but response body was empty", statusCode = response.code()))
                }
            } else {
                Result.failure(parseApiError(response, "Registration failed."))
            }
        } catch (e: IOException) {
            Result.failure(IOException("Unable to connect to server. Please check your internet connection.", e))
        } catch (e: Throwable) {
            Result.failure(AuthApiException(e.message ?: "An unexpected error occurred during registration", cause = e))
        }
    }

    /**
     * Initiates a password reset request for the specified email.
     * Does not claim an email was sent unless the backend confirms success.
     */
    suspend fun forgotPassword(email: String): Result<String> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank()) {
            return Result.failure(AuthApiException("Email address cannot be blank", statusCode = 400))
        }

        return try {
            val response = apiService.forgotPassword(ForgotPasswordRequestDto(email = trimmedEmail))
            if (response.isSuccessful) {
                val body = response.body()
                val message = body?.message ?: "Password reset instructions have been sent to your email."
                Result.success(message)
            } else {
                Result.failure(parseApiError(response, "Unable to process forgot password request."))
            }
        } catch (e: IOException) {
            Result.failure(IOException("Unable to connect to server. Please check your internet connection.", e))
        } catch (e: Throwable) {
            Result.failure(AuthApiException(e.message ?: "An unexpected error occurred", cause = e))
        }
    }

    /**
     * Completes password reset using email, reset token, and new password.
     */
    suspend fun resetPassword(email: String, token: String, newPassword: String): Result<String> {
        val trimmedEmail = email.trim()
        val trimmedToken = token.trim()
        if (trimmedEmail.isBlank() || trimmedToken.isBlank() || newPassword.isBlank()) {
            return Result.failure(AuthApiException("Email, reset token, and new password are required", statusCode = 400))
        }

        return try {
            val response = apiService.resetPassword(
                ResetPasswordRequestDto(
                    email = trimmedEmail,
                    token = trimmedToken,
                    newPassword = newPassword
                )
            )
            if (response.isSuccessful) {
                val body = response.body()
                val message = body?.message ?: "Password reset successfully. You can now log in."
                Result.success(message)
            } else {
                Result.failure(parseApiError(response, "Password reset failed. Please verify your reset token."))
            }
        } catch (e: IOException) {
            Result.failure(IOException("Unable to connect to server. Please check your internet connection.", e))
        } catch (e: Throwable) {
            Result.failure(AuthApiException(e.message ?: "An unexpected error occurred", cause = e))
        }
    }

    /**
     * Retrieves currently authenticated user metadata from GET /api/auth/me.
     * Sends the stored JWT in an explicit Authorization: Bearer header.
     *
     * If backend returns HTTP 401 Unauthorized, automatically wipes the invalid local session.
     * If a network or 5xx server error occurs, preserves the local session.
     */
    suspend fun getCurrentUser(): Result<CurrentUserResponseDto> {
        val token = tokenManager.getToken()
        if (token.isNullOrBlank()) {
            return Result.failure(AuthApiException("No active authentication token found", statusCode = 401))
        }

        return try {
            val response = apiService.getCurrentUser("Bearer $token")
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(AuthApiException("User profile response was empty", statusCode = response.code()))
                }
            } else {
                if (response.code() == 401) {
                    // Token expired or invalidated: wipe session
                    tokenManager.clearSession()
                    Result.failure(AuthApiException("Session expired. Please log in again.", statusCode = 401))
                } else {
                    // Do NOT clear session for temporary or non-auth errors
                    Result.failure(parseApiError(response, "Failed to retrieve user profile."))
                }
            }
        } catch (e: IOException) {
            // Network failure: preserve local session
            Result.failure(IOException("Unable to connect to server. Please check your internet connection.", e))
        } catch (e: Throwable) {
            Result.failure(AuthApiException(e.message ?: "An unexpected error occurred", cause = e))
        }
    }

    /**
     * Validates an existing saved JWT against GET /api/auth/me and enriches local session metadata.
     *
     * - Returns [Result.success] with updated [UserSession] if the token is valid.
     * - Clears local session and returns [Result.failure] if the token is expired/invalid (HTTP 401).
     * - Preserves local session if validation cannot be reached due to network or temporary server errors.
     */
    suspend fun restoreSession(): Result<UserSession> {
        val currentSession = tokenManager.getUserSession()
        if (currentSession == null || currentSession.token.isBlank()) {
            return Result.failure(AuthApiException("No active session found", statusCode = 401))
        }

        val currentUserResult = getCurrentUser()
        return currentUserResult.fold(
            onSuccess = { currentUser ->
                val enrichedSession = currentSession.copy(
                    userId = currentUser.userIdAsLong ?: currentSession.userId,
                    name = currentUser.name ?: currentSession.name,
                    email = currentUser.email ?: currentSession.email
                )
                tokenManager.saveSession(
                    token = enrichedSession.token,
                    userId = enrichedSession.userId,
                    name = enrichedSession.name,
                    email = enrichedSession.email
                )
                Result.success(enrichedSession)
            },
            onFailure = { error ->
                // Note: If 401 occurred, getCurrentUser() already cleared tokenManager.
                // If network/5xx occurred, tokenManager remains intact.
                Result.failure(error)
            }
        )
    }

    /**
     * Logs out the user by wiping all local encrypted session credentials.
     */
    fun logout() {
        tokenManager.clearSession()
    }

    /**
     * Returns the currently stored user session, or null if unauthenticated.
     */
    fun getStoredSession(): UserSession? {
        return tokenManager.getUserSession()
    }

    /**
     * Returns true if a local authentication token is saved.
     */
    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }

    /**
     * Safely converts backend error bodies into user-facing messages.
     * Gracefully handles malformed JSON, empty bodies, and non-JSON error pages.
     */
    private fun parseApiError(response: Response<*>, defaultMessage: String): AuthApiException {
        val statusCode = response.code()
        return try {
            val errorBody = response.errorBody()?.string()
            if (!errorBody.isNullOrBlank()) {
                val errorDto = gson.fromJson(errorBody, ApiErrorResponseDto::class.java)
                val friendlyMessage = errorDto?.userFriendlyMessage()
                if (!friendlyMessage.isNullOrBlank()) {
                    return AuthApiException(friendlyMessage, statusCode = statusCode)
                }
            }
            val fallback = when (statusCode) {
                400 -> "Invalid request. Please check your input."
                401 -> "Invalid credentials or unauthorized."
                403 -> "Access denied."
                404 -> "Requested service or resource not found."
                409 -> "An account with these details already exists."
                500 -> "Server error occurred. Please try again later."
                else -> defaultMessage
            }
            AuthApiException(fallback, statusCode = statusCode)
        } catch (_: Throwable) {
            AuthApiException(defaultMessage, statusCode = statusCode)
        }
    }

    companion object {
        @Volatile
        private var instance: AuthRepository? = null

        fun getInstance(
            context: Context,
            apiService: AuthApiService = RetrofitClient.authApiService,
            customStorageEngine: SecureStorageEngine? = null
        ): AuthRepository {
            return instance ?: synchronized(this) {
                instance ?: AuthRepository(
                    apiService = apiService,
                    tokenManager = TokenManager.getInstance(context, customStorageEngine)
                ).also { instance = it }
            }
        }
    }
}
