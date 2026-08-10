package com.bolskapps.libri.data

import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for the local library. ViewModels never touch the DAOs
 * directly, which keeps a future sync backend a one-file change.
 */
class BookRepository(
    private val bookDao: BookDao,
    private val sessionDao: ReadingSessionDao,
    private val goalDao: ReadingGoalDao
) {

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

    // ---- Reading sessions -------------------------------------------------

    fun sessionsSince(since: Long): Flow<List<ReadingSession>> = sessionDao.getSessionsSince(since)

    fun sessionsForBook(bookId: Int): Flow<List<ReadingSession>> =
        sessionDao.getSessionsForBook(bookId)

    // ---- Goals ------------------------------------------------------------

    fun goalFor(year: Int): Flow<ReadingGoal?> = goalDao.getGoal(year)

    fun booksFinishedIn(year: Int): Flow<List<Book>> = bookDao.getBooksFinishedBetween(
        from = startOfYear(year),
        until = startOfYear(year + 1)
    )

    suspend fun setGoal(year: Int, targetBooks: Int) {
        goalDao.upsert(ReadingGoal(year, targetBooks.coerceIn(1, MAX_GOAL_BOOKS)))
    }

    suspend fun clearGoal(year: Int) = goalDao.clear(year)

    // ---- Writes -----------------------------------------------------------

    /**
     * Inserts when the book is new, updates otherwise, and records a [ReadingSession]
     * for any forward movement so pace and streaks have something to read from.
     */
    suspend fun save(book: Book, now: Long = System.currentTimeMillis()) {
        val previous = if (book.id == NEW_BOOK_ID) null else bookDao.getBookById(book.id)
        val normalized = book.normalized(now, previousFinishedAt = previous?.finishedAt)

        if (normalized.id == NEW_BOOK_ID) {
            val newId = bookDao.insert(normalized.copy(addedAt = now)).toInt()
            // A book added straight to Reading or Finished already carries progress.
            logProgress(bookId = newId, from = 0, to = normalized.currentPage, now = now)
        } else {
            bookDao.update(normalized)
            logProgress(
                bookId = normalized.id,
                from = previous?.currentPage ?: 0,
                to = normalized.currentPage,
                now = now
            )
        }
    }

    /** Progress-only update, leaving metadata untouched. */
    suspend fun updateProgress(book: Book, currentPage: Int, now: Long = System.currentTimeMillis()) {
        save(book.copy(currentPage = currentPage), now)
    }

    suspend fun delete(book: Book) = bookDao.delete(book)

    /** Only forward movement is reading; a correction backwards is not a session. */
    private suspend fun logProgress(bookId: Int, from: Int, to: Int, now: Long) {
        val delta = to - from
        if (delta <= 0) return
        sessionDao.insert(
            ReadingSession(
                bookId = bookId,
                recordedAt = now,
                pagesRead = delta,
                endPage = to
            )
        )
    }
}

/**
 * Applies the library's invariants before a book reaches the database:
 *  1. `currentPage` never escapes `0..totalPages`.
 *  2. Reaching the last page promotes the book to FINISHED; a finished book that drops
 *     below the last page falls back to READING.
 *  3. `finishedAt` is stamped on arrival at the Finished shelf and cleared on leaving
 *     it, but never re-stamped — otherwise every later edit would move the book into
 *     the current year's goal.
 *
 * Ratings and notes are deliberately preserved on every shelf: they are the user's own
 * opinion, so a wishlist entry can carry a star rating and a review.
 *
 * @param previousFinishedAt the stored timestamp, so a re-save keeps the original date.
 */
fun Book.normalized(
    now: Long = System.currentTimeMillis(),
    previousFinishedAt: Long? = finishedAt
): Book {
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

    val resolvedFinishedAt = when {
        resolvedStatus != ReadingStatus.FINISHED -> null
        previousFinishedAt != null -> previousFinishedAt
        else -> now
    }

    return copy(
        title = title.trim(),
        author = author.trim(),
        totalPages = safeTotal,
        currentPage = safeCurrent,
        status = resolvedStatus,
        rating = rating.coerceIn(0, MAX_RATING),
        notes = notes?.trim()?.takeIf { it.isNotEmpty() },
        genre = genre?.trim()?.takeIf { it.isNotEmpty() },
        finishedAt = resolvedFinishedAt
    )
}
