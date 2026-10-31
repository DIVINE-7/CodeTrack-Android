
package com.example.codetrack_backend.controller

import com.example.codetrack_backend.dto.ProblemRequest
import com.example.codetrack_backend.dto.ProblemResponse
import com.example.codetrack_backend.entity.Problem
import com.example.codetrack_backend.service.ProblemService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/problems")
class ProblemController(
    private val problemService: ProblemService
) {

    private fun toResponse(problem: Problem) = ProblemResponse(
        id = problem.id,
        title = problem.title,
        platform = problem.platform,
        topic = problem.topic,
        difficulty = problem.difficulty,
        problemUrl = problem.problemUrl,
        solved = problem.solved,
        solvedDate = problem.solvedDate,
        notes = problem.notes,
        createdAt = problem.createdAt
    )

    // Get all problems
    @GetMapping
    fun getAllProblems() =
        problemService.getAllProblems().map { toResponse(it) }

    // Get problem by ID
    @GetMapping("/{id}")
    fun getProblemById(@PathVariable id: Long) =
        toResponse(problemService.getProblemById(id))

    // Create a new problem
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createProblem(
        @Valid @RequestBody request: ProblemRequest
    ): ProblemResponse {

        val problem = Problem(
            title = request.title,
            platform = request.platform,
            topic = request.topic,
            difficulty = request.difficulty,
            problemUrl = request.problemUrl,
            solved = request.solved,
            solvedDate = request.solvedDate,
            notes = request.notes
        )

        return toResponse(problemService.createProblem(problem))
    }

    // Update an existing problem
    @PutMapping("/{id}")
    fun updateProblem(
        @PathVariable id: Long,
        @Valid @RequestBody request: ProblemRequest
    ): ProblemResponse {

        val updatedProblem = Problem(
            title = request.title,
            platform = request.platform,
            topic = request.topic,
            difficulty = request.difficulty,
            problemUrl = request.problemUrl,
            solved = request.solved,
            solvedDate = request.solvedDate,
            notes = request.notes
        )

        return toResponse(problemService.updateProblem(id, updatedProblem))
    }

    // Delete a problem
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProblem(@PathVariable id: Long) {
        problemService.deleteProblem(id)
    }

    // Filter problems by solved status
    @GetMapping("/filter/solved")
    fun getBySolvedStatus(@RequestParam solved: Boolean) =
        problemService.getProblemsBySolvedStatus(solved).map { toResponse(it) }

    // Filter problems by platform
    @GetMapping("/filter/platform")
    fun getByPlatform(@RequestParam platform: String) =
        problemService.getProblemsByPlatform(platform).map { toResponse(it) }

    // Filter problems by topic
    @GetMapping("/filter/topic")
    fun getByTopic(@RequestParam topic: String) =
        problemService.getProblemsByTopic(topic).map { toResponse(it) }

    // Filter problems by difficulty
    @GetMapping("/filter/difficulty")
    fun getByDifficulty(@RequestParam difficulty: String) =
        problemService.getProblemsByDifficulty(difficulty).map { toResponse(it) }
}
