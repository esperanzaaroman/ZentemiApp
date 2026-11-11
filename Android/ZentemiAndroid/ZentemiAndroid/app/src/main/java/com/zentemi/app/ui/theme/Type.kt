package com.zentemi.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
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

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = Quicksand,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = Quicksand,
        fontWeight = FontWeight.Bold,
        fontSize = 25.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Quicksand,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Quicksand,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Quicksand,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Quicksand,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Quicksand,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Quicksand,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Quicksand,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Quicksand,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Quicksand,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Quicksand,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Quicksand,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp
    )
)
