package com.example.codetrack.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RevisionDao {

    @Insert
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
}