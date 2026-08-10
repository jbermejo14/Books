package com.bolskapps.libri.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Sentinel id used for a book that has not been persisted yet. */
const val NEW_BOOK_ID: Int = 0

/** Highest rating a finished book can receive. */
const val MAX_RATING: Int = 5

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Int = NEW_BOOK_ID,
    val title: String,
    val author: String,
    val currentPage: Int = 0,
    val totalPages: Int,
    val status: ReadingStatus = ReadingStatus.TO_READ,
    val rating: Int = 0,
    /** Free-form review / reading notes written by the user. */
    val notes: String? = null,
    val genre: String? = null,
    /** Open Library `cover_i`; resolved to a URL by [coverUrl]. */
    val coverId: Long? = null,
    /** Open Library work key, e.g. `/works/OL45883W`. Lets us re-fetch details later. */
    val openLibraryKey: String? = null,
    /** Drives the dashboard's "Recently Added" ordering. */
    val addedAt: Long = 0L
) {
    val isFinished: Boolean get() = status == ReadingStatus.FINISHED

    /**
     * Reading progress in the 0f..1f range. Guards against a zero page count so a
     * wishlist entry with no page data can never produce NaN.
     */
    val progress: Float
        get() = if (totalPages <= 0) 0f
        else (currentPage.toFloat() / totalPages.toFloat()).coerceIn(0f, 1f)

    val progressPercent: Int
        get() = (progress * 100).toInt()

    fun coverUrl(size: CoverSize = CoverSize.MEDIUM): String? =
        coverId?.let { "https://covers.openlibrary.org/b/id/$it-${size.suffix}.jpg" }
}

enum class CoverSize(val suffix: String) {
    SMALL("S"), MEDIUM("M"), LARGE("L")
}
