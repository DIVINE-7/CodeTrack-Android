package com.example.codetrack.data.repository

import com.example.codetrack.data.local.ProblemDao
import com.example.codetrack.data.local.ProblemEntity
import com.example.codetrack.data.local.RevisionDao
import com.example.codetrack.data.local.RevisionEntity
import kotlinx.coroutines.flow.Flow

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
}