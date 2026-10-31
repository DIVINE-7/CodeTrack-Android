
package com.example.codetrack_backend.controller

import com.example.codetrack_backend.dto.RevisionRequest
import com.example.codetrack_backend.dto.RevisionResponse
import com.example.codetrack_backend.entity.Revision
import com.example.codetrack_backend.service.RevisionService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/revisions")
class RevisionController(
    private val revisionService: RevisionService
) {

    private fun toResponse(revision: Revision) = RevisionResponse(
        id = revision.id,
        problemId = revision.problem?.id,
        revisionNumber = revision.revisionNumber,
        revisionDate = revision.revisionDate,
        completed = revision.completed,
        completedAt = revision.completedAt,
        notes = revision.notes
    )

    // Get all revisions
    @GetMapping
    fun getAllRevisions() =
        revisionService.getAllRevisions().map { toResponse(it) }

    // Get revision by ID
    @GetMapping("/{id}")
    fun getRevisionById(@PathVariable id: Long) =
        toResponse(revisionService.getRevisionById(id))

    // Get revisions by problem ID
    @GetMapping("/problem/{problemId}")
    fun getRevisionsByProblem(@PathVariable problemId: Long) =
        revisionService.getRevisionsByProblem(problemId).map { toResponse(it) }

    // Get revisions by date
    @GetMapping("/date/{date}")
    fun getRevisionsByDate(@PathVariable date: String) =
        revisionService.getRevisionsByDate(LocalDate.parse(date)).map { toResponse(it) }

    // Create a revision
    @PostMapping("/problem/{problemId}")
    @ResponseStatus(HttpStatus.CREATED)
    fun createRevision(
        @PathVariable problemId: Long,
        @Valid @RequestBody request: RevisionRequest
    ): RevisionResponse {

        val revision = Revision(
            revisionNumber = request.revisionNumber,
            revisionDate = request.revisionDate,
            completed = request.completed,
            notes = request.notes
        )

        return toResponse(revisionService.createRevision(problemId, revision))
    }

    // Update a revision
    @PutMapping("/{id}")
    fun updateRevision(
        @PathVariable id: Long,
        @Valid @RequestBody request: RevisionRequest
    ): RevisionResponse {

        val updatedRevision = Revision(
            revisionNumber = request.revisionNumber,
            revisionDate = request.revisionDate,
            completed = request.completed,
            notes = request.notes
        )

        return toResponse(revisionService.updateRevision(id, updatedRevision))
    }

    // Mark a revision as completed
    @PatchMapping("/{id}/complete")
    fun markRevisionCompleted(@PathVariable id: Long) =
        toResponse(revisionService.markRevisionCompleted(id))

    // Delete a revision
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteRevision(@PathVariable id: Long) {
        revisionService.deleteRevision(id)
    }
}
