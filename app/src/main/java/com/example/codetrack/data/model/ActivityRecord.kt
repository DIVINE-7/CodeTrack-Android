package com.example.codetrack.data.model

import java.time.LocalDate

data class ActivityRecord(
    val date: LocalDate,
    val problemsSolved: Int
)
