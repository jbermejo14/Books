package com.example.booktracker.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Libri palette, transcribed verbatim from Diseños/libri/DESIGN.md.
 * Names match the design tokens so a value can be traced back to the spec.
 */
object Libri {
    val Surface = Color(0xFFFBF9F8)
    val SurfaceDim = Color(0xFFDBD9D9)
    val SurfaceBright = Color(0xFFFBF9F8)
    val SurfaceContainerLowest = Color(0xFFFFFFFF)
    val SurfaceContainerLow = Color(0xFFF5F3F3)
    val SurfaceContainer = Color(0xFFEFEDED)
    val SurfaceContainerHigh = Color(0xFFEAE8E7)
    val SurfaceContainerHighest = Color(0xFFE4E2E2)
    val OnSurface = Color(0xFF1B1C1C)
    val OnSurfaceVariant = Color(0xFF43474D)
    val InverseSurface = Color(0xFF303030)
    val InverseOnSurface = Color(0xFFF2F0F0)
    val Outline = Color(0xFF74777D)
    val OutlineVariant = Color(0xFFC4C6CD)
    val SurfaceTint = Color(0xFF4C6078)

    /** Ink Blue — permanence of the printed word. */
    val Primary = Color(0xFF03192E)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFF1A2E44)
    val OnPrimaryContainer = Color(0xFF8296B0)
    val InversePrimary = Color(0xFFB4C8E4)

    /** Warm Amber — accents and reading progress. */
    val Secondary = Color(0xFF7D562D)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFFFCA98)
    val OnSecondaryContainer = Color(0xFF7A532A)

    val Tertiary = Color(0xFF181815)
    val OnTertiary = Color(0xFFFFFFFF)
    val TertiaryContainer = Color(0xFF2D2D29)
    val OnTertiaryContainer = Color(0xFF96948E)

    val Error = Color(0xFFBA1A1A)
    val OnError = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFFFDAD6)
    val OnErrorContainer = Color(0xFF93000A)

    val PrimaryFixed = Color(0xFFD1E4FF)
    val PrimaryFixedDim = Color(0xFFB4C8E4)
    val OnPrimaryFixed = Color(0xFF061D32)
    val OnPrimaryFixedVariant = Color(0xFF35485F)
    val SecondaryFixed = Color(0xFFFFDCBD)
    val SecondaryFixedDim = Color(0xFFF0BD8B)
    val OnSecondaryFixed = Color(0xFF2C1600)
    val OnSecondaryFixedVariant = Color(0xFF623F18)

    val Background = Color(0xFFFBF9F8)
    val OnBackground = Color(0xFF1B1C1C)
    val SurfaceVariant = Color(0xFFE4E2E2)

    /** "Ambient shadow": never black — always tinted with Ink Blue at 4%. */
    val AmbientShadow = Color(0x0A03192E)
}
