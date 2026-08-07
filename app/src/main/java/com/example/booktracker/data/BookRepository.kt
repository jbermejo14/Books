package com.example.booktracker.data

import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for the local library. ViewModels never touch [BookDao]
 * directly, which keeps a future sync backend a one-file change.
 */
class BookRepository(private val bookDao: BookDao) {

    val allBooks: Flow<List<Book>> = bookDao.getAllBooks()
    val readingBooks: Flow<List<Book>> = bookDao.getReadingBooks()
    val toReadBooks: Flow<List<Book>> = bookDao.getToReadBooks()
    val finishedBooks: Flow<List<Book>> = bookDao.getFinishedBooks()
    val wishlistBooks: Flow<List<Book>> = bookDao.getBooksByStatus(ReadingStatus.WISHLIST)

    fun booksByStatus(status: ReadingStatus): Flow<List<Book>> = bookDao.getBooksByStatus(status)

    fun getBookStream(id: Int): Flow<Book?> = bookDao.getBookStream(id)

    suspend fun getBookById(id: Int): Book? = bookDao.getBookById(id)

    suspend fun isAlreadyInLibrary(openLibraryKey: String): Boolean =
        bookDao.getBookByOpenLibraryKey(openLibraryKey) != null

    /** Inserts when the book is new, updates otherwise. */
    suspend fun save(book: Book, now: Long = System.currentTimeMillis()) {
        val normalized = book.normalized()
        if (normalized.id == NEW_BOOK_ID) {
            bookDao.insert(normalized.copy(addedAt = now))
        } else {
            bookDao.update(normalized)
        }
    }

    /** Progress-only update from the dashboard / pending list, leaving metadata untouched. */
    suspend fun updateProgress(book: Book, currentPage: Int) {
        bookDao.update(book.copy(currentPage = currentPage).normalized())
    }

    suspend fun delete(book: Book) = bookDao.delete(book)
}

/**
 * Applies the library's invariants before a book reaches the database:
 *  1. `currentPage` never escapes `0..totalPages`.
 *  2. Reaching the last page promotes the book to FINISHED; a finished book that
 *     drops below the last page falls back to READING.
 *
 * Ratings and notes are deliberately preserved on every shelf: they are the user's
 * own opinion, so a wishlist entry can carry a star rating and a review.
 */
fun Book.normalized(): Book {
    val safeTotal = totalPages.coerceAtLeast(0)
    val safeCurrent = if (safeTotal == 0) 0 else currentPage.coerceIn(0, safeTotal)

    val resolvedStatus = when {
        // Wishlist entries are aspirational — page counts must not promote them.
        status == ReadingStatus.WISHLIST -> ReadingStatus.WISHLIST
        safeTotal > 0 && safeCurrent == safeTotal -> ReadingStatus.FINISHED
        status == ReadingStatus.FINISHED -> ReadingStatus.READING
        safeCurrent > 0 -> ReadingStatus.READING
        else -> status
    }

    return copy(
        title = title.trim(),
        author = author.trim(),
        totalPages = safeTotal,
        currentPage = safeCurrent,
        status = resolvedStatus,
        rating = rating.coerceIn(0, MAX_RATING),
        notes = notes?.trim()?.takeIf { it.isNotEmpty() },
        genre = genre?.trim()?.takeIf { it.isNotEmpty() }
    )
}
