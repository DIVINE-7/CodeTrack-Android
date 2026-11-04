package com.example.codetrack_backend.controller

import com.example.codetrack_backend.dto.*
import com.example.codetrack_backend.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(
        @Valid @RequestBody request: RegisterRequest
    ): AuthResponse {
        return authService.register(request)
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest
    ): AuthResponse {
        return authService.login(request)
    }

    @PostMapping("/forgot-password")
    fun forgotPassword(
        @Valid @RequestBody request: ForgotPasswordRequest
    ): MessageResponse {
        return authService.forgotPassword(request)
    }

    @PostMapping("/reset-password")
    fun resetPassword(
        @Valid @RequestBody request: ResetPasswordRequest
    ): MessageResponse {
        return authService.resetPassword(request)
    }

    @GetMapping("/me")
    fun currentUser(
        @AuthenticationPrincipal jwt: Jwt
    ): Map<String, Any?> {
        return mapOf(
            "userId" to jwt.subject,
            "email" to jwt.getClaimAsString("email"),
            "name" to jwt.getClaimAsString("name")
        )
    }
}