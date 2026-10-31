
package com.example.codetrack_backend.service

import com.example.codetrack_backend.entity.Revision
import com.example.codetrack_backend.repository.ProblemRepository
import com.example.codetrack_backend.repository.RevisionRepository
import com.example.codetrack_backend.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class RevisionService(
    private val revisionRepository: RevisionRepository,
    private val problemRepository: ProblemRepository
) {

    // Get all revisions
    fun getAllRevisions(): List<Revision> {
        return revisionRepository.findAll()
    }

    // Get revision by ID
    fun getRevisionById(id: Long): Revision {
        return revisionRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException("Revision not found with id: $id")
            }
    }

    // Get revisions by problem ID
    fun getRevisionsByProblem(problemId: Long): List<Revision> {
        return revisionRepository.findByProblemId(problemId)
    }

    // Get revisions by date
    fun getRevisionsByDate(date: LocalDate): List<Revision> {
        return revisionRepository.findByRevisionDate(date)
    }

    // Create a revision for a problem
    fun createRevision(problemId: Long, revision: Revision): Revision {
        val problem = problemRepository.findById(problemId)
            .orElseThrow {
                ResourceNotFoundException("Problem not found with id: $problemId")
            }

        revision.problem = problem
        return revisionRepository.save(revision)
    }

    // Update an existing revision
    fun updateRevision(id: Long, updatedRevision: Revision): Revision {
        val existingRevision = getRevisionById(id)

        existingRevision.revisionNumber = updatedRevision.revisionNumber
        existingRevision.revisionDate = updatedRevision.revisionDate
        existingRevision.completed = updatedRevision.completed
        existingRevision.completedAt = updatedRevision.completedAt
        existingRevision.notes = updatedRevision.notes

        return revisionRepository.save(existingRevision)
    }

    // Mark a revision as completed
    fun markRevisionCompleted(id: Long): Revision {
        val revision = getRevisionById(id)

        revision.completed = true
        revision.completedAt = LocalDateTime.now()

        return revisionRepository.save(revision)
    }

    // Delete a revision
    fun deleteRevision(id: Long) {
        val revision = getRevisionById(id)
        revisionRepository.delete(revision)
    }
}
