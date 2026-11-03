package com.example.codetrack

import com.example.codetrack.data.local.AppDatabase
import com.example.codetrack.data.local.RevisionDao
import com.example.codetrack.data.local.RevisionEntity
import com.example.codetrack.data.local.toRevisionEntity
import com.example.codetrack.data.local.toRevisionItem
import com.example.codetrack.data.model.RevisionCategory
import com.example.codetrack.data.model.RevisionItem
import com.example.codetrack.data.repository.CodeTrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RevisionIntegrationTest {

    @Test
    fun testRevisionEntityDefaultsAndMapping() {
        val entity = RevisionEntity(
            id = 1,
            problemId = 42,
            revisionNumber = 1,
            revisionDate = System.currentTimeMillis(),
            completed = false,
            category = "DSA",
            problemTitle = "Two Sum",
            topic = "Arrays"
        )

        val item = entity.toRevisionItem()
        assertEquals(1, item.id)
        assertEquals(42, item.problemId)
        assertEquals("Two Sum", item.title)
        assertEquals(RevisionCategory.DSA, item.category)
        assertEquals("Arrays", item.topic)
        assertFalse(item.isCompleted)
        assertEquals(LocalDate.now(), item.scheduledDate)

        val convertedEntity = item.toRevisionEntity(revisionNumber = 2)
        assertEquals(1, convertedEntity.id)
        assertEquals(42, convertedEntity.problemId)
        assertEquals(2, convertedEntity.revisionNumber)
        assertEquals("DSA", convertedEntity.category)
        assertEquals("Two Sum", convertedEntity.problemTitle)
        assertEquals("Arrays", convertedEntity.topic)
        assertFalse(convertedEntity.completed)
    }

    @Test
    fun testRevisionEntityFallbackForEmptyTitle() {
        val entity = RevisionEntity(
            id = 10,
            problemId = 99,
            revisionNumber = 1,
            revisionDate = System.currentTimeMillis(),
            completed = false,
            category = "INTERVIEW",
            problemTitle = "",
            topic = "System Design"
        )

        val item = entity.toRevisionItem()
        assertEquals("Problem #99", item.title)
        assertEquals(RevisionCategory.INTERVIEW, item.category)
    }

    @Test
    fun testMigrationDefinition() {
        assertEquals(1, AppDatabase.MIGRATION_1_2.startVersion)
        assertEquals(2, AppDatabase.MIGRATION_1_2.endVersion)
    }

    @Test
    fun testRepositoryDuplicatePreventionAndActions() = runBlocking {
        val fakeDao = FakeRevisionDao()
        val fakeProblemDao = FakeProblemDao()
        val repository = CodeTrackRepository(fakeProblemDao, fakeDao)

        // Add first revision
        val id1 = repository.addRevision(
            problemId = 1,
            title = "Two Sum",
            category = RevisionCategory.DSA,
            topic = "Arrays"
        )
        assertEquals(1L, id1)

        // Attempt duplicate addition for same problem and category
        val idDup = repository.addRevision(
            problemId = 1,
            title = "Two Sum Duplicate",
            category = RevisionCategory.DSA,
            topic = "Arrays"
        )
        assertEquals(-1L, idDup)

        // Checking isProblemInRevision
        assertTrue(repository.isProblemInRevision(1, RevisionCategory.DSA))
        assertFalse(repository.isProblemInRevision(1, RevisionCategory.INTERVIEW))
        assertFalse(repository.isProblemInRevision(2, RevisionCategory.DSA))

        // Mark completed
        repository.markRevisionCompleted(1, true)
        val allRevs = repository.getAllRevisions().first()
        assertEquals(1, allRevs.size)
        assertTrue(allRevs.first().completed)

        // Undo completion
        repository.undoRevision(1)
        val afterUndo = repository.getAllRevisions().first()
        assertFalse(afterUndo.first().completed)

        // Delete revision
        repository.deleteRevisionByProblemIdAndCategory(1, RevisionCategory.DSA)
        assertFalse(repository.isProblemInRevision(1, RevisionCategory.DSA))
        assertEquals(0, repository.getAllRevisions().first().size)
    }

    private class FakeProblemDao : com.example.codetrack.data.local.ProblemDao {
        override suspend fun insertProblem(problem: com.example.codetrack.data.local.ProblemEntity): Long = 1L
        override suspend fun updateProblem(problem: com.example.codetrack.data.local.ProblemEntity) {}
        override suspend fun deleteProblem(problem: com.example.codetrack.data.local.ProblemEntity) {}
        override fun getAllProblems(): Flow<List<com.example.codetrack.data.local.ProblemEntity>> = flowOf(emptyList())
        override suspend fun getProblemById(id: Int): com.example.codetrack.data.local.ProblemEntity? = null
        override fun getSolvedProblems(): Flow<List<com.example.codetrack.data.local.ProblemEntity>> = flowOf(emptyList())
        override fun getUnsolvedProblems(): Flow<List<com.example.codetrack.data.local.ProblemEntity>> = flowOf(emptyList())
        override fun getProblemCount(): Flow<Int> = flowOf(0)
        override fun getSolvedProblemCount(): Flow<Int> = flowOf(0)
    }

    private class FakeRevisionDao : RevisionDao {
        private val list = mutableListOf<RevisionEntity>()
        private val flow = MutableStateFlow<List<RevisionEntity>>(emptyList())
        private var nextId = 1

        private fun notifyChange() {
            flow.value = list.toList()
        }

        override suspend fun insertRevision(revision: RevisionEntity): Long {
            val assignedId = if (revision.id == 0) nextId++ else revision.id
            val entity = revision.copy(id = assignedId)
            list.add(entity)
            notifyChange()
            return assignedId.toLong()
        }

        override suspend fun updateRevision(revision: RevisionEntity) {
            val idx = list.indexOfFirst { it.id == revision.id }
            if (idx != -1) {
                list[idx] = revision
                notifyChange()
            }
        }

        override suspend fun deleteRevision(revision: RevisionEntity) {
            list.removeAll { it.id == revision.id }
            notifyChange()
        }

        override fun getAllRevisions(): Flow<List<RevisionEntity>> = flow

        override fun getRevisionsForProblem(problemId: Int): Flow<List<RevisionEntity>> =
            flowOf(list.filter { it.problemId == problemId })

        override fun getDueRevisions(currentTime: Long): Flow<List<RevisionEntity>> =
            flowOf(list.filter { it.revisionDate <= currentTime && !it.completed })

        override fun getPendingRevisions(): Flow<List<RevisionEntity>> =
            flowOf(list.filter { !it.completed })

        override suspend fun getRevisionById(id: Int): RevisionEntity? =
            list.find { it.id == id }

        override fun getCompletedRevisionCount(): Flow<Int> =
            flowOf(list.count { it.completed })

        override fun getPendingRevisionCount(): Flow<Int> =
            flowOf(list.count { !it.completed })

        override fun getTotalRevisionCount(): Flow<Int> =
            flowOf(list.size)

        override suspend fun getRevisionByProblemAndCategory(problemId: Int, category: String): RevisionEntity? =
            list.find { it.problemId == problemId && it.category == category }

        override suspend fun updateCompletionStatus(id: Int, completed: Boolean) {
            val idx = list.indexOfFirst { it.id == id }
            if (idx != -1) {
                list[idx] = list[idx].copy(completed = completed)
                notifyChange()
            }
        }

        override suspend fun deleteRevisionById(id: Int) {
            list.removeAll { it.id == id }
            notifyChange()
        }

        override suspend fun deleteRevisionByProblemIdAndCategory(problemId: Int, category: String) {
            list.removeAll { it.problemId == problemId && it.category == category }
            notifyChange()
        }

        override fun getRevisionsDueBetween(startMillis: Long, endMillis: Long): Flow<List<RevisionEntity>> =
            flowOf(list.filter { it.revisionDate in startMillis..endMillis })
    }
}
