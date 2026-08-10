package com.bolskapps.libri.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Migration coverage. Requires a device or emulator — `./gradlew connectedDebugAndroidTest`.
 *
 * There is no `fallbackToDestructiveMigration()`, so a broken migration crashes on
 * upgrade instead of silently deleting a library. That makes this the highest-value
 * test in the project, and the one that must be extended for every schema bump.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private companion object {
        const val TEST_DB = "migration-test"
    }

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        BookDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate1To2_preservesExistingBooks() {
        helper.createDatabase(TEST_DB, 1).use { db ->
            db.execSQL(
                """
                INSERT INTO books
                  (id, title, author, currentPage, totalPages, status, rating, notes,
                   genre, coverId, openLibraryKey, addedAt)
                VALUES
                  (1, 'Dune', 'Frank Herbert', 150, 300, 'READING', 0, NULL,
                   NULL, 6976407, '/works/OL893516W', 1700000000000)
                """.trimIndent()
            )
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        db.query("SELECT title, currentPage, finishedAt FROM books WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Dune", cursor.getString(0))
            assertEquals(150, cursor.getInt(1))
            // Never backfilled: the real finish date is unknown, and stamping "now"
            // would dump a whole back catalogue into this year's goal.
            assertTrue(cursor.isNull(2))
        }
    }

    @Test
    fun migrate1To2_addsTheNewTables() {
        helper.createDatabase(TEST_DB, 1).close()

        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        db.query("SELECT COUNT(*) FROM reading_sessions").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
        db.query("SELECT COUNT(*) FROM reading_goals").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
    }

    @Test
    fun migrate1To2_finishedBooksKeepNoCompletionDate() {
        helper.createDatabase(TEST_DB, 1).use { db ->
            db.execSQL(
                """
                INSERT INTO books
                  (id, title, author, currentPage, totalPages, status, rating, notes,
                   genre, coverId, openLibraryKey, addedAt)
                VALUES
                  (2, 'Old', 'Someone', 200, 200, 'FINISHED', 5, NULL,
                   NULL, NULL, NULL, 1600000000000)
                """.trimIndent()
            )
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        db.query("SELECT rating, finishedAt FROM books WHERE id = 2").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(5, cursor.getInt(0))
            assertTrue(cursor.isNull(1))
        }
    }

    /**
     * Opens the migrated database through Room itself. This is what catches a schema
     * that merely looks right: Room compares an identity hash and refuses to open on
     * any mismatch, including a stray column default.
     */
    @Test
    fun migratedDatabaseOpensThroughRoom() {
        helper.createDatabase(TEST_DB, 1).close()
        helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val room = androidx.room.Room.databaseBuilder(context, BookDatabase::class.java, TEST_DB)
            .addMigrations(*ALL_MIGRATIONS)
            .build()

        try {
            // Forces the open + schema validation to actually happen.
            runBlocking { assertNull(room.bookDao().getBookById(999)) }
        } finally {
            room.close()
        }
    }
}
