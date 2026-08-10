package com.bolskapps.libri.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.bolskapps.libri.R

/**
 * Both families ship as variable fonts, so each weight is registered against the
 * same file with an explicit `wght` axis value (requires API 26 — see minSdk).
 */
@OptIn(ExperimentalTextApi::class)
private fun variable(resId: Int, weight: FontWeight) = Font(
    resId = resId,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight))
)

/** Editorial voice: titles, section headers, hero numbers. */
val LibreCaslonText = FontFamily(
    variable(R.font.libre_caslon_text, FontWeight.Normal),
    variable(R.font.libre_caslon_text, FontWeight.Medium),
    variable(R.font.libre_caslon_text, FontWeight.SemiBold),
    variable(R.font.libre_caslon_text, FontWeight.Bold)
)

/** Functional voice: body copy, labels, metadata. */
val Manrope = FontFamily(
    variable(R.font.manrope, FontWeight.Normal),
    variable(R.font.manrope, FontWeight.Medium),
    variable(R.font.manrope, FontWeight.SemiBold),
    variable(R.font.manrope, FontWeight.Bold)
)

/** The eight named styles from DESIGN.md, mapped onto Material 3 slots. */
object LibriType {
    val displayLg = TextStyle(
        fontFamily = LibreCaslonText,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 56.sp,
        letterSpacing = (-0.96).sp // -0.02em
    )
    val headlineLg = TextStyle(
        fontFamily = LibreCaslonText,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    )

    /** The mobile step-down of headline-lg — the one actually used on phones. */
    val headlineLgMobile = TextStyle(
        fontFamily = LibreCaslonText,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    )
    val headlineMd = TextStyle(
        fontFamily = LibreCaslonText,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp
    )
    val bodyLg = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 28.sp
    )
    val bodyMd = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    )
    val labelMd = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.7.sp // 0.05em
    )
    val labelSm = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
}

val LibriTypography = Typography(
    displayLarge = LibriType.displayLg,
    displayMedium = LibriType.displayLg,
    headlineLarge = LibriType.headlineLg,
    headlineMedium = LibriType.headlineMd,
    headlineSmall = LibriType.headlineLgMobile,
    titleLarge = LibriType.headlineMd,
    titleMedium = LibriType.labelMd,
    titleSmall = LibriType.labelMd,
    bodyLarge = LibriType.bodyLg,
    bodyMedium = LibriType.bodyMd,
    bodySmall = LibriType.labelSm,
    labelLarge = LibriType.labelMd,
    labelMedium = LibriType.labelMd,
    labelSmall = LibriType.labelSm
)
