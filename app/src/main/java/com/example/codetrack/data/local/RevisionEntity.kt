package com.example.codetrack.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.codetrack.data.model.RevisionCategory
import com.example.codetrack.data.model.RevisionItem
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Entity(tableName = "revisions")
data class RevisionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val problemId: Int,
    val revisionNumber: Int = 1,
    val revisionDate: Long = System.currentTimeMillis(),
    val completed: Boolean = false,
    val category: String = "DSA",
    val problemTitle: String = "",
    val topic: String = ""
)

fun RevisionEntity.toRevisionItem(): RevisionItem {
    val date = try {
        Instant.ofEpochMilli(revisionDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    } catch (_: Exception) {
        LocalDate.now()
    }
    val cat = try {
        RevisionCategory.valueOf(category.uppercase())
    } catch (_: Exception) {
        RevisionCategory.entries.find { it.displayName.equals(category, ignoreCase = true) }
            ?: RevisionCategory.DSA
    }
    return RevisionItem(
        id = id,
        problemId = problemId,
        title = if (problemTitle.isNotBlank()) problemTitle else "Problem #$problemId",
        category = cat,
        topic = topic,
        scheduledDate = date,
        isCompleted = completed
    )
}

fun RevisionItem.toRevisionEntity(revisionNumber: Int = 1): RevisionEntity {
    val epochMillis = try {
        scheduledDate.atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    } catch (_: Exception) {
        System.currentTimeMillis()
    }
    return RevisionEntity(
        id = id,
        problemId = problemId,
        revisionNumber = revisionNumber,
        revisionDate = epochMillis,
        completed = isCompleted,
        category = category.name,
        problemTitle = title,
        topic = topic
    )
}
