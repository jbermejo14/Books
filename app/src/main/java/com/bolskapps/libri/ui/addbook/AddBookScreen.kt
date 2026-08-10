package com.bolskapps.libri.ui.addbook

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bolskapps.libri.R
import com.bolskapps.libri.data.CoverSize
import com.bolskapps.libri.data.remote.BookSearchResult
import com.bolskapps.libri.ui.AppViewModelProvider
import com.bolskapps.libri.ui.components.BookCover
import com.bolskapps.libri.ui.components.LibriGenreDropdown
import com.bolskapps.libri.ui.components.LibriPrimaryButton
import com.bolskapps.libri.ui.components.LibriSegmentedStatus
import com.bolskapps.libri.ui.components.LibriTopBar
import com.bolskapps.libri.ui.components.LibriUnderlineField
import com.bolskapps.libri.ui.theme.Libri
import com.bolskapps.libri.ui.theme.LibriType

@Composable
fun AddBookScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddBookViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        LibriTopBar(
            title = stringResource(R.string.add_new_book),
            navigationIcon = Icons.AutoMirrored.Outlined.ArrowBack,
            onNavigationClick = onNavigateBack,
            navigationContentDescription = stringResource(R.string.back)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            CoverSlot(
                coverUrl = uiState.coverUrl,
                title = uiState.title,
                onClear = viewModel::clearCover
            )

            // The title field doubles as the Open Library search box — the design's
            // barcode affordance becomes a lookup, which is what it is here.
            Column {
                LibriUnderlineField(
                    value = uiState.title,
                    onValueChange = viewModel::updateTitle,
                    placeholder = stringResource(R.string.book_title),
                    isError = uiState.titleError,
                    supportingText = when {
                        uiState.titleError -> stringResource(R.string.error_title_required)
                        uiState.alreadyInLibrary -> stringResource(R.string.already_in_library)
                        uiState.searchFailed -> stringResource(R.string.search_failed)
                        uiState.isSearching -> stringResource(R.string.searching)
                        uiState.title.isBlank() -> stringResource(R.string.search_hint)
                        else -> null
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    trailingIcon = {
                        if (uiState.isSearching) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = Libri.Primary,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = stringResource(R.string.search_open_library),
                                tint = Libri.OutlineVariant
                            )
                        }
                    }
                )

                if (uiState.showResults) {
                    SearchResults(
                        results = uiState.results,
                        onSelect = viewModel::selectResult,
                        onDismiss = viewModel::dismissResults
                    )
                }
            }

            LibriUnderlineField(
                value = uiState.author,
                onValueChange = viewModel::updateAuthor,
                placeholder = stringResource(R.string.author),
                isError = uiState.authorError,
                supportingText = if (uiState.authorError) {
                    stringResource(R.string.error_author_required)
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                LibriGenreDropdown(
                    selected = uiState.genre,
                    options = GENRES,
                    placeholder = stringResource(R.string.select_genre),
                    onSelect = viewModel::updateGenre,
                    modifier = Modifier.weight(1f)
                )
                LibriUnderlineField(
                    value = uiState.totalPages,
                    onValueChange = viewModel::updateTotalPages,
                    placeholder = stringResource(R.string.total_pages),
                    isError = uiState.totalPagesError,
                    supportingText = if (uiState.totalPagesError) {
                        stringResource(R.string.error_total_pages_invalid)
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Column {
                Text(
                    text = stringResource(R.string.reading_status),
                    style = LibriType.labelMd,
                    color = Libri.OnSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LibriSegmentedStatus(
                    selected = uiState.status,
                    onSelect = viewModel::updateStatus
                )
            }

            LibriPrimaryButton(
                text = stringResource(R.string.add_to_library),
                onClick = viewModel::save
            )
        }
    }
}

/**
 * The dashed "Add Cover" plate from the design. Once a search result is picked the
 * plate is replaced by the Open Library artwork, with a clear affordance.
 */
@Composable
private fun CoverSlot(
    coverUrl: String?,
    title: String,
    onClear: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        if (coverUrl == null) {
            Column(
                modifier = Modifier
                    .width(160.dp)
                    .height(240.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Libri.SurfaceContainer)
                    .border(1.dp, Libri.OutlineVariant, RoundedCornerShape(8.dp)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddPhotoAlternate,
                    contentDescription = null,
                    tint = Libri.OutlineVariant,
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    text = stringResource(R.string.add_cover),
                    style = LibriType.labelMd,
                    color = Libri.OutlineVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            Box {
                BookCover(
                    coverUrl = coverUrl,
                    title = title,
                    shape = RoundedCornerShape(8.dp),
                    elevation = 8.dp,
                    modifier = Modifier
                        .width(160.dp)
                        .height(240.dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(28.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(Libri.Primary)
                        .clickable(onClick = onClear),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.clear_cover),
                        tint = Libri.OnPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/** Inline results list — tapping a row fills the whole form. */
@Composable
private fun SearchResults(
    results: List<BookSearchResult>,
    onSelect: (BookSearchResult) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Libri.SurfaceContainerLowest)
            .border(1.dp, Libri.OutlineVariant, RoundedCornerShape(12.dp))
            .heightIn(max = 320.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.search_open_library),
                style = LibriType.labelSm,
                color = Libri.OnSurfaceVariant
            )
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = stringResource(R.string.close),
                tint = Libri.OnSurfaceVariant,
                modifier = Modifier
                    .size(18.dp)
                    .clickable(onClick = onDismiss)
            )
        }

        results.forEach { result ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(result) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BookCover(
                    coverUrl = result.coverUrl(CoverSize.SMALL),
                    title = result.title,
                    elevation = 2.dp,
                    modifier = Modifier
                        .width(36.dp)
                        .height(48.dp)
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = result.title,
                        style = LibriType.labelMd,
                        color = Libri.OnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = result.author,
                        style = LibriType.labelSm,
                        color = Libri.OnSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val meta = listOfNotNull(
                        result.firstPublishYear?.let { stringResource(R.string.first_published, it) },
                        result.totalPages?.let { stringResource(R.string.pages_count, it) }
                    ).joinToString(" · ")
                    if (meta.isNotEmpty()) {
                        Text(
                            text = meta,
                            style = LibriType.labelSm,
                            color = Libri.OutlineVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
