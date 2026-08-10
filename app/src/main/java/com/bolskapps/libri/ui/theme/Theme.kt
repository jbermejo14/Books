package com.bolskapps.libri.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LibriColorScheme = lightColorScheme(
    primary = Libri.Primary,
    onPrimary = Libri.OnPrimary,
    primaryContainer = Libri.PrimaryContainer,
    onPrimaryContainer = Libri.OnPrimaryContainer,
    inversePrimary = Libri.InversePrimary,
    secondary = Libri.Secondary,
    onSecondary = Libri.OnSecondary,
    secondaryContainer = Libri.SecondaryContainer,
    onSecondaryContainer = Libri.OnSecondaryContainer,
    tertiary = Libri.Tertiary,
    onTertiary = Libri.OnTertiary,
    tertiaryContainer = Libri.TertiaryContainer,
    onTertiaryContainer = Libri.OnTertiaryContainer,
    error = Libri.Error,
    onError = Libri.OnError,
    errorContainer = Libri.ErrorContainer,
    onErrorContainer = Libri.OnErrorContainer,
    background = Libri.Background,
    onBackground = Libri.OnBackground,
    surface = Libri.Surface,
    onSurface = Libri.OnSurface,
    surfaceVariant = Libri.SurfaceVariant,
    onSurfaceVariant = Libri.OnSurfaceVariant,
    surfaceTint = Libri.SurfaceTint,
    inverseSurface = Libri.InverseSurface,
    inverseOnSurface = Libri.InverseOnSurface,
    outline = Libri.Outline,
    outlineVariant = Libri.OutlineVariant,
    surfaceBright = Libri.SurfaceBright,
    surfaceDim = Libri.SurfaceDim,
    surfaceContainerLowest = Libri.SurfaceContainerLowest,
    surfaceContainerLow = Libri.SurfaceContainerLow,
    surfaceContainer = Libri.SurfaceContainer,
    surfaceContainerHigh = Libri.SurfaceContainerHigh,
    surfaceContainerHighest = Libri.SurfaceContainerHighest
)

/** DESIGN.md "Shapes": 8dp for controls, 16dp for containers, pill for tags. */
private val LibriShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

/**
 * Libri is a light-only, "Paper White sanctuary" identity — DESIGN.md ships a single
 * palette, so no dark scheme is declared rather than inventing one off-spec.
 * Dynamic colour is deliberately off: the brand palette is the point.
 */
@Composable
fun LibriTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LibriColorScheme,
        typography = LibriTypography,
        shapes = LibriShapes,
        content = content
    )
}
