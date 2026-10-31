
package com.example.codetrack_backend.dto

import java.time.LocalDate
import java.time.LocalDateTime

data class ProblemResponse(
    val id: Long?,
    val title: String,
    val platform: String?,
    val topic: String?,
    val difficulty: String?,
    val problemUrl: String?,
    val solved: Boolean?,
    val solvedDate: LocalDate?,
    val notes: String?,
    val createdAt: LocalDateTime?
)
