
package com.example.codetrack_backend.dto

import java.time.LocalDate
import java.time.LocalDateTime

data class RevisionResponse(
    val id: Long?,
    val problemId: Long?,
    val revisionNumber: Int,
    val revisionDate: LocalDate,
    val completed: Boolean?,
    val completedAt: LocalDateTime?,
    val notes: String?
)
