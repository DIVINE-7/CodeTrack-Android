package com.example.codetrack.data.repository

import com.example.codetrack.data.model.ActivityRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class StreakRepository {
    // In-memory storage for activity records
    private val _activityRecords = MutableStateFlow<List<ActivityRecord>>(emptyList())
    val activityRecords: StateFlow<List<ActivityRecord>> = _activityRecords.asStateFlow()

    init {
        // Adding deterministic demo data: Active for the last 5 days including today
        val today = LocalDate.now()
        val demoRecords = listOf(
            ActivityRecord(today, 5),
            ActivityRecord(today.minusDays(1), 8),
            ActivityRecord(today.minusDays(2), 12),
            ActivityRecord(today.minusDays(3), 4),
            ActivityRecord(today.minusDays(4), 10),
            // A gap to test streak logic
            ActivityRecord(today.minusDays(7), 6)
        )
        _activityRecords.value = demoRecords
    }

    fun recordActivity(date: LocalDate, count: Int) {
        _activityRecords.update { currentRecords ->
            val existingRecord = currentRecords.find { it.date == date }
            if (existingRecord != null) {
                currentRecords.map {
                    if (it.date == date) it.copy(problemsSolved = it.problemsSolved + count) else it
                }
            } else {
                currentRecords + ActivityRecord(date, count)
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: StreakRepository? = null

        fun getInstance(): StreakRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = StreakRepository()
                INSTANCE = instance
                instance
            }
        }
    }
}
