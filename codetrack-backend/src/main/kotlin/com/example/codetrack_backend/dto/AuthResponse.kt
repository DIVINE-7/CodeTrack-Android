package com.example.codetrack_backend.dto

data class AuthResponse(
    val message: String,
    val token: String? = null,
    val tokenType: String? = null,
    val userId: Long? = null,
    val name: String? = null,
    val email: String? = null
)