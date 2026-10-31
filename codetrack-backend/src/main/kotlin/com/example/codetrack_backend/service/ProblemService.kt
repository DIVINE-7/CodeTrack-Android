package com.example.codetrack_backend.service

import com.example.codetrack_backend.entity.Problem
import com.example.codetrack_backend.repository.ProblemRepository
import org.springframework.stereotype.Service
import com.example.codetrack_backend.exception.ResourceNotFoundException

@Service
class ProblemService(
    private val problemRepository: ProblemRepository
) {

    fun getAllProblems(): List<Problem> {
        return problemRepository.findAll()
    }

    fun getProblemById(id: Long): Problem {
        return problemRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException("Problem not found with id: $id")
            }
    }

    fun createProblem(problem: Problem): Problem {
        return problemRepository.save(problem)
    }

    fun updateProblem(id: Long, updatedProblem: Problem): Problem {
        val existingProblem = getProblemById(id)

        existingProblem.title = updatedProblem.title
        existingProblem.platform = updatedProblem.platform
        existingProblem.topic = updatedProblem.topic
        existingProblem.difficulty = updatedProblem.difficulty
        existingProblem.problemUrl = updatedProblem.problemUrl
        existingProblem.solved = updatedProblem.solved
        existingProblem.solvedDate = updatedProblem.solvedDate
        existingProblem.notes = updatedProblem.notes

        return problemRepository.save(existingProblem)
    }

    fun deleteProblem(id: Long) {
        val problem = getProblemById(id)
        problemRepository.delete(problem)
    }

    fun getProblemsBySolvedStatus(solved: Boolean): List<Problem> {
        return problemRepository.findBySolved(solved)
    }

    fun getProblemsByPlatform(platform: String): List<Problem> {
        return problemRepository.findByPlatformIgnoreCase(platform)
    }

    fun getProblemsByTopic(topic: String): List<Problem> {
        return problemRepository.findByTopicIgnoreCase(topic)
    }

    fun getProblemsByDifficulty(difficulty: String): List<Problem> {
        return problemRepository.findByDifficultyIgnoreCase(difficulty)
    }
}
