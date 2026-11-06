package com.example.codetrack.data.remote.api

import com.example.codetrack.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Retrofit contract for CodeTrack Spring Boot Authentication APIs.
 *
 * Base URL: http://10.0.2.2:8080/api/
 */
interface AuthApiService {

    /**
     * Registers a new user.
     * Endpoint: POST /api/auth/register
     */
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequestDto
    ): Response<AuthResponseDto>

    /**
     * Authenticates existing user credentials.
     * Endpoint: POST /api/auth/login
     */
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): Response<AuthResponseDto>

    /**
     * Requests a password reset token via email.
     * Endpoint: POST /api/auth/forgot-password
     */
    @POST("auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequestDto
    ): Response<MessageResponseDto>

    /**
     * Validates reset token and sets a new password.
     * Endpoint: POST /api/auth/reset-password
     */
    @POST("auth/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequestDto
    ): Response<MessageResponseDto>

    /**
     * Retrieves currently authenticated user metadata from JWT claims.
     * Endpoint: GET /api/auth/me
     *
     * @param authorizationHeader "Bearer <JWT_TOKEN>"
     */
    @GET("auth/me")
    suspend fun getCurrentUser(
        @Header("Authorization") authorizationHeader: String
    ): Response<CurrentUserResponseDto>
}
