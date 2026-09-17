package com.example.codetrack.data.repository

import com.example.codetrack.data.model.DailyProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProgressRepository {
    // Demo baseline values
    private val DSA_DEMO_COMPLETED = 2
    private val APTITUDE_DEMO_COMPLETED = 5
    private val INTERVIEW_DEMO_COMPLETED = 1

    // In-memory storage for daily progress with demo data
    private val _dailyProgress = MutableStateFlow(
        DailyProgress(
            dsaCompleted = DSA_DEMO_COMPLETED,
            dsaTarget = 5,
            aptitudeCompleted = APTITUDE_DEMO_COMPLETED,
            aptitudeTarget = 10,
            interviewCompleted = INTERVIEW_DEMO_COMPLETED,
            interviewTarget = 3
        )
    )
    val dailyProgress: StateFlow<DailyProgress> = _dailyProgress.asStateFlow()

    fun recordDsaProgress(count: Int = 1) {
        _dailyProgress.update { current ->
            val newValue = (current.dsaCompleted + count).coerceIn(0, current.dsaTarget)
            current.copy(dsaCompleted = newValue)
        }
    }

    fun setDsaGoalCompleted(isGoalChecked: Boolean) {
        _dailyProgress.update { current ->
            val newValue = if (isGoalChecked) current.dsaTarget else DSA_DEMO_COMPLETED
            current.copy(dsaCompleted = newValue)
        }
    }

    fun recordAptitudeProgress(count: Int = 1) {
        _dailyProgress.update { current ->
            val newValue = (current.aptitudeCompleted + count).coerceIn(0, current.aptitudeTarget)
            current.copy(aptitudeCompleted = newValue)
        }
    }

    fun setAptitudeGoalCompleted(isGoalChecked: Boolean) {
        _dailyProgress.update { current ->
            val newValue = if (isGoalChecked) current.aptitudeTarget else APTITUDE_DEMO_COMPLETED
            current.copy(aptitudeCompleted = newValue)
        }
    }

    fun recordInterviewProgress(count: Int = 1) {
        _dailyProgress.update { current ->
            val newValue = (current.interviewCompleted + count).coerceIn(0, current.interviewTarget)
            current.copy(interviewCompleted = newValue)
        }
    }

    fun setInterviewGoalCompleted(isGoalChecked: Boolean) {
        _dailyProgress.update { current ->
            val newValue = if (isGoalChecked) current.interviewTarget else INTERVIEW_DEMO_COMPLETED
            current.copy(interviewCompleted = newValue)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ProgressRepository? = null

        fun getInstance(): ProgressRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = ProgressRepository()
                INSTANCE = instance
                instance
            }
        }
    }
}
