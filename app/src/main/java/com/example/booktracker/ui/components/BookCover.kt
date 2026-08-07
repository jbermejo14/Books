package com.example.booktracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.booktracker.R
import com.example.booktracker.ui.theme.Libri
import com.example.booktracker.ui.theme.LibriType

/** Every cover in the app is a 2:3 portrait, per the design's `aspect-[2/3]`. */
const val COVER_ASPECT_RATIO = 2f / 3f

/**
 * Book cover with the design's ambient shadow (Ink Blue at 4%, never black).
 * Falls back to a typographic "paper" plate when Open Library has no cover.
 */
@Composable
fun BookCover(
    coverUrl: String?,
    title: String,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(4.dp),
    elevation: Dp = 4.dp
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Libri.Primary,
                spotColor = Libri.Primary
            )
            .clip(shape)
            .background(Libri.SurfaceContainerHighest)
    ) {
        if (coverUrl == null) {
            CoverPlaceholder(title)
        } else {
            SubcomposeAsyncImage(
                model = coverUrl,
                contentDescription = stringResource(R.string.cover_of, title),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { Box(Modifier.fillMaxSize().background(Libri.SurfaceContainerHigh)) },
                error = { CoverPlaceholder(title) }
            )
        }
    }
}

/** Same shape as [BookCover] but sized by the 2:3 ratio rather than by its parent. */
@Composable
fun BookCoverPortrait(
    coverUrl: String?,
    title: String,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(4.dp)
) {
    BookCover(
        coverUrl = coverUrl,
        title = title,
        shape = shape,
        modifier = modifier.aspectRatio(COVER_ASPECT_RATIO)
    )
}

@Composable
private fun CoverPlaceholder(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Libri.SurfaceContainerHigh)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = LibriType.headlineMd.copy(fontSize = MaterialTheme.typography.titleMedium.fontSize),
            color = Libri.OnSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
    }
}
