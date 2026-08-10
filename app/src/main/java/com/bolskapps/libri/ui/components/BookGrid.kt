package com.bolskapps.libri.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bolskapps.libri.R
import com.bolskapps.libri.data.Book
import com.bolskapps.libri.data.ReadingStatus

/**
 * Emits a book grid as rows of a [LazyListScope], so a screen can interleave grids
 * with headers and cards in one scrolling column (LazyVerticalGrid cannot nest).
 */
fun LazyListScope.bookGridRows(
    books: List<Book>,
    columns: Int = 2,
    horizontalSpacing: Dp = 16.dp,
    itemContent: @Composable (Book) -> Unit
) {
    if (books.isEmpty()) return
    val rows = books.chunked(columns)

    items(
        count = rows.size,
        key = { index -> "grid-${rows[index].first().id}" }
    ) { index ->
        val row = rows[index]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
        ) {
            row.forEach { book ->
                Box(modifier = Modifier.weight(1f)) { itemContent(book) }
            }
            // Keeps a short final row left-aligned instead of stretching its cards.
            repeat(columns - row.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun statusLabel(status: ReadingStatus): String = stringResource(
    when (status) {
        ReadingStatus.WISHLIST -> R.string.status_wishlist
        ReadingStatus.TO_READ -> R.string.status_to_read
        ReadingStatus.READING -> R.string.status_reading
        ReadingStatus.FINISHED -> R.string.status_finished
    }
)
