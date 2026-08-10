package com.bolskapps.libri.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One forward jump in a book's progress.
 *
 * `Book.currentPage` is a single number that gets overwritten, so on its own it has no
 * memory of how the reader got there. Appending a row per advance is what makes pace,
 * streaks and finish estimates answerable.
 *
 * Sessions are only recorded for positive movement: correcting a typo backwards is not
 * reading.
 */
@Entity(
    tableName = "reading_sessions",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            // Deleting a book must not leave its sessions behind, inflating the stats.
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bookId"), Index("recordedAt")]
)
data class ReadingSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val bookId: Int,
    val recordedAt: Long,
    /** Pages advanced in this session. Always greater than zero. */
    val pagesRead: Int,
    /** Page landed on, kept so a session reads sensibly on its own. */
    val endPage: Int
)
