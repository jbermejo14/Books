package com.bolskapps.libri.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Schema history. Every statement below is copied verbatim from the `createSql` that
 * Room exported for the target version under `app/schemas/` — Room compares an identity
 * hash of the resulting schema at open time, so hand-written SQL that merely looks
 * equivalent will throw.
 */

/**
 * v1 -> v2: reading sessions, yearly goals, and the finish timestamp the goal counts
 * against.
 *
 * Existing books keep their rows. Books already on the Finished shelf get a null
 * `finishedAt` because the date genuinely isn't known — backfilling it with "now" would
 * dump a whole back catalogue into this year's goal.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // No DEFAULT clause: the exported schema records no default for this column, and
        // SQLite would report `DEFAULT NULL` back as the string "NULL" — which Room's
        // schema validation compares and rejects at open time.
        db.execSQL("ALTER TABLE `books` ADD COLUMN `finishedAt` INTEGER")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `reading_sessions` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`bookId` INTEGER NOT NULL, " +
                "`recordedAt` INTEGER NOT NULL, " +
                "`pagesRead` INTEGER NOT NULL, " +
                "`endPage` INTEGER NOT NULL, " +
                "FOREIGN KEY(`bookId`) REFERENCES `books`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_reading_sessions_bookId` " +
                "ON `reading_sessions` (`bookId`)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_reading_sessions_recordedAt` " +
                "ON `reading_sessions` (`recordedAt`)"
        )

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `reading_goals` (" +
                "`year` INTEGER NOT NULL, " +
                "`targetBooks` INTEGER NOT NULL, " +
                "PRIMARY KEY(`year`))"
        )
    }
}

/**
 * Declared last: top-level properties initialise in file order, so referencing the
 * migrations above this point would hand Room an array of nulls.
 */
val ALL_MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2)
