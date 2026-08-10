package com.bolskapps.libri.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bolskapps.libri.data.Book
import com.bolskapps.libri.data.BookRepository
import com.bolskapps.libri.data.PACE_WINDOW_DAYS
import com.bolskapps.libri.data.ReadingStats
import com.bolskapps.libri.data.computeReadingStats
import com.bolskapps.libri.data.currentYear
import com.bolskapps.libri.data.daysToFinish
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class DashboardUiState(
    val readingBooks: List<Book> = emptyList(),
    val recentlyAdded: List<Book> = emptyList(),
    val year: Int = 0,
    /** Null when no goal has been set for [year] yet. */
    val goalTarget: Int? = null,
    val booksFinishedThisYear: Int = 0,
    val stats: ReadingStats = ReadingStats(),
    val isLoading: Boolean = true
) {
    val activeCount: Int get() = readingBooks.size

    /** The book the "Currently Reading" and "Update Progress" cards operate on. */
    val heroBook: Book? get() = readingBooks.firstOrNull()

    val hasGoal: Boolean get() = goalTarget != null

    val goalProgress: Float
        get() = goalTarget?.takeIf { it > 0 }
            ?.let { (booksFinishedThisYear.toFloat() / it).coerceIn(0f, 1f) }
            ?: 0f

    val goalRemaining: Int
        get() = ((goalTarget ?: 0) - booksFinishedThisYear).coerceAtLeast(0)

    val goalReached: Boolean
        get() = goalTarget != null && booksFinishedThisYear >= goalTarget

    /** Days left on the hero book at the reader's recent pace, if projectable. */
    val heroDaysLeft: Int? get() = heroBook?.daysToFinish(stats)
}

/** Inline editor state for the "Update Progress" card. */
data class ProgressForm(
    val bookId: Int? = null,
    val currentPage: String = "",
    val totalPages: String = ""
) {
    val currentPageValue: Int get() = currentPage.toIntOrNull() ?: 0
    val totalPagesValue: Int get() = totalPages.toIntOrNull() ?: 0

    /** Drives the "Live Preview" bar — recomputed on every keystroke. */
    val previewProgress: Float
        get() = if (totalPagesValue <= 0) 0f
        else (currentPageValue.toFloat() / totalPagesValue.toFloat()).coerceIn(0f, 1f)

    val previewPercent: Int get() = (previewProgress * 100).toInt()

    val isValid: Boolean
        get() = bookId != null &&
            totalPagesValue > 0 &&
            currentPage.isNotBlank() &&
            currentPageValue in 0..totalPagesValue
}

class DashboardViewModel(private val repository: BookRepository) : ViewModel() {

    /**
     * Resolved once per ViewModel rather than per emission: the goal and finished-book
     * queries are keyed on it, and re-keying them mid-session would restart the flows.
     */
    private val year = currentYear(System.currentTimeMillis())

    private val _progressForm = MutableStateFlow(ProgressForm())
    val progressForm: StateFlow<ProgressForm> = _progressForm.asStateFlow()

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.readingBooks,
        repository.allBooks,
        repository.goalFor(year),
        repository.booksFinishedIn(year),
        repository.sessionsSince(System.currentTimeMillis() - PACE_WINDOW_MILLIS)
    ) { reading, all, goal, finishedThisYear, sessions ->
        DashboardUiState(
            readingBooks = reading,
            recentlyAdded = all.take(RECENTLY_ADDED_LIMIT),
            year = year,
            goalTarget = goal?.targetBooks,
            booksFinishedThisYear = finishedThisYear.size,
            stats = computeReadingStats(sessions, System.currentTimeMillis()),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = DashboardUiState(year = year)
    )

    init {
        // Reseed the editor whenever the hero book changes identity, but not on every
        // progress write — otherwise saving would stomp on what the user is typing.
        viewModelScope.launch {
            repository.readingBooks
                .map { it.firstOrNull() }
                .distinctUntilChangedBy { it?.id }
                .collect { hero -> _progressForm.value = hero.toForm() }
        }
    }

    fun updateCurrentPage(value: String) = _progressForm.update {
        it.copy(currentPage = value.digitsOnly())
    }

    fun updateTotalPages(value: String) = _progressForm.update {
        it.copy(totalPages = value.digitsOnly())
    }

    fun saveProgress() {
        val form = _progressForm.value
        val book = uiState.value.heroBook ?: return
        if (!form.isValid || form.bookId != book.id) return

        viewModelScope.launch {
            repository.save(
                book.copy(
                    currentPage = form.currentPageValue,
                    totalPages = form.totalPagesValue
                )
            )
        }
    }

    fun setGoal(target: Int) {
        viewModelScope.launch { repository.setGoal(year, target) }
    }

    fun clearGoal() {
        viewModelScope.launch { repository.clearGoal(year) }
    }

    fun saveBook(book: Book) {
        viewModelScope.launch { repository.save(book) }
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch { repository.delete(book) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val RECENTLY_ADDED_LIMIT = 6
        val PACE_WINDOW_MILLIS: Long = TimeUnit.DAYS.toMillis(PACE_WINDOW_DAYS)
    }
}

private fun Book?.toForm(): ProgressForm =
    if (this == null) ProgressForm()
    else ProgressForm(
        bookId = id,
        currentPage = currentPage.toString(),
        totalPages = totalPages.toString()
    )

/** Keeps numeric fields numeric without relying on the soft keyboard type alone. */
internal fun String.digitsOnly(maxLength: Int = 6): String =
    filter(Char::isDigit).take(maxLength)
