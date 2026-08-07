package com.example.booktracker.ui.pending

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booktracker.R
import com.example.booktracker.data.Book
import com.example.booktracker.data.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The three chips in the utility bar of the `pendientes` design. */
enum class PendingFilter(@StringRes val labelRes: Int) {
    ALL_UNREAD(R.string.filter_all_unread),
    CURRENTLY_READING(R.string.filter_currently_reading),
    NEXT_UP(R.string.filter_next_up)
}

/** Backs the "Sort" affordance next to the chips. */
enum class BookSort(@StringRes val labelRes: Int) {
    RECENT(R.string.sort_recent),
    TITLE(R.string.sort_title),
    PROGRESS(R.string.sort_progress)
}

data class PendingUiState(
    val inProgress: List<Book> = emptyList(),
    val toRead: List<Book> = emptyList(),
    val filter: PendingFilter = PendingFilter.ALL_UNREAD,
    val sort: BookSort = BookSort.RECENT,
    val isLoading: Boolean = true
) {
    val showInProgress: Boolean get() = filter != PendingFilter.NEXT_UP
    val showToRead: Boolean get() = filter != PendingFilter.CURRENTLY_READING

    val isEmpty: Boolean
        get() = !isLoading &&
            (!showInProgress || inProgress.isEmpty()) &&
            (!showToRead || toRead.isEmpty())
}

class PendingViewModel(private val repository: BookRepository) : ViewModel() {

    private val filter = MutableStateFlow(PendingFilter.ALL_UNREAD)
    private val sort = MutableStateFlow(BookSort.RECENT)

    val uiState: StateFlow<PendingUiState> = combine(
        repository.readingBooks,
        repository.toReadBooks,
        filter,
        sort
    ) { reading, toRead, activeFilter, activeSort ->
        PendingUiState(
            inProgress = reading.sortedWith(activeSort.comparator),
            toRead = toRead.sortedWith(activeSort.comparator),
            filter = activeFilter,
            sort = activeSort,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = PendingUiState()
    )

    fun selectFilter(value: PendingFilter) {
        filter.value = value
    }

    fun selectSort(value: BookSort) {
        sort.value = value
    }

    fun saveBook(book: Book) {
        viewModelScope.launch { repository.save(book) }
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch { repository.delete(book) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

private val BookSort.comparator: Comparator<Book>
    get() = when (this) {
        BookSort.RECENT -> compareByDescending { it.addedAt }
        BookSort.TITLE -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.title }
        BookSort.PROGRESS -> compareByDescending { it.progress }
    }
