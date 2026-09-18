package com.example.codetrack.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProblemDao {

    @Insert
    suspend fun insertProblem(problem: ProblemEntity): Long

    @Update
    suspend fun updateProblem(problem: ProblemEntity)

    @Delete
    suspend fun deleteProblem(problem: ProblemEntity)

    @Query("SELECT * FROM problems ORDER BY id DESC")
    fun getAllProblems(): Flow<List<ProblemEntity>>

    @Query("SELECT * FROM problems WHERE id = :id")
    suspend fun getProblemById(id: Int): ProblemEntity?

    @Query("SELECT * FROM problems WHERE solved = 1 ORDER BY solvedDate DESC")
    fun getSolvedProblems(): Flow<List<ProblemEntity>>

    @Query("SELECT * FROM problems WHERE solved = 0 ORDER BY id DESC")
    fun getUnsolvedProblems(): Flow<List<ProblemEntity>>

    @Query("SELECT COUNT(*) FROM problems")
    fun getProblemCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM problems WHERE solved = 1")
    fun getSolvedProblemCount(): Flow<Int>
}