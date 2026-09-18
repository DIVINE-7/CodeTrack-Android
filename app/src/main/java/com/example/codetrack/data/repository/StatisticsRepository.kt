package com.example.codetrack.data.repository

import com.example.codetrack.data.model.WeeklyStatistics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class StatisticsRepository {
    
    // Weekly baseline constants
    private val DSA_WEEKLY_BASELINE = 18
    private val APTITUDE_WEEKLY_BASELINE = 35
    private val INTERVIEW_WEEKLY_BASELINE = 9
    
    // Daily differences (Target - Daily Baseline)
    private val DSA_DIFF = 3 // 5 - 2
    private val APTITUDE_DIFF = 5 // 10 - 5
    private val INTERVIEW_DIFF = 2 // 3 - 1

    /**
     * In-memory storage for weekly statistics.
     * These are deterministic demo values as Room/database is not yet integrated.
     */
    private val _weeklyStats = MutableStateFlow(
        WeeklyStatistics(
            dsaCompleted = DSA_WEEKLY_BASELINE,
            dsaTarget = 25,
            aptitudeCompleted = APTITUDE_WEEKLY_BASELINE,
            aptitudeTarget = 50,
            interviewCompleted = INTERVIEW_WEEKLY_BASELINE,
            interviewTarget = 15,
            // Activity for Monday to Sunday
            weeklyActivity = listOf(4, 6, 3, 7, 5, 8, 2)
        )
    )
    
    val weeklyStats: StateFlow<WeeklyStatistics> = _weeklyStats.asStateFlow()

    fun setDsaGoalCompleted(isGoalChecked: Boolean) {
        _weeklyStats.update { current ->
            val newValue = if (isGoalChecked) DSA_WEEKLY_BASELINE + DSA_DIFF else DSA_WEEKLY_BASELINE
            current.copy(dsaCompleted = newValue)
        }
    }

    fun setAptitudeGoalCompleted(isGoalChecked: Boolean) {
        _weeklyStats.update { current ->
            val newValue = if (isGoalChecked) APTITUDE_WEEKLY_BASELINE + APTITUDE_DIFF else APTITUDE_WEEKLY_BASELINE
            current.copy(aptitudeCompleted = newValue)
        }
    }

    fun setInterviewGoalCompleted(isGoalChecked: Boolean) {
        _weeklyStats.update { current ->
            val newValue = if (isGoalChecked) INTERVIEW_WEEKLY_BASELINE + INTERVIEW_DIFF else INTERVIEW_WEEKLY_BASELINE
            current.copy(interviewCompleted = newValue)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: StatisticsRepository? = null

        fun getInstance(): StatisticsRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = StatisticsRepository()
                INSTANCE = instance
                instance
            }
        }
    }
}
