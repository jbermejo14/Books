package com.example.booktracker.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.booktracker.R
import com.example.booktracker.data.Book
import com.example.booktracker.data.CoverSize
import com.example.booktracker.data.ReadingStatus
import com.example.booktracker.data.remote.BookSearchResult
import com.example.booktracker.ui.AppViewModelProvider
import com.example.booktracker.ui.components.BookCover
import com.example.booktracker.ui.components.BookSheet
import com.example.booktracker.ui.components.LibriTopBar
import com.example.booktracker.ui.components.LibriUnderlineField
import com.example.booktracker.ui.components.SectionHeader
import com.example.booktracker.ui.components.statusLabel
import com.example.booktracker.ui.theme.Libri
import com.example.booktracker.ui.theme.LibriType

/**
 * Open Library discovery. Every hit can be added straight to a shelf, rated and
 * reviewed through the shared [BookSheet], so nothing has to be typed by hand.
 */
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onAddManually: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxWidth()) {
        LibriTopBar(
            title = stringResource(R.string.discover_title),
            navigationIcon = Icons.AutoMirrored.Outlined.ArrowBack,
            onNavigationClick = onNavigateBack,
            navigationContentDescription = stringResource(R.string.back),
            actionIcon = Icons.Outlined.EditNote,
            onActionClick = onAddManually,
            actionContentDescription = stringResource(R.string.add_manually)
        )

        LibriUnderlineField(
            value = uiState.query,
            onValueChange = viewModel::updateQuery,
            placeholder = stringResource(R.string.search_placeholder),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            trailingIcon = {
                when {
                    uiState.isSearching -> CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = Libri.Primary,
                        modifier = Modifier.size(18.dp)
                    )

                    uiState.query.isNotEmpty() -> Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.clear),
                        tint = Libri.OnSurfaceVariant,
                        modifier = Modifier.clickable(onClick = viewModel::clearQuery)
                    )

                    else -> Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = Libri.OutlineVariant
                    )
                }
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (uiState.query.isBlank()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SEARCH_SUGGESTIONS.forEach { suggestion ->
                            SuggestionChip(
                                label = suggestion,
                                onClick = { viewModel.applySuggestion(suggestion) }
                            )
                        }
                    }
                }
            }

            item {
                SectionHeader(
                    text = stringResource(
                        if (uiState.query.isBlank()) R.string.suggested else R.string.results
                    ),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            if (uiState.searchFailed) {
                item {
                    Text(
                        text = stringResource(R.string.search_failed),
                        style = LibriType.bodyMd,
                        color = Libri.Error
                    )
                }
            }

            if (uiState.showNoResults) {
                item {
                    Text(
                        text = stringResource(R.string.no_results),
                        style = LibriType.bodyMd,
                        color = Libri.OnSurfaceVariant
                    )
                }
            }

            items(
                count = uiState.results.size,
                key = { index -> uiState.results[index].openLibraryKey }
            ) { index ->
                val result = uiState.results[index]
                SearchResultRow(
                    result = result,
                    saved = uiState.libraryByKey[result.openLibraryKey],
                    onClick = { viewModel.select(result) }
                )
            }
        }
    }

    selected?.let { result ->
        val existing = uiState.libraryByKey[result.openLibraryKey]
        // Either edit the saved row, or open a draft built from the Open Library record.
        val draft = existing ?: Book(
            title = result.title,
            author = result.author,
            totalPages = result.totalPages ?: 0,
            status = ReadingStatus.TO_READ,
            coverId = result.coverId,
            openLibraryKey = result.openLibraryKey
        )

        BookSheet(
            book = draft,
            isNew = existing == null,
            meta = result.metaLine(),
            onDismiss = { viewModel.select(null) },
            onSave = viewModel::saveBook,
            onDelete = if (existing != null) viewModel::deleteBook else null
        )
    }
}

@Composable
private fun SearchResultRow(
    result: BookSearchResult,
    saved: Book?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BookCover(
            coverUrl = result.coverUrl(CoverSize.MEDIUM),
            title = result.title,
            modifier = Modifier
                .width(64.dp)
                .height(96.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = result.title,
                style = LibriType.labelMd,
                color = Libri.OnSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = result.author,
                style = LibriType.labelSm,
                color = Libri.OnSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
            val meta = result.metaLine()
            if (meta != null) {
                Text(
                    text = meta,
                    style = LibriType.labelSm,
                    color = Libri.OutlineVariant,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (saved != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(Libri.SecondaryContainer)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = Libri.OnSecondaryContainer,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = statusLabel(saved.status),
                        style = LibriType.labelSm,
                        color = Libri.OnSecondaryContainer,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(if (saved == null) Libri.Primary else Libri.SurfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (saved == null) Icons.Outlined.Add else Icons.Outlined.EditNote,
                contentDescription = stringResource(
                    if (saved == null) R.string.add_this_book else R.string.update
                ),
                tint = if (saved == null) Libri.OnPrimary else Libri.OnSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SuggestionChip(
    label: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(percent = 50)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(Libri.Surface)
            .border(1.dp, Libri.OutlineVariant, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = label, style = LibriType.labelSm, color = Libri.OnSurface)
    }
}

/** "1976 · 504 pages", omitting whichever half Open Library doesn't have. */
@Composable
private fun BookSearchResult.metaLine(): String? {
    val parts = listOfNotNull(
        firstPublishYear?.let { stringResource(R.string.first_published, it) },
        totalPages?.let { stringResource(R.string.pages_count, it) }
    )
    return parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
}
