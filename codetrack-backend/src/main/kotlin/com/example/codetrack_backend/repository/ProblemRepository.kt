
package com.example.codetrack_backend.repository

import com.example.codetrack_backend.entity.Problem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProblemRepository : JpaRepository<Problem, Long> {

    fun findBySolved(solved: Boolean): List<Problem>

    fun findByPlatformIgnoreCase(platform: String): List<Problem>

    fun findByTopicIgnoreCase(topic: String): List<Problem>

    fun findByDifficultyIgnoreCase(difficulty: String): List<Problem>
}
