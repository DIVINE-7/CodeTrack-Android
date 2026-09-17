package com.example.codetrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codetrack.data.model.Goal
import com.example.codetrack.data.model.ActivityRecord
import com.example.codetrack.data.model.DailyProgress
import com.example.codetrack.data.repository.GoalRepository
import com.example.codetrack.data.repository.StreakRepository
import com.example.codetrack.data.repository.ProgressRepository
import kotlinx.coroutines.flow.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ProductivityViewModel(
    private val goalRepository: GoalRepository = GoalRepository.getInstance(),
    private val streakRepository: StreakRepository = StreakRepository.getInstance(),
    private val progressRepository: ProgressRepository = ProgressRepository.getInstance()
) : ViewModel() {
    
    // Daily Goals State
    val goals: StateFlow<List<Goal>> = goalRepository.goals

    val completedTasks: StateFlow<Int> = goals
        .map { it.count { goal -> goal.isChecked } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val progressPercent: StateFlow<Float> = goals
        .map { list ->
            if (list.isEmpty()) 0f else list.count { it.isChecked }.toFloat() / list.size
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0f
        )

    // Streak State
    private val activityRecords = streakRepository.activityRecords

    val currentStreak: StateFlow<Int> = activityRecords
        .map { calculateStreak(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val problemsSolvedThisWeek: StateFlow<Int> = activityRecords
        .map { calculateWeeklyProblems(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val activeDaysThisWeek: StateFlow<Int> = activityRecords
        .map { calculateWeeklyActiveDays(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Daily Progress State
    val dailyProgress: StateFlow<DailyProgress> = progressRepository.dailyProgress

    fun toggleGoal(goalId: Int) {
        val goal = goals.value.find { it.id == goalId }
        if (goal != null) {
            val newCheckedState = !goal.isChecked
            goalRepository.toggleGoal(goalId)
            
            // Sync with DailyProgress for specific goals
            when (goalId) {
                1 -> progressRepository.setDsaGoalCompleted(newCheckedState)
                2 -> progressRepository.setAptitudeGoalCompleted(newCheckedState)
                3 -> progressRepository.setInterviewGoalCompleted(newCheckedState)
            }
        }
    }

    fun recordProblemSolved(count: Int = 1) {
        streakRepository.recordActivity(LocalDate.now(), count)
    }

    fun recordDsaProgress(count: Int = 1) {
        progressRepository.recordDsaProgress(count)
    }

    fun recordAptitudeProgress(count: Int = 1) {
        progressRepository.recordAptitudeProgress(count)
    }

    fun recordInterviewProgress(count: Int = 1) {
        progressRepository.recordInterviewProgress(count)
    }

    private fun calculateStreak(records: List<ActivityRecord>): Int {
        val activeDates = records.filter { it.problemsSolved > 0 }.map { it.date }.toSet()
        if (activeDates.isEmpty()) return 0

        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        var currentSearchDate = when {
            activeDates.contains(today) -> today
            activeDates.contains(yesterday) -> yesterday
            else -> return 0
        }

        var streak = 0
        while (activeDates.contains(currentSearchDate)) {
            streak++
            currentSearchDate = currentSearchDate.minusDays(1)
        }
        return streak
    }

    private fun calculateWeeklyProblems(records: List<ActivityRecord>): Int {
        val startOfWeek = getStartOfCurrentWeek()
        val today = LocalDate.now()
        return records.filter { 
            (it.date.isEqual(startOfWeek) || it.date.isAfter(startOfWeek)) && 
            (it.date.isEqual(today) || it.date.isBefore(today))
        }.sumOf { it.problemsSolved }
    }

    private fun calculateWeeklyActiveDays(records: List<ActivityRecord>): Int {
        val startOfWeek = getStartOfCurrentWeek()
        val today = LocalDate.now()
        return records.filter { 
            it.problemsSolved > 0 &&
            (it.date.isEqual(startOfWeek) || it.date.isAfter(startOfWeek)) && 
            (it.date.isEqual(today) || it.date.isBefore(today))
        }.map { it.date }.distinct().size
    }

    private fun getStartOfCurrentWeek(): LocalDate {
        val today = LocalDate.now()
        // DayOfWeek enum: Monday is 1, Sunday is 7
        val daysToSubtract = (today.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong()
        return today.minusDays(daysToSubtract)
    }
}
