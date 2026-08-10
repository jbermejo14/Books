package com.bolskapps.libri.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bolskapps.libri.ui.theme.Libri

/**
 * DESIGN.md, Progress Bars: "a thin, Warm Amber line against a very light neutral
 * track, appearing elegant rather than gamified."
 *
 * Hand-rolled rather than Material's LinearProgressIndicator, whose M3 track gap and
 * stop-dot are off-spec here.
 */
@Composable
fun LibriProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 4.dp,
    color: Color = Libri.Secondary,
    trackColor: Color = Libri.SurfaceContainerHigh,
    animated: Boolean = true
) {
    val target = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = target,
        label = "libriProgress"
    )
    val shown = if (animated) animatedProgress else target

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(percent = 50))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(shown)
                .clip(RoundedCornerShape(percent = 50))
                .background(color)
        )
    }
}
