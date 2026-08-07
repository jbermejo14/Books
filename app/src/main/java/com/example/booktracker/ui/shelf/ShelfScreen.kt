package com.example.booktracker.ui.shelf

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.booktracker.R
import com.example.booktracker.data.Book
import com.example.booktracker.data.ReadingStatus
import com.example.booktracker.ui.AppViewModelProvider
import com.example.booktracker.ui.components.EmptyState
import com.example.booktracker.ui.components.GridBookCard
import com.example.booktracker.ui.components.LibriTopBar
import com.example.booktracker.ui.components.BookSheet
import com.example.booktracker.ui.components.bookGridRows

/** Wishlist and History: the same grid, a different shelf. */
@Composable
fun ShelfScreen(
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ShelfViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var sheetBook by remember { mutableStateOf<Book?>(null) }

    val isHistory = uiState.status == ReadingStatus.FINISHED
    val titleRes = if (isHistory) R.string.history_title else R.string.wishlist_title
    val emptyRes = if (isHistory) R.string.empty_history else R.string.empty_wishlist

    Column(modifier = modifier.fillMaxWidth()) {
        LibriTopBar(
            title = stringResource(titleRes),
            actionIcon = Icons.Outlined.Search,
            onActionClick = onSearch,
            actionContentDescription = stringResource(R.string.search)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (uiState.isEmpty) {
                item { EmptyState(message = stringResource(emptyRes)) }
            }

            bookGridRows(uiState.books) { book ->
                GridBookCard(
                    book = book,
                    onClick = { sheetBook = book },
                    // Ratings only mean something once a book is finished.
                    showRating = isHistory
                )
            }
        }
    }

    sheetBook?.let { book ->
        BookSheet(
            book = book,
            onDismiss = { sheetBook = null },
            onSave = viewModel::saveBook,
            onDelete = viewModel::deleteBook
        )
    }
}
