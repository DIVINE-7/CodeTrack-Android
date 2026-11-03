package com.example.codetrack.data.repository

import com.example.codetrack.data.local.ProblemDao
import com.example.codetrack.data.local.ProblemEntity
import com.example.codetrack.data.local.RevisionDao
import com.example.codetrack.data.local.RevisionEntity
import com.example.codetrack.data.model.RevisionCategory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId

class CodeTrackRepository(
    private val problemDao: ProblemDao,
    private val revisionDao: RevisionDao
) {

    // -------------------------
    // Problem operations
    // -------------------------

    suspend fun insertProblem(problem: ProblemEntity): Long {
        return problemDao.insertProblem(problem)
    }

    suspend fun updateProblem(problem: ProblemEntity) {
        problemDao.updateProblem(problem)
    }

    suspend fun deleteProblem(problem: ProblemEntity) {
        problemDao.deleteProblem(problem)
    }

    fun getAllProblems(): Flow<List<ProblemEntity>> {
        return problemDao.getAllProblems()
    }

    suspend fun getProblemById(id: Int): ProblemEntity? {
        return problemDao.getProblemById(id)
    }

    fun getSolvedProblems(): Flow<List<ProblemEntity>> {
        return problemDao.getSolvedProblems()
    }

    fun getUnsolvedProblems(): Flow<List<ProblemEntity>> {
        return problemDao.getUnsolvedProblems()
    }

    fun getProblemCount(): Flow<Int> {
        return problemDao.getProblemCount()
    }

    fun getSolvedProblemCount(): Flow<Int> {
        return problemDao.getSolvedProblemCount()
    }

    // -------------------------
    // Revision operations
    // -------------------------

    suspend fun insertRevision(revision: RevisionEntity): Long {
        return revisionDao.insertRevision(revision)
    }

    suspend fun updateRevision(revision: RevisionEntity) {
        revisionDao.updateRevision(revision)
    }

    suspend fun deleteRevision(revision: RevisionEntity) {
        revisionDao.deleteRevision(revision)
    }

    fun getAllRevisions(): Flow<List<RevisionEntity>> {
        return revisionDao.getAllRevisions()
    }

    fun getRevisionsForProblem(problemId: Int): Flow<List<RevisionEntity>> {
        return revisionDao.getRevisionsForProblem(problemId)
    }

    fun getDueRevisions(currentTime: Long): Flow<List<RevisionEntity>> {
        return revisionDao.getDueRevisions(currentTime)
    }

    fun getPendingRevisions(): Flow<List<RevisionEntity>> {
        return revisionDao.getPendingRevisions()
    }

    suspend fun getRevisionById(id: Int): RevisionEntity? {
        return revisionDao.getRevisionById(id)
    }

    fun getCompletedRevisionCount(): Flow<Int> {
        return revisionDao.getCompletedRevisionCount()
    }

    fun getPendingRevisionCount(): Flow<Int> {
        return revisionDao.getPendingRevisionCount()
    }

    fun getTotalRevisionCount(): Flow<Int> {
        return revisionDao.getTotalRevisionCount()
    }

    suspend fun addRevision(
        problemId: Int,
        title: String,
        category: String,
        topic: String,
        revisionDate: Long = System.currentTimeMillis()
    ): Long {
        val existing = revisionDao.getRevisionByProblemAndCategory(problemId, category)
        if (existing != null) {
            return -1L
        }
        val entity = RevisionEntity(
            problemId = problemId,
            revisionNumber = 1,
            revisionDate = revisionDate,
            completed = false,
            category = category,
            problemTitle = title,
            topic = topic
        )
        return revisionDao.insertRevision(entity)
    }

    suspend fun addRevision(
        problemId: Int,
        title: String,
        category: RevisionCategory,
        topic: String,
        revisionDate: Long = System.currentTimeMillis()
    ): Long {
        return addRevision(problemId, title, category.name, topic, revisionDate)
    }

    suspend fun markRevisionCompleted(id: Int, isCompleted: Boolean) {
        revisionDao.updateCompletionStatus(id, isCompleted)
    }

    suspend fun undoRevision(id: Int) {
        markRevisionCompleted(id, false)
    }

    suspend fun deleteRevisionById(id: Int) {
        revisionDao.deleteRevisionById(id)
    }

    suspend fun deleteRevisionByProblemIdAndCategory(problemId: Int, category: String) {
        revisionDao.deleteRevisionByProblemIdAndCategory(problemId, category)
    }

    suspend fun deleteRevisionByProblemIdAndCategory(problemId: Int, category: RevisionCategory) {
        deleteRevisionByProblemIdAndCategory(problemId, category.name)
    }

    suspend fun isProblemInRevision(problemId: Int, category: String): Boolean {
        return revisionDao.getRevisionByProblemAndCategory(problemId, category) != null
    }

    suspend fun isProblemInRevision(problemId: Int, category: RevisionCategory): Boolean {
        return isProblemInRevision(problemId, category.name)
    }

    fun getRevisionsDueToday(): Flow<List<RevisionEntity>> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val endOfDay = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        return revisionDao.getRevisionsDueBetween(startOfDay, endOfDay)
    }
}
