package com.bolskapps.libri.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Book::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class BookDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao

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
                    // No destructive fallback: this is the shipping schema, and a user's
                    // library is the whole value of the app. Every future version bump
                    // must add a Migration here — the exported schemas under app/schemas
                    // are what those migrations are written and tested against.
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
