
package com.example.codetrack_backend.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class ProblemRequest(

    @field:NotBlank(message = "Problem title is required")
    @field:Size(max = 255, message = "Title cannot exceed 255 characters")
    val title: String,

    @field:Size(max = 100, message = "Platform cannot exceed 100 characters")
    val platform: String? = null,

    @field:Size(max = 100, message = "Topic cannot exceed 100 characters")
    val topic: String? = null,

    @field:Size(max = 50, message = "Difficulty cannot exceed 50 characters")
    val difficulty: String? = null,

    @field:Size(max = 500, message = "Problem URL cannot exceed 500 characters")
    val problemUrl: String? = null,

    val solved: Boolean? = false,
    val solvedDate: LocalDate? = null,
    val notes: String? = null
)
