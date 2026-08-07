package com.example.booktracker.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booktracker.data.Book
import com.example.booktracker.data.BookRepository
import com.example.booktracker.data.remote.BookSearchResult
import com.example.booktracker.data.remote.OpenLibraryRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Seed searches offered before the user types anything. */
val SEARCH_SUGGESTIONS = listOf(
    "Fiction",
    "Philosophy",
    "Science",
    "History",
    "Poetry",
    "Art"
)

data class SearchUiState(
    val query: String = "",
    val results: List<BookSearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val searchFailed: Boolean = false,
    /** False until the first request completes, so the empty view isn't shown too early. */
    val hasSearched: Boolean = false,
    /** Open Library key -> the saved row, used to mark results already in the library. */
    val libraryByKey: Map<String, Book> = emptyMap()
) {
    val showNoResults: Boolean
        get() = hasSearched && !isSearching && !searchFailed && results.isEmpty()
}

class SearchViewModel(
    private val bookRepository: BookRepository,
    private val openLibraryRepository: OpenLibraryRepository
) : ViewModel() {

    private val searchState = MutableStateFlow(SearchUiState())
    private val queryFlow = MutableStateFlow(DEFAULT_QUERY)

    private val _selected = MutableStateFlow<BookSearchResult?>(null)
    val selected: StateFlow<BookSearchResult?> = _selected.asStateFlow()

    val uiState: StateFlow<SearchUiState> = combine(
        searchState,
        bookRepository.allBooks
    ) { state, books ->
        state.copy(
            libraryByKey = books
                .mapNotNull { book -> book.openLibraryKey?.let { key -> key to book } }
                .toMap()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = SearchUiState()
    )

    @OptIn(FlowPreview::class)
    private fun observeQuery() {
        viewModelScope.launch {
            queryFlow
                .debounce(SEARCH_DEBOUNCE_MILLIS)
                .distinctUntilChanged()
                // collectLatest cancels an in-flight request when the query moves on.
                .collectLatest { query -> runSearch(query) }
        }
    }

    init {
        // Land on a populated page rather than a blank one.
        observeQuery()
    }

    private suspend fun runSearch(query: String) {
        if (query.isBlank()) {
            searchState.update {
                it.copy(results = emptyList(), isSearching = false, searchFailed = false, hasSearched = false)
            }
            return
        }

        searchState.update { it.copy(isSearching = true, searchFailed = false) }

        openLibraryRepository.search(query).fold(
            onSuccess = { results ->
                searchState.update {
                    it.copy(
                        results = results,
                        isSearching = false,
                        searchFailed = false,
                        hasSearched = true
                    )
                }
            },
            onFailure = {
                searchState.update {
                    it.copy(
                        results = emptyList(),
                        isSearching = false,
                        searchFailed = true,
                        hasSearched = true
                    )
                }
            }
        )
    }

    fun updateQuery(value: String) {
        searchState.update { it.copy(query = value) }
        queryFlow.value = value
    }

    /** Suggestion chips search immediately rather than waiting out the debounce. */
    fun applySuggestion(suggestion: String) = updateQuery(suggestion)

    fun clearQuery() = updateQuery("")

    fun select(result: BookSearchResult?) {
        _selected.value = result
    }

    fun saveBook(book: Book) {
        viewModelScope.launch { bookRepository.save(book) }
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch { bookRepository.delete(book) }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 350L
        const val STOP_TIMEOUT_MILLIS = 5_000L

        /** Opening query, so the page is never empty on arrival. */
        const val DEFAULT_QUERY = "Fiction"
    }
}
