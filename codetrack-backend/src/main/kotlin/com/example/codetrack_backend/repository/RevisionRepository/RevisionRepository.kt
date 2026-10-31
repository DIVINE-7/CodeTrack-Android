
package com.example.codetrack_backend.repository

import com.example.codetrack_backend.entity.Revision
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface RevisionRepository : JpaRepository<Revision, Long> {

    fun findByProblemId(problemId: Long): List<Revision>

    fun findByRevisionDate(revisionDate: LocalDate): List<Revision>

    fun findByProblemIdAndCompleted(
        problemId: Long,
        completed: Boolean
    ): List<Revision>
}
