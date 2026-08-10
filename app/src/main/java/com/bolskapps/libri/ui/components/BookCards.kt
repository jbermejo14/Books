package com.bolskapps.libri.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bolskapps.libri.R
import com.bolskapps.libri.data.Book
import com.bolskapps.libri.data.CoverSize
import com.bolskapps.libri.ui.theme.Libri
import com.bolskapps.libri.ui.theme.LibriType

/**
 * Grid card for shelves without progress — "To Read", Wishlist, History and the
 * dashboard's "Recently Added".
 */
@Composable
fun GridBookCard(
    book: Book,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showRating: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        BookCoverPortrait(
            coverUrl = book.coverUrl(CoverSize.MEDIUM),
            title = book.title,
            modifier = Modifier.fillMaxWidth()
        )
        BookCaption(book = book, modifier = Modifier.padding(top = 12.dp))

        if (showRating) {
            StarRatingRow(
                rating = book.rating,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

/**
 * Grid card for the "In Progress" section: cover, caption, page counter with an
 * Update affordance, the thin amber bar and a right-aligned percentage.
 */
@Composable
fun ReadingBookCard(
    book: Book,
    onClick: () -> Unit,
    onUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        BookCoverPortrait(
            coverUrl = book.coverUrl(CoverSize.MEDIUM),
            title = book.title,
            modifier = Modifier.fillMaxWidth()
        )
        BookCaption(book = book, modifier = Modifier.padding(top = 12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.page_of, book.currentPage, book.totalPages),
                style = LibriType.labelSm,
                color = Libri.OnSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(onClick = onUpdate)
                    .padding(start = 8.dp, top = 2.dp, bottom = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = Libri.Primary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = stringResource(R.string.update),
                    style = LibriType.labelSm,
                    color = Libri.Primary,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        LibriProgressBar(
            progress = book.progress,
            modifier = Modifier.padding(top = 6.dp)
        )

        Text(
            text = stringResource(R.string.percent_complete, book.progressPercent),
            style = LibriType.labelSm,
            color = Libri.OnSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )
    }
}

@Composable
private fun BookCaption(
    book: Book,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = book.title,
            style = LibriType.labelMd,
            color = Libri.OnSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = book.author,
            style = LibriType.labelSm,
            color = Libri.OnSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
