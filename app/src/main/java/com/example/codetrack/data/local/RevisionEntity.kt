package com.example.codetrack.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "revisions")
data class RevisionEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val problemId: Int,
    val revisionNumber: Int,
    val revisionDate: Long,
    val completed: Boolean = false
)