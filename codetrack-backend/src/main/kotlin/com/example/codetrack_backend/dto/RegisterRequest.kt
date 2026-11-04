package com.example.codetrack_backend.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(

    @field:NotBlank
    @field:Size(max = 100)
    val name: String,

    @field:NotBlank
    @field:Email
    @field:Size(max = 150)
    val email: String,

    @field:NotBlank
    @field:Size(min = 8, max = 72)
    val password: String
)