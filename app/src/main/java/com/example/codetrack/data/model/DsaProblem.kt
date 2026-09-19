package com.example.codetrack.data.model

data class DsaProblem(
    val id: Int,
    val title: String,
    val difficulty: String, // "Easy", "Medium", "Hard"
    val topic: String, // "Arrays", "Strings", "Linked List", etc.
    val description: String,
    val exampleInput: String,
    val exampleOutput: String,
    val explanation: String,
    val constraints: String,
    val hint: String,
    val isSolved: Boolean = false
)
