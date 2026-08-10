package com.bolskapps.libri.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bolskapps.libri.R
import com.bolskapps.libri.data.MAX_RATING
import com.bolskapps.libri.ui.theme.Libri

/**
 * Read-only rating: [rating] filled stars then outlined ones, in the Warm Amber
 * accent. Announced to TalkBack as a single value rather than five icons.
 */
@Composable
fun StarRatingRow(
    rating: Int,
    modifier: Modifier = Modifier,
    starSize: Dp = 16.dp
) {
    val description = stringResource(R.string.star_rating, rating)
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (star in 1..MAX_RATING) {
            val filled = star <= rating
            Icon(
                imageVector = if (filled) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = null,
                tint = if (filled) Libri.Secondary else Libri.OutlineVariant,
                modifier = Modifier.size(starSize)
            )
        }
    }
}

/** Selectable rating. Tapping the current value clears it back to unrated. */
@Composable
fun StarRatingPicker(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    starSize: Dp = 32.dp
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (star in 1..MAX_RATING) {
            val filled = star <= rating
            IconButton(onClick = { onRatingChange(if (rating == star) 0 else star) }) {
                Icon(
                    imageVector = if (filled) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = stringResource(R.string.rate_n_stars, star),
                    tint = if (filled) Libri.Secondary else Libri.OutlineVariant,
                    modifier = Modifier.size(starSize)
                )
            }
        }
    }
}
