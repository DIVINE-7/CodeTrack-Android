package com.example.codetrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codetrack.data.local.toRevisionItem
import com.example.codetrack.data.model.ActivityRecord
import com.example.codetrack.data.model.DailyProgress
import com.example.codetrack.data.model.Goal
import com.example.codetrack.data.model.RevisionCategory
import com.example.codetrack.data.model.RevisionItem
import com.example.codetrack.data.model.WeeklyStatistics
import com.example.codetrack.data.repository.CodeTrackRepository
import com.example.codetrack.data.repository.GoalRepository
import com.example.codetrack.data.repository.ProgressRepository
import com.example.codetrack.data.repository.StatisticsRepository
import com.example.codetrack.data.repository.StreakRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

class ProductivityViewModel(
    private val goalRepository: GoalRepository = GoalRepository.getInstance(),
    private val streakRepository: StreakRepository = StreakRepository.getInstance(),
    private val progressRepository: ProgressRepository = ProgressRepository.getInstance(),
    private val statisticsRepository: StatisticsRepository = StatisticsRepository.getInstance(),
    private val codeTrackRepository: CodeTrackRepository? = try {
        CodeTrackApplication.repository
    } catch (_: Throwable) {
        null
    }
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

    // Weekly Statistics State
    val weeklyStatistics: StateFlow<WeeklyStatistics> = statisticsRepository.weeklyStats

    // Revision State (Backed by Room database)
    val revisions: StateFlow<List<RevisionItem>> = (
        codeTrackRepository?.getAllRevisions()?.map { entities ->
            entities.map { it.toRevisionItem() }
        } ?: emptyFlow()
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val completedRevisionsCount: StateFlow<Int> = revisions
        .map { it.count { item -> item.isCompleted } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingRevisionsCount: StateFlow<Int> = revisions
        .map { it.count { item -> !item.isCompleted } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val dueTodayRevisionsCount: StateFlow<Int> = revisions
        .map { list ->
            val today = LocalDate.now()
            list.count { item -> !item.isCompleted && item.scheduledDate.isEqual(today) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun toggleGoal(goalId: Int) {
        val goal = goals.value.find { it.id == goalId }
        if (goal != null) {
            val newCheckedState = !goal.isChecked
            goalRepository.toggleGoal(goalId)
            
            // Sync with DailyProgress and WeeklyStatistics for specific goals
            when (goalId) {
                1 -> {
                    progressRepository.setDsaGoalCompleted(newCheckedState)
                    statisticsRepository.setDsaGoalCompleted(newCheckedState)
                }
                2 -> {
                    progressRepository.setAptitudeGoalCompleted(newCheckedState)
                    statisticsRepository.setAptitudeGoalCompleted(newCheckedState)
                }
                3 -> {
                    progressRepository.setInterviewGoalCompleted(newCheckedState)
                    statisticsRepository.setInterviewGoalCompleted(newCheckedState)
                }
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

    fun markRevisionCompleted(id: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            codeTrackRepository?.markRevisionCompleted(id, isCompleted)
        }
    }

    fun addRevision(problemId: Int, title: String, category: RevisionCategory, topic: String) {
        viewModelScope.launch {
            codeTrackRepository?.addRevision(problemId, title, category, topic)
        }
    }

    fun removeRevision(id: Int) {
        viewModelScope.launch {
            codeTrackRepository?.deleteRevisionById(id)
        }
    }
    
    fun removeRevisionByProblemId(problemId: Int, category: RevisionCategory) {
        viewModelScope.launch {
            codeTrackRepository?.deleteRevisionByProblemIdAndCategory(problemId, category)
        }
    }
    
    fun isProblemInRevision(problemId: Int, category: RevisionCategory): Boolean {
        return revisions.value.any { it.problemId == problemId && it.category == category }
    }

    suspend fun isProblemInRevisionAsync(problemId: Int, category: RevisionCategory): Boolean {
        return codeTrackRepository?.isProblemInRevision(problemId, category) ?: false
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
