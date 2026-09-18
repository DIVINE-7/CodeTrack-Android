package com.example.codetrack.data.repository

import com.example.codetrack.data.model.RevisionCategory
import com.example.codetrack.data.model.RevisionItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class RevisionRepository {
    private val _revisions = MutableStateFlow<List<RevisionItem>>(emptyList())
    val revisions: StateFlow<List<RevisionItem>> = _revisions.asStateFlow()

    private var nextId = 1

    fun markCompleted(id: Int, isCompleted: Boolean) {
        _revisions.update { current ->
            current.map {
                if (it.id == id) it.copy(isCompleted = isCompleted) else it
            }
        }
    }

    fun addRevision(problemId: Int, title: String, category: RevisionCategory, topic: String) {
        _revisions.update { current ->
            if (current.any { it.problemId == problemId && it.category == category }) {
                current
            } else {
                val newItem = RevisionItem(
                    id = nextId++,
                    problemId = problemId,
                    title = title,
                    category = category,
                    topic = topic,
                    scheduledDate = LocalDate.now(),
                    isCompleted = false
                )
                current + newItem
            }
        }
    }

    fun removeRevision(id: Int) {
        _revisions.update { it.filterNot { item -> item.id == id } }
    }
    
    fun removeRevisionByProblemId(problemId: Int, category: RevisionCategory) {
        _revisions.update { it.filterNot { item -> item.problemId == problemId && item.category == category } }
    }

    fun isProblemInRevision(problemId: Int, category: RevisionCategory): Boolean {
        return _revisions.value.any { it.problemId == problemId && it.category == category }
    }

    companion object {
        @Volatile
        private var INSTANCE: RevisionRepository? = null

        fun getInstance(): RevisionRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = RevisionRepository()
                INSTANCE = instance
                instance
            }
        }
    }
}
