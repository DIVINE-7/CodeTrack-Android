package com.example.codetrack.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RevisionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRevision(revision: RevisionEntity): Long

    @Update
    suspend fun updateRevision(revision: RevisionEntity)

    @Delete
    suspend fun deleteRevision(revision: RevisionEntity)

    @Query("SELECT * FROM revisions ORDER BY revisionDate ASC")
    fun getAllRevisions(): Flow<List<RevisionEntity>>

    @Query("SELECT * FROM revisions WHERE problemId = :problemId ORDER BY revisionNumber ASC")
    fun getRevisionsForProblem(problemId: Int): Flow<List<RevisionEntity>>

    @Query("SELECT * FROM revisions WHERE revisionDate <= :currentTime AND completed = 0 ORDER BY revisionDate ASC")
    fun getDueRevisions(currentTime: Long): Flow<List<RevisionEntity>>

    @Query("SELECT * FROM revisions WHERE completed = 0 ORDER BY revisionDate ASC")
    fun getPendingRevisions(): Flow<List<RevisionEntity>>

    @Query("SELECT * FROM revisions WHERE id = :id")
    suspend fun getRevisionById(id: Int): RevisionEntity?

    @Query("SELECT COUNT(*) FROM revisions WHERE completed = 1")
    fun getCompletedRevisionCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM revisions WHERE completed = 0")
    fun getPendingRevisionCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM revisions")
    fun getTotalRevisionCount(): Flow<Int>

    @Query("SELECT * FROM revisions WHERE problemId = :problemId AND category = :category LIMIT 1")
    suspend fun getRevisionByProblemAndCategory(problemId: Int, category: String): RevisionEntity?

    @Query("UPDATE revisions SET completed = :completed WHERE id = :id")
    suspend fun updateCompletionStatus(id: Int, completed: Boolean)

    @Query("DELETE FROM revisions WHERE id = :id")
    suspend fun deleteRevisionById(id: Int)

    @Query("DELETE FROM revisions WHERE problemId = :problemId AND category = :category")
    suspend fun deleteRevisionByProblemIdAndCategory(problemId: Int, category: String)

    @Query("SELECT * FROM revisions WHERE revisionDate BETWEEN :startMillis AND :endMillis ORDER BY revisionDate ASC")
    fun getRevisionsDueBetween(startMillis: Long, endMillis: Long): Flow<List<RevisionEntity>>
}
