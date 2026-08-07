package com.example.booktracker.ui.pending

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.booktracker.R
import com.example.booktracker.data.Book
import com.example.booktracker.ui.AppViewModelProvider
import com.example.booktracker.ui.components.EmptyState
import com.example.booktracker.ui.components.GridBookCard
import com.example.booktracker.ui.components.LibriTopBar
import com.example.booktracker.ui.components.ReadingBookCard
import com.example.booktracker.ui.components.SectionHeader
import com.example.booktracker.ui.components.BookSheet
import com.example.booktracker.ui.components.bookGridRows
import com.example.booktracker.ui.theme.Libri
import com.example.booktracker.ui.theme.LibriType

@Composable
fun PendingScreen(
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PendingViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var sheetBook by remember { mutableStateOf<Book?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        LibriTopBar(
            title = stringResource(R.string.pending_title),
            navigationIcon = Icons.AutoMirrored.Outlined.MenuBook,
            actionIcon = Icons.Outlined.Search,
            onActionClick = onSearch,
            actionContentDescription = stringResource(R.string.search)
        )

        FilterBar(
            filter = uiState.filter,
            sort = uiState.sort,
            onFilterSelected = viewModel::selectFilter,
            onSortSelected = viewModel::selectSort
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            if (uiState.isEmpty) {
                item { EmptyState(message = stringResource(R.string.empty_pending)) }
            }

            if (uiState.showInProgress && uiState.inProgress.isNotEmpty()) {
                item { SectionHeader(text = stringResource(R.string.section_in_progress)) }
                bookGridRows(uiState.inProgress) { book ->
                    ReadingBookCard(
                        book = book,
                        onClick = { sheetBook = book },
                        onUpdate = { sheetBook = book }
                    )
                }
            }

            if (uiState.showToRead && uiState.toRead.isNotEmpty()) {
                item {
                    SectionHeader(
                        text = stringResource(R.string.section_to_read),
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
                bookGridRows(uiState.toRead) { book ->
                    GridBookCard(book = book, onClick = { sheetBook = book })
                }
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

/** The utility bar: scrollable chips on the left, a Sort menu on the right. */
@Composable
private fun FilterBar(
    filter: PendingFilter,
    sort: BookSort,
    onFilterSelected: (PendingFilter) -> Unit,
    onSortSelected: (BookSort) -> Unit
) {
    var sortExpanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PendingFilter.entries.forEach { entry ->
                    LibriChip(
                        label = stringResource(entry.labelRes),
                        selected = entry == filter,
                        onClick = { onFilterSelected(entry) }
                    )
                }
            }

            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .clickable { sortExpanded = true }
                        .padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Sort,
                        contentDescription = null,
                        tint = Libri.OnSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(R.string.sort),
                        style = LibriType.labelSm,
                        color = Libri.OnSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                DropdownMenu(
                    expanded = sortExpanded,
                    onDismissRequest = { sortExpanded = false }
                ) {
                    BookSort.entries.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(option.labelRes),
                                    style = LibriType.bodyMd,
                                    color = if (option == sort) Libri.Primary else Libri.OnSurface
                                )
                            },
                            onClick = {
                                onSortSelected(option)
                                sortExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Libri.SurfaceContainerHighest)
        )
    }
}

/** Pill tag: amber fill when active, hairline outline when not. */
@Composable
private fun LibriChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(percent = 50)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) Libri.SecondaryContainer else Libri.Surface)
            .then(
                if (selected) Modifier
                else Modifier.border(1.dp, Libri.OutlineVariant, shape)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = LibriType.labelSm,
            color = if (selected) Libri.OnSecondaryContainer else Libri.OnSurface,
            maxLines = 1
        )
    }
}
