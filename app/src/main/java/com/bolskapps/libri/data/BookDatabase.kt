package com.bolskapps.libri.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Book::class, ReadingSession::class, ReadingGoal::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class BookDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun readingSessionDao(): ReadingSessionDao
    abstract fun readingGoalDao(): ReadingGoalDao

    companion object {
        private const val DATABASE_NAME = "book_database"

        @Volatile
        private var INSTANCE: BookDatabase? = null

        fun getDatabase(context: Context): BookDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BookDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(*ALL_MIGRATIONS)
                    // No destructive fallback: a user's library is the whole value of
                    // the app. A version bump without a migration must fail loudly here
                    // rather than silently delete it.
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
