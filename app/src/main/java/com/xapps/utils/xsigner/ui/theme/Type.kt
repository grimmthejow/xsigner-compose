package com.xapps.utils.xsigner.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.xapps.utils.xsigner.R

private val GoogleSansFlex = FontFamily(
    Font(
        resId = R.font.google_sans_flex,
        weight = FontWeight.Thin,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(100),
            FontVariation.width(100f),
            FontVariation.Setting("ROND", 0f),
            FontVariation.Setting("GRAD", 0f),
            FontVariation.Setting("opsz", 14f)
        )
    ),
    Font(
        resId = R.font.google_sans_flex,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(400),
            FontVariation.width(100f),
            FontVariation.Setting("ROND", 0f),
            FontVariation.Setting("GRAD", 0f),
            FontVariation.Setting("opsz", 14f)
        )
    ),
    Font(
        resId = R.font.google_sans_flex,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(700),
            FontVariation.width(100f),
            FontVariation.Setting("ROND", 0f),
            FontVariation.Setting("GRAD", 0f),
            FontVariation.Setting("opsz", 14f)
        )
    )
)

private fun appTextStyle(
    size: Int,
    weight: Int,
    lineHeight: Int,
    rond: Float = 0f,
    grad: Float = 0f,
    width: Float = 100f,
    slant: Float = 0f
): TextStyle {
    val family = FontFamily(
        Font(
            resId = R.font.google_sans_flex,
            weight = FontWeight(weight),
            variationSettings = FontVariation.Settings(
                FontVariation.weight(weight),
                FontVariation.width(width),
                FontVariation.slant(slant),
                FontVariation.Setting("ROND", rond),
                FontVariation.Setting("GRAD", grad),
                FontVariation.Setting("opsz", size.toFloat())
            )
        )
    )

    return TextStyle(
        fontFamily = family,
        fontSize = size.sp,
        fontWeight = FontWeight(weight),
        lineHeight = lineHeight.sp
    )
}

val AppTypography = Typography(
    displayLarge = appTextStyle(57, 650, 64, 100f),
    displayMedium = appTextStyle(45, 600, 52, 16f),
    displaySmall = appTextStyle(36, 600, 44, 12f),
    headlineLarge = appTextStyle(32, 700, 40, 100f, width = 125f),
    headlineMedium = appTextStyle(28, 550, 36, 8f),
    headlineSmall = appTextStyle(24, 550, 32, 6f),
    titleLarge = appTextStyle(22, 600, 28, 100f),
    titleMedium = appTextStyle(16, 550, 24, 2f),
    titleSmall = appTextStyle(14, 550, 20),
    bodyLarge = appTextStyle(16, 400, 24),
    bodyMedium = appTextStyle(14, 400, 20),
    bodySmall = appTextStyle(12, 400, 16),
    labelLarge = appTextStyle(14, 600, 20),
    labelMedium = appTextStyle(12, 550, 16),
    labelSmall = appTextStyle(11, 500, 16),
)