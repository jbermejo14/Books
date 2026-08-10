package com.bolskapps.libri.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bolskapps.libri.R
import com.bolskapps.libri.data.Book
import com.bolskapps.libri.data.CoverSize
import com.bolskapps.libri.data.ReadingStatus
import com.bolskapps.libri.ui.AppViewModelProvider
import com.bolskapps.libri.ui.components.BookCover
import com.bolskapps.libri.ui.components.GridBookCard
import com.bolskapps.libri.ui.components.LibriBoxedField
import com.bolskapps.libri.ui.components.LibriPrimaryButton
import com.bolskapps.libri.ui.components.LibriProgressBar
import com.bolskapps.libri.ui.components.LibriTopBar
import com.bolskapps.libri.ui.components.BookSheet
import com.bolskapps.libri.ui.components.bookGridRows
import com.bolskapps.libri.ui.components.statusLabel
import com.bolskapps.libri.ui.theme.Libri
import com.bolskapps.libri.ui.theme.LibriType
import java.util.Calendar

@Composable
fun DashboardScreen(
    onSeeCollection: () -> Unit,
    onSearch: () -> Unit,
    onCategory: (String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val form by viewModel.progressForm.collectAsStateWithLifecycle()
    var sheetBook by remember { mutableStateOf<Book?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        LibriTopBar(
            title = stringResource(R.string.dashboard_title),
            actionIcon = Icons.Outlined.Search,
            onActionClick = onSearch,
            actionContentDescription = stringResource(R.string.search)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            item { GreetingBlock() }

            item {
                CurrentlyReadingCard(
                    count = uiState.activeCount,
                    hero = uiState.heroBook,
                    onHeroClick = { book -> sheetBook = book }
                )
            }

            item {
                UpdateProgressCard(
                    form = form,
                    enabled = uiState.heroBook != null,
                    onCurrentPageChange = viewModel::updateCurrentPage,
                    onTotalPagesChange = viewModel::updateTotalPages,
                    onSave = viewModel::saveProgress
                )
            }

            item { CategoryGrid(onCategory = onCategory) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = stringResource(R.string.recently_added),
                        style = LibriType.headlineMd,
                        color = Libri.Primary
                    )
                    Text(
                        text = stringResource(R.string.see_collection),
                        style = LibriType.labelMd,
                        color = Libri.Secondary,
                        modifier = Modifier.clickable(onClick = onSeeCollection)
                    )
                }
            }

            if (uiState.recentlyAdded.isEmpty() && !uiState.isLoading) {
                item {
                    Text(
                        text = stringResource(R.string.empty_dashboard),
                        style = LibriType.bodyMd,
                        color = Libri.OnSurfaceVariant
                    )
                }
            }

            // Two-column book grid inside a LazyColumn, one row per item.
            bookGridRows(uiState.recentlyAdded) { book ->
                GridBookCard(book = book, onClick = { sheetBook = book })
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

@Composable
private fun GreetingBlock() {
    val greetingRes = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> R.string.greeting_morning
            in 12..18 -> R.string.greeting_afternoon
            else -> R.string.greeting_evening
        }
    }
    Column {
        Text(
            text = stringResource(greetingRes),
            style = LibriType.headlineLgMobile,
            color = Libri.Primary
        )
        Text(
            text = stringResource(R.string.greeting_subtitle),
            style = LibriType.bodyMd,
            color = Libri.OnSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

/** The hero card: an amber rule on the leading edge, the active count, and a progress row. */
@Composable
private fun CurrentlyReadingCard(
    count: Int,
    hero: Book?,
    onHeroClick: (Book) -> Unit
) {
    LibriCard {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(96.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Libri.SecondaryContainer)
            )
            Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
                Text(
                    text = stringResource(R.string.currently_reading).uppercase(),
                    style = LibriType.labelMd,
                    color = Libri.OnSurfaceVariant
                )
                Text(
                    text = count.toString(),
                    style = LibriType.displayLg,
                    color = Libri.Primary
                )
                Text(
                    text = stringResource(R.string.books_actively_engaged),
                    style = LibriType.bodyMd,
                    color = Libri.OnSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Libri.Primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                    contentDescription = null,
                    tint = Libri.OnPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (hero == null) {
            Text(
                text = stringResource(R.string.no_books_in_progress),
                style = LibriType.bodyMd,
                color = Libri.OnSurfaceVariant,
                modifier = Modifier.padding(top = 24.dp)
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
                    .clickable { onHeroClick(hero) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                BookCover(
                    coverUrl = hero.coverUrl(CoverSize.SMALL),
                    title = hero.title,
                    modifier = Modifier
                        .width(48.dp)
                        .height(64.dp),
                    shape = RoundedCornerShape(4.dp)
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = hero.title,
                        style = LibriType.labelMd,
                        color = Libri.Primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    LibriProgressBar(
                        progress = hero.progress,
                        color = Libri.SecondaryFixedDim,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.percent_complete, hero.progressPercent),
                            style = LibriType.labelSm,
                            color = Libri.OnSurfaceVariant
                        )
                        Text(
                            text = stringResource(
                                R.string.page_of,
                                hero.currentPage,
                                hero.totalPages
                            ),
                            style = LibriType.labelSm,
                            color = Libri.OnSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdateProgressCard(
    form: ProgressForm,
    enabled: Boolean,
    onCurrentPageChange: (String) -> Unit,
    onTotalPagesChange: (String) -> Unit,
    onSave: () -> Unit
) {
    LibriCard {
        Text(
            text = stringResource(R.string.update_progress).uppercase(),
            style = LibriType.labelMd,
            color = Libri.OnSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        LibriBoxedField(
            value = form.currentPage,
            onValueChange = onCurrentPageChange,
            label = stringResource(R.string.current_page),
            isError = form.totalPagesValue > 0 && form.currentPageValue > form.totalPagesValue,
            keyboardOptions = KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                imeAction = androidx.compose.ui.text.input.ImeAction.Next
            )
        )
        Spacer(Modifier.height(12.dp))

        LibriBoxedField(
            value = form.totalPages,
            onValueChange = onTotalPagesChange,
            label = stringResource(R.string.total_pages_label),
            keyboardOptions = KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                imeAction = androidx.compose.ui.text.input.ImeAction.Done
            )
        )
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.live_preview),
                style = LibriType.labelSm,
                color = Libri.OnSurfaceVariant
            )
            Text(
                text = stringResource(R.string.percent_complete, form.previewPercent),
                style = LibriType.labelSm,
                color = Libri.Primary
            )
        }
        LibriProgressBar(
            progress = form.previewProgress,
            height = 8.dp,
            color = Libri.SecondaryFixedDim,
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(Modifier.height(20.dp))

        LibriPrimaryButton(
            text = stringResource(R.string.update_sanctuary),
            enabled = enabled && form.isValid,
            shape = RoundedCornerShape(8.dp),
            onClick = onSave
        )
    }
}

/** @param subject the Open Library subject to search; null opens Discover unfiltered. */
private data class Category(
    val labelRes: Int,
    val icon: ImageVector,
    val subject: String?
)

@Composable
private fun CategoryGrid(onCategory: (String?) -> Unit) {
    val categories = listOf(
        Category(R.string.category_philosophy, Icons.Outlined.Psychology, "Philosophy"),
        Category(R.string.category_art_history, Icons.Outlined.Brush, "Art History"),
        Category(R.string.category_science, Icons.Outlined.Science, "Science"),
        Category(R.string.category_view_all, Icons.Outlined.MoreHoriz, null)
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        categories.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { category ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Libri.SurfaceContainerLow)
                            .clickable { onCategory(category.subject) }
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = Libri.Primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = stringResource(category.labelRes),
                            style = LibriType.labelMd,
                            color = Libri.OnSurface
                        )
                    }
                }
            }
        }
    }
}

/** "Level 1" surface from DESIGN.md: white, 16dp radius, soft Ink Blue ambient shadow. */
@Composable
private fun LibriCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Libri.Primary,
                spotColor = Libri.Primary
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Libri.SurfaceContainerLowest)
            .padding(24.dp),
        content = content
    )
}
