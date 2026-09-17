package com.example.codetrack.data.repository

import com.example.codetrack.data.model.Goal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GoalRepository {
    private val _goals = MutableStateFlow(
        listOf(
            Goal(1, "Solve 5 DSA problems"),
            Goal(2, "Practice 10 aptitude questions"),
            Goal(3, "Revise 3 interview questions"),
            Goal(4, "Watch 1 recommended learning video"),
            Goal(5, "Participate in a coding contest")
        )
    )
    val goals: StateFlow<List<Goal>> = _goals.asStateFlow()

    fun toggleGoal(goalId: Int) {
        _goals.update { currentGoals ->
            currentGoals.map {
                if (it.id == goalId) it.copy(isChecked = !it.isChecked) else it
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: GoalRepository? = null

        fun getInstance(): GoalRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = GoalRepository()
                INSTANCE = instance
                instance
            }
        }
    }
}
