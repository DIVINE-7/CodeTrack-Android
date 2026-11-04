package com.example.codetrack_backend.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ResetPasswordRequest(

    @field:NotBlank
    @field:Email
    val email: String,

    @field:NotBlank
    val token: String,

    @field:NotBlank
    @field:Size(min = 8, max = 72)
    val newPassword: String
)