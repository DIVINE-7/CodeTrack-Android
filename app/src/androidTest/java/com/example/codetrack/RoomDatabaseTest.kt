package com.example.codetrack

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.codetrack.data.local.AppDatabase
import com.example.codetrack.data.local.RevisionDao
import com.example.codetrack.data.local.RevisionEntity
import com.example.codetrack.data.model.RevisionCategory
import com.example.codetrack.data.repository.CodeTrackRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class RoomDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var revisionDao: RevisionDao
    private lateinit var repository: CodeTrackRepository
    private lateinit var context: Context

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        revisionDao = database.revisionDao()
        repository = CodeTrackRepository(database.problemDao(), revisionDao)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun testInsertAndRetrieveRevisionThroughRoomDao() = runBlocking {
        val testDate = 1729000000000L
        val revision = RevisionEntity(
            id = 0,
            problemId = 101,
            revisionNumber = 1,
            revisionDate = testDate,
            completed = false,
            category = "DSA",
            problemTitle = "Two Sum",
            topic = "Arrays"
        )

        val insertedId = revisionDao.insertRevision(revision).toInt()
        assertTrue("Inserted ID should be greater than 0", insertedId > 0)

        val retrieved = revisionDao.getRevisionById(insertedId)
        assertNotNull("Retrieved revision should not be null", retrieved)
        assertEquals(insertedId, retrieved!!.id)
        assertEquals(101, retrieved.problemId)
        assertEquals(1, retrieved.revisionNumber)
        assertEquals(testDate, retrieved.revisionDate)
        assertFalse("Completed should be false", retrieved.completed)
        assertEquals("DSA", retrieved.category)
        assertEquals("Two Sum", retrieved.problemTitle)
        assertEquals("Arrays", retrieved.topic)
    }

    @Test
    fun testMarkCompletedAndUndoRevision() = runBlocking {
        val revision = RevisionEntity(
            id = 0,
            problemId = 202,
            revisionNumber = 1,
            revisionDate = System.currentTimeMillis(),
            completed = false,
            category = "DSA",
            problemTitle = "Binary Search",
            topic = "Searching"
        )
        val id = revisionDao.insertRevision(revision).toInt()

        // Mark completed
        repository.markRevisionCompleted(id, true)
        val completedRevision = revisionDao.getRevisionById(id)
        assertNotNull(completedRevision)
        assertTrue("Completed should be true after marking complete", completedRevision!!.completed)

        // Undo completion
        repository.undoRevision(id)
        val undoneRevision = revisionDao.getRevisionById(id)
        assertNotNull(undoneRevision)
        assertFalse("Completed should be false after undoing completion", undoneRevision!!.completed)
    }

    @Test
    fun testDeleteRevision() = runBlocking {
        val revision = RevisionEntity(
            id = 0,
            problemId = 303,
            revisionNumber = 1,
            revisionDate = System.currentTimeMillis(),
            completed = false,
            category = "Aptitude",
            problemTitle = "Speed and Distance",
            topic = "Math"
        )
        val id = revisionDao.insertRevision(revision).toInt()
        assertNotNull(revisionDao.getRevisionById(id))

        repository.deleteRevisionById(id)
        val afterDelete = revisionDao.getRevisionById(id)
        assertNull("Deleted revision should be null", afterDelete)
    }

    @Test
    fun testDuplicatePreventionThroughActualRoomAndRepository() = runBlocking {
        val firstResult = repository.addRevision(
            problemId = 404,
            title = "Valid Parentheses",
            category = RevisionCategory.DSA,
            topic = "Stacks"
        )
        assertTrue("First insertion should succeed", firstResult > 0)

        // Attempting to add duplicate for same problem and category
        val duplicateResult = repository.addRevision(
            problemId = 404,
            title = "Valid Parentheses Duplicate",
            category = RevisionCategory.DSA,
            topic = "Stacks"
        )
        assertEquals("Duplicate insertion should return -1", -1L, duplicateResult)

        // Verify only one entry exists in database
        val all = revisionDao.getAllRevisions().first()
        val problemRevs = all.filter { it.problemId == 404 && it.category == "DSA" }
        assertEquals("Should only have 1 revision for problem 404 in DSA", 1, problemRevs.size)
    }

    @Test
    fun testMigrationVersion1ToVersion2PreservesDataAndSuppliesDefaults() {
        val testDbName = "migration_test_room.db"
        context.deleteDatabase(testDbName)

        // Step 1: Create a genuine version 1 database using the original schema
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(testDbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS `problems` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `title` TEXT NOT NULL,
                            `platform` TEXT NOT NULL,
                            `topic` TEXT NOT NULL,
                            `difficulty` TEXT NOT NULL,
                            `problemUrl` TEXT NOT NULL,
                            `solved` INTEGER NOT NULL,
                            `solvedDate` INTEGER,
                            `notes` TEXT NOT NULL
                        )
                    """.trimIndent())
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS `revisions` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `problemId` INTEGER NOT NULL,
                            `revisionNumber` INTEGER NOT NULL,
                            `revisionDate` INTEGER NOT NULL,
                            `completed` INTEGER NOT NULL
                        )
                    """.trimIndent())
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val v1Helper = FrameworkSQLiteOpenHelperFactory().create(configuration)
        val v1Db = v1Helper.writableDatabase

        // Step 2: Insert a sample version 1 revision (no category, problemTitle, or topic columns)
        val sampleDate = 1710000000000L
        v1Db.execSQL("""
            INSERT INTO revisions (id, problemId, revisionNumber, revisionDate, completed)
            VALUES (42, 555, 3, $sampleDate, 1)
        """.trimIndent())
        v1Db.close()
        v1Helper.close()

        // Step 3: Open database with Room v2 configuration and registered MIGRATION_1_2
        val migratedDb = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            testDbName
        )
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()

        // Step 4: Verify the migrated row is preserved with expected defaults
        val migratedRevision = runBlocking {
            migratedDb.revisionDao().getRevisionById(42)
        }

        assertNotNull("Migrated revision must exist in database", migratedRevision)
        assertEquals(42, migratedRevision!!.id)
        assertEquals(555, migratedRevision.problemId)
        assertEquals(3, migratedRevision.revisionNumber)
        assertEquals(sampleDate, migratedRevision.revisionDate)
        assertTrue("Completed status must be preserved", migratedRevision.completed)

        // Verify default values supplied by MIGRATION_1_2
        assertEquals("Default category must be 'DSA'", "DSA", migratedRevision.category)
        assertEquals("Default problemTitle must be ''", "", migratedRevision.problemTitle)
        assertEquals("Default topic must be ''", "", migratedRevision.topic)

        migratedDb.close()
        context.deleteDatabase(testDbName)
    }
}
