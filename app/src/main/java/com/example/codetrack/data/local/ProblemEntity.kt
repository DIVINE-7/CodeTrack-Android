package com.example.codetrack.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "problems")
data class ProblemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val platform: String,
    val topic: String,
    val difficulty: String,
    val problemUrl: String = "",
    val solved: Boolean = false,
    val solvedDate: Long? = null,
    val notes: String = ""
)