package com.example.codetrack.data.model

import java.time.LocalDate

enum class RevisionCategory(val displayName: String) {
    DSA("DSA"),
    APTITUDE("Aptitude"),
    INTERVIEW("Interview")
}

data class RevisionItem(
    val id: Int,
    val problemId: Int,
    val title: String,
    val category: RevisionCategory,
    val topic: String,
    val scheduledDate: LocalDate,
    val isCompleted: Boolean = false
)
