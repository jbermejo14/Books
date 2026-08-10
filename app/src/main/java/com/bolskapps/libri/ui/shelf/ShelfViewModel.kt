package com.bolskapps.libri.ui.shelf

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bolskapps.libri.data.Book
import com.bolskapps.libri.data.BookRepository
import com.bolskapps.libri.data.ReadingStatus
import com.bolskapps.libri.ui.navigation.BookDestinations
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ShelfUiState(
    val status: ReadingStatus = ReadingStatus.WISHLIST,
    val books: List<Book> = emptyList(),
    val isLoading: Boolean = true
) {
    val isEmpty: Boolean get() = !isLoading && books.isEmpty()
}

/**
 * Backs both the Wishlist and History tabs — they are the same grid over a
 * different shelf, so the status arrives as a navigation argument.
 */
class ShelfViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: BookRepository
) : ViewModel() {

    private val status: ReadingStatus =
        ReadingStatus.fromName(savedStateHandle[BookDestinations.STATUS_ARG])

    val uiState: StateFlow<ShelfUiState> = repository.booksByStatus(status)
        .map { books -> ShelfUiState(status = status, books = books, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = ShelfUiState(status = status)
        )

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
