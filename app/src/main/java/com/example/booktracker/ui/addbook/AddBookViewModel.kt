package com.example.booktracker.ui.addbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booktracker.data.Book
import com.example.booktracker.data.BookRepository
import com.example.booktracker.data.ReadingStatus
import com.example.booktracker.data.remote.BookSearchResult
import com.example.booktracker.data.remote.OpenLibraryRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The dropdown options from the design's "Select Genre" control. */
val GENRES = listOf(
    "Fiction",
    "Non-Fiction",
    "Sci-Fi & Fantasy",
    "Mystery & Thriller",
    "Biography"
)

data class AddBookUiState(
    // Open Library lookup — driven by the title field.
    val results: List<BookSearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val searchFailed: Boolean = false,
    val showResults: Boolean = false,
    val alreadyInLibrary: Boolean = false,

    // Form
    val title: String = "",
    val author: String = "",
    val genre: String? = null,
    val totalPages: String = "",
    val status: ReadingStatus = ReadingStatus.TO_READ,
    val coverId: Long? = null,
    val openLibraryKey: String? = null,

    val showErrors: Boolean = false,
    val isSaved: Boolean = false
) {
    val totalPagesValue: Int get() = totalPages.toIntOrNull() ?: 0

    /** Page count only matters once you are actually tracking progress. */
    val pagesRequired: Boolean
        get() = status == ReadingStatus.READING || status == ReadingStatus.FINISHED

    val titleError: Boolean get() = showErrors && title.isBlank()
    val authorError: Boolean get() = showErrors && author.isBlank()
    val totalPagesError: Boolean get() = showErrors && pagesRequired && totalPagesValue <= 0

    val isValid: Boolean
        get() = title.isNotBlank() &&
            author.isNotBlank() &&
            (!pagesRequired || totalPagesValue > 0)

    val coverUrl: String?
        get() = coverId?.let { "https://covers.openlibrary.org/b/id/$it-L.jpg" }
}

class AddBookViewModel(
    private val bookRepository: BookRepository,
    private val openLibraryRepository: OpenLibraryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddBookUiState())
    val uiState: StateFlow<AddBookUiState> = _uiState.asStateFlow()

    /** Separate from the form so typing does not fire a request per keystroke. */
    private val queryFlow = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    private fun observeQuery() {
        viewModelScope.launch {
            queryFlow
                .debounce(SEARCH_DEBOUNCE_MILLIS)
                .distinctUntilChanged()
                // collectLatest cancels an in-flight search when the query moves on.
                .collectLatest { query -> runSearch(query) }
        }
    }

    init {
        observeQuery()
    }

    private suspend fun runSearch(query: String) {
        if (query.trim().length < MIN_QUERY_LENGTH) {
            _uiState.update {
                it.copy(results = emptyList(), isSearching = false, searchFailed = false, showResults = false)
            }
            return
        }

        _uiState.update { it.copy(isSearching = true, searchFailed = false) }

        openLibraryRepository.search(query).fold(
            onSuccess = { results ->
                _uiState.update {
                    it.copy(
                        results = results,
                        isSearching = false,
                        searchFailed = false,
                        showResults = results.isNotEmpty()
                    )
                }
            },
            onFailure = {
                _uiState.update {
                    it.copy(
                        results = emptyList(),
                        isSearching = false,
                        searchFailed = true,
                        showResults = false
                    )
                }
            }
        )
    }

    fun updateTitle(value: String) {
        _uiState.update {
            // Typing over a picked result detaches it from its Open Library record.
            it.copy(title = value, openLibraryKey = null, alreadyInLibrary = false)
        }
        queryFlow.value = value
    }

    fun updateAuthor(value: String) = _uiState.update { it.copy(author = value) }

    fun updateGenre(value: String?) = _uiState.update { it.copy(genre = value) }

    fun updateTotalPages(value: String) = _uiState.update {
        it.copy(totalPages = value.filter(Char::isDigit).take(MAX_PAGE_DIGITS))
    }

    fun updateStatus(value: ReadingStatus) = _uiState.update { it.copy(status = value) }

    fun dismissResults() = _uiState.update { it.copy(showResults = false) }

    /** Fills the whole form from a search hit, cover included. */
    fun selectResult(result: BookSearchResult) {
        // Stop the debounced search from re-opening the dropdown over the selection.
        queryFlow.value = result.title

        _uiState.update {
            it.copy(
                title = result.title,
                author = result.author,
                totalPages = result.totalPages?.toString() ?: it.totalPages,
                coverId = result.coverId,
                openLibraryKey = result.openLibraryKey,
                showResults = false,
                searchFailed = false
            )
        }

        viewModelScope.launch {
            val duplicate = bookRepository.isAlreadyInLibrary(result.openLibraryKey)
            _uiState.update { it.copy(alreadyInLibrary = duplicate) }
        }
    }

    fun clearCover() = _uiState.update { it.copy(coverId = null, openLibraryKey = null) }

    fun save() {
        val state = _uiState.value
        if (!state.isValid) {
            _uiState.update { it.copy(showErrors = true) }
            return
        }

        viewModelScope.launch {
            bookRepository.save(
                Book(
                    title = state.title,
                    author = state.author,
                    // Adding something straight to the Finished shelf means it was read
                    // cover to cover; anything else starts at page zero.
                    currentPage = if (state.status == ReadingStatus.FINISHED) {
                        state.totalPagesValue
                    } else {
                        0
                    },
                    totalPages = state.totalPagesValue,
                    status = state.status,
                    genre = state.genre,
                    coverId = state.coverId,
                    openLibraryKey = state.openLibraryKey
                )
            )
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 350L
        const val MIN_QUERY_LENGTH = 2
        const val MAX_PAGE_DIGITS = 6
    }
}
