package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * A Filc tipográfia: Montserrat, akárcsak a reFilcben
 * (`_defaultFontFamily = "Montserrat"`). A betűtípust beépítjük (`res/font`),
 * hogy offline is ugyanazt a duci, kerek karaktervilágot kapjuk.
 */
val FilcFontFamily = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold)
)

private val lineHeightStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None
)

private fun filcStyle(
    size: Int,
    weight: FontWeight,
    lineHeight: Int,
    tracking: Float = 0f
) = TextStyle(
    fontFamily = FilcFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = tracking.sp,
    lineHeightStyle = lineHeightStyle
)

/**
 * A Filc tipográfiai skálája. A `titleMedium` a panelek címe (félkövér, mint a
 * reFilc `PanelTitle` stílusa), a `titleLarge` az üdvözlő szöveg.
 */
val Typography = Typography(
    displayLarge = filcStyle(34, FontWeight.Bold, 42, -0.6f),
    displayMedium = filcStyle(30, FontWeight.Bold, 38, -0.5f),
    displaySmall = filcStyle(26, FontWeight.Bold, 34, -0.4f),
    headlineLarge = filcStyle(24, FontWeight.Bold, 32, -0.3f),
    headlineMedium = filcStyle(22, FontWeight.Bold, 30, -0.3f),
    headlineSmall = filcStyle(20, FontWeight.Bold, 28, -0.2f),
    titleLarge = filcStyle(18, FontWeight.Bold, 26, -0.1f),
    titleMedium = filcStyle(16, FontWeight.SemiBold, 24),
    titleSmall = filcStyle(14, FontWeight.SemiBold, 20),
    bodyLarge = filcStyle(16, FontWeight.Normal, 24),
    bodyMedium = filcStyle(14, FontWeight.Normal, 21),
    bodySmall = filcStyle(13, FontWeight.Medium, 19),
    labelLarge = filcStyle(15, FontWeight.SemiBold, 21),
    labelMedium = filcStyle(13, FontWeight.Medium, 18),
    labelSmall = filcStyle(11, FontWeight.Medium, 15, 0.2f)
)

/** Kerekített, félszéles számstílus – átlagokhoz, counterekhez. */
val FilcNumberStyle = filcStyle(15, FontWeight.SemiBold, 18)
