package com.bolskapps.libri.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bolskapps.libri.R
import com.bolskapps.libri.data.Book
import com.bolskapps.libri.data.CoverSize
import com.bolskapps.libri.data.MAX_RATING
import com.bolskapps.libri.data.ReadingStatus
import com.bolskapps.libri.ui.theme.Libri
import com.bolskapps.libri.ui.theme.LibriType

/**
 * The one editor in the app. The designs have no book-detail screen — progress is
 * edited through the dashboard's "Update Progress" card — so that card is lifted into
 * a bottom sheet and extended with the status, rating and review controls every shelf
 * (and the search page) needs.
 *
 * @param isNew true when [book] is a draft from search that is not saved yet.
 * @param meta optional "1976 · 504 pages" line from the Open Library record.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookSheet(
    book: Book,
    onDismiss: () -> Unit,
    onSave: (Book) -> Unit,
    isNew: Boolean = false,
    meta: String? = null,
    onDelete: ((Book) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Keyed on identity so opening a different book reseeds every field.
    val formKey = if (isNew) book.openLibraryKey ?: book.title else book.id.toString()
    var currentPage by rememberSaveable(formKey) { mutableStateOf(book.currentPage.toString()) }
    var totalPages by rememberSaveable(formKey) { mutableStateOf(book.totalPages.toString()) }
    var notes by rememberSaveable(formKey) { mutableStateOf(book.notes.orEmpty()) }
    var rating by rememberSaveable(formKey) { mutableStateOf(book.rating) }
    // Held as a name so the state survives config changes without a custom Saver.
    var statusName by rememberSaveable(formKey) { mutableStateOf(book.status.name) }

    val status = ReadingStatus.fromName(statusName)
    val currentValue = currentPage.toIntOrNull() ?: 0
    val totalValue = totalPages.toIntOrNull() ?: 0
    val previewProgress = remember(currentValue, totalValue) {
        if (totalValue <= 0) 0f else (currentValue.toFloat() / totalValue).coerceIn(0f, 1f)
    }

    // A wishlist entry is aspirational: page tracking would be noise.
    val tracksProgress = status != ReadingStatus.WISHLIST
    val pageError = tracksProgress && totalValue > 0 && currentValue > totalValue
    val canSave = !pageError

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Libri.SurfaceContainerLowest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                BookCover(
                    coverUrl = book.coverUrl(CoverSize.MEDIUM),
                    title = book.title,
                    modifier = Modifier
                        .width(64.dp)
                        .height(96.dp),
                    shape = RoundedCornerShape(4.dp)
                )
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(
                        text = book.title,
                        style = LibriType.headlineMd,
                        color = Libri.Primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = book.author,
                        style = LibriType.labelSm,
                        color = Libri.OnSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (meta != null) {
                        Text(
                            text = meta,
                            style = LibriType.labelSm,
                            color = Libri.OutlineVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = stringResource(R.string.reading_status),
                style = LibriType.labelMd,
                color = Libri.OnSurfaceVariant
            )
            LibriSegmentedStatus(
                selected = status,
                onSelect = { statusName = it.name }
            )

            if (tracksProgress) {
                Text(
                    text = stringResource(R.string.update_progress).uppercase(),
                    style = LibriType.labelMd,
                    color = Libri.OnSurfaceVariant
                )

                LibriBoxedField(
                    value = currentPage,
                    onValueChange = { currentPage = it.filter(Char::isDigit).take(6) },
                    label = stringResource(R.string.current_page),
                    isError = pageError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )

                LibriBoxedField(
                    value = totalPages,
                    onValueChange = { totalPages = it.filter(Char::isDigit).take(6) },
                    label = stringResource(R.string.total_pages_label),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )

                if (pageError) {
                    Text(
                        text = stringResource(R.string.error_current_page_invalid),
                        style = LibriType.labelSm,
                        color = Libri.Error
                    )
                }

                Column {
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
                            text = stringResource(
                                R.string.percent_complete,
                                (previewProgress * 100).toInt()
                            ),
                            style = LibriType.labelSm,
                            color = Libri.Primary
                        )
                    }
                    LibriProgressBar(
                        progress = previewProgress,
                        height = 8.dp,
                        color = Libri.SecondaryFixedDim,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.your_rating),
                    style = LibriType.labelMd,
                    color = Libri.OnSurfaceVariant
                )
                StarRatingPicker(
                    rating = rating,
                    onRatingChange = { rating = it.coerceIn(0, MAX_RATING) }
                )
            }

            LibriNotesField(
                value = notes,
                onValueChange = { notes = it.take(MAX_NOTES_LENGTH) },
                label = stringResource(R.string.your_notes),
                placeholder = stringResource(R.string.notes_placeholder)
            )

            LibriPrimaryButton(
                text = stringResource(
                    if (isNew) R.string.add_to_library else R.string.update_sanctuary
                ),
                enabled = canSave,
                shape = RoundedCornerShape(8.dp),
                onClick = {
                    onSave(
                        book.copy(
                            currentPage = if (tracksProgress) currentValue else 0,
                            totalPages = totalValue,
                            status = status,
                            rating = rating,
                            notes = notes
                        )
                    )
                    onDismiss()
                }
            )

            if (onDelete != null) {
                TextButton(
                    onClick = {
                        onDelete(book)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.delete_book),
                        style = LibriType.labelMd,
                        color = Libri.Error
                    )
                }
            }
        }
    }
}

private const val MAX_NOTES_LENGTH = 2000
