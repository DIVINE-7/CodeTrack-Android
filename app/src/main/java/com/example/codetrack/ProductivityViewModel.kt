package com.example.codetrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codetrack.data.model.Goal
import com.example.codetrack.data.repository.GoalRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ProductivityViewModel(
    private val repository: GoalRepository = GoalRepository.getInstance()
) : ViewModel() {
    
    val goals: StateFlow<List<Goal>> = repository.goals

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

    fun toggleGoal(goalId: Int) {
        repository.toggleGoal(goalId)
    }
}
