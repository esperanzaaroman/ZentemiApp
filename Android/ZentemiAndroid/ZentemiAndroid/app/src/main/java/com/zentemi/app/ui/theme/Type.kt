package com.zentemi.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.zentemi.app.R

// Quicksand (variable font, res/font/quicksand.ttf)
@OptIn(ExperimentalTextApi::class)
private fun quicksand(weight: FontWeight) = Font(
    R.font.quicksand,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight))
)

val Quicksand = FontFamily(
    quicksand(FontWeight.Light),
    quicksand(FontWeight.Normal),
    quicksand(FontWeight.Medium),
    quicksand(FontWeight.SemiBold),
    quicksand(FontWeight.Bold)
)

/**
 * Escala de tipografía: todos los estilos en Quicksand (antes algunos caían en
 * Roboto), con interlineado fijo y títulos un poco más apretados.
 */
private fun style(
    size: Int,
    weight: FontWeight,
    line: Int,
    tracking: TextUnit = 0.em
) = TextStyle(
    fontFamily = Quicksand,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking
)

val Typography = Typography(
    displayLarge = style(34, FontWeight.Bold, 40, (-0.02).em),
    displayMedium = style(30, FontWeight.Bold, 36, (-0.02).em),
    displaySmall = style(27, FontWeight.Bold, 33, (-0.015).em),
    headlineLarge = style(25, FontWeight.Bold, 31, (-0.01).em),
    headlineMedium = style(22, FontWeight.Bold, 28, (-0.01).em),
    headlineSmall = style(20, FontWeight.Bold, 26, (-0.005).em),
    titleLarge = style(19, FontWeight.Bold, 25),
    titleMedium = style(16, FontWeight.SemiBold, 22),
    titleSmall = style(14, FontWeight.SemiBold, 20),
    bodyLarge = style(16, FontWeight.Medium, 23),
    bodyMedium = style(14, FontWeight.Medium, 20),
    bodySmall = style(12, FontWeight.Medium, 17),
    labelLarge = style(14, FontWeight.Bold, 18, 0.01.em),
    labelMedium = style(13, FontWeight.SemiBold, 17, 0.01.em),
    labelSmall = style(11, FontWeight.Bold, 15, 0.03.em)
)
