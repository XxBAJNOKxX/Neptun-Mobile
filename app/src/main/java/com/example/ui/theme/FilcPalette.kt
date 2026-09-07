package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * A Filc paletta levezetett színrendszere.
 *
 * A `reFilc` forrásaiban a háttér- és a lapfelületek nem hardcoded értékekből,
 * hanem az aktuális kiemelésszín (accent) és a fehér/fekete keverékéből állnak
 * elő (`ColorsUtils().lighten/darken`). Ezt a logikát vetítjük át Compose-ra: a
 * `FILC` lime accent esetén az eredmény pontosan az eredeti app
 * `#FAFFF0` / `#F3FBDE` / `#0D1202` / `#141905` értékei, bármelyik másik
 * accentnél pedig ugyanaz a tónusképlet, csak az adott színhez igazítva.
 */
@Immutable
data class FilcPalette(
    /** Éles kiemelésszín (gombok, indikátorok, aktív tab). */
    val accent: Color,
    val background: Color,
    /** Lapok / panelek felülete. */
    val surface: Color,
    /** A felületnél világosabb, "sima" gombháttér (#FEFFFD / #20290B). */
    val surfaceRaised: Color,
    /** Navigációs sáv, lap alja – a háttér mérsékelt változata. */
    val backgroundDeep: Color,
    val text: Color,
    val textSecondary: Color,
    val textMuted: Color,
    /** A Filc-re jellemző kék ikon szín. */
    val icon: Color,
    val onAccent: Color,
    /** Kártyák halvány kerete / elválasztó vonal. */
    val hairline: Color,
    val stroke: Color,
    val shadow: Color,
    val isLight: Boolean,
    // Állapotszínek
    val red: Color,
    val orange: Color,
    val yellow: Color,
    val green: Color,
    val teal: Color,
    val blue: Color,
    val indigo: Color,
    val purple: Color,
    val pink: Color,
    // Jegyszínek
    val gradeFive: Color,
    val gradeFour: Color,
    val gradeThree: Color,
    val gradeTwo: Color,
    val gradeOne: Color
) {
    /** 5..1 jegyek színezése – a reFilc `gradeColor()` mintájára. */
    fun gradeColor(grade: Int?): Color = when (grade) {
        5 -> gradeFive
        4 -> gradeFour
        3 -> gradeThree
        2 -> gradeTwo
        1 -> gradeOne
        else -> textMuted
    }

    /** Tanulmányi átlag színezése (0,0 = nincs adat). */
    fun averageColor(average: Double): Color = when {
        average <= 0.0 -> textMuted
        average >= 4.5 -> gradeFive
        average >= 4.0 -> gradeFour
        average >= 3.0 -> gradeThree
        average >= 2.0 -> gradeTwo
        else -> gradeOne
    }

    /** A szellemjegyes ("ghost") mód színe. */
    val ghost: Color
        get() = purple

    val scrim: Color
        get() = if (isLight) Color(0x66000000) else Color(0x99000000)

    companion object {
        fun of(accent: Color, dark: Boolean): FilcPalette {
            val white = Color(0xFFFFFFFF)
            val black = Color(0xFF000000)
            val isFilc = accent == FilcLime

            return if (dark) {
                FilcPalette(
                    accent = accent,
                    background = if (isFilc) FilcDarkBackground else lerp(accent, black, 0.925f),
                    surface = if (isFilc) FilcDarkSurface else lerp(accent, black, 0.873f),
                    surfaceRaised = if (isFilc) FilcDarkRaised else lerp(accent, black, 0.79f),
                    backgroundDeep = if (isFilc) FilcDarkDeep else lerp(accent, black, 0.955f),
                    text = white,
                    textSecondary = white.copy(alpha = 0.80f),
                    textMuted = white.copy(alpha = 0.55f),
                    icon = FilcLoginIconDark,
                    onAccent = white.copy(alpha = 0.92f),
                    hairline = white.copy(alpha = 0.08f),
                    stroke = lerp(accent, white, 0.24f).copy(alpha = 0.35f),
                    shadow = lerp(accent, black, 0.5f),
                    isLight = false,
                    red = StatusRedDark,
                    orange = StatusOrangeDark,
                    yellow = StatusYellowDark,
                    green = StatusGreenDark,
                    teal = StatusTealDark,
                    blue = StatusBlueDark,
                    indigo = StatusIndigoDark,
                    purple = StatusPurpleDark,
                    pink = StatusPinkDark,
                    gradeFive = GradeFive,
                    gradeFour = GradeFour,
                    gradeThree = GradeThree,
                    gradeTwo = GradeTwo,
                    gradeOne = GradeOne
                )
            } else {
                FilcPalette(
                    accent = accent,
                    background = if (isFilc) FilcLightBackground else lerp(accent, white, 0.945f),
                    surface = if (isFilc) FilcLightSurface else lerp(accent, white, 0.867f),
                    surfaceRaised = if (isFilc) FilcLightRaised else lerp(accent, white, 0.99f),
                    backgroundDeep = if (isFilc) FilcLightDeep else lerp(accent, white, 0.985f),
                    text = black,
                    textSecondary = black.copy(alpha = 0.75f),
                    textMuted = black.copy(alpha = 0.55f),
                    icon = FilcLoginIconLight,
                    onAccent = black.copy(alpha = 0.90f),
                    hairline = black.copy(alpha = 0.06f),
                    stroke = lerp(FilcLoginIconLight, white, 0.68f),
                    shadow = Color(0xFFE8E8E8),
                    isLight = true,
                    red = StatusRedLight,
                    orange = StatusOrangeLight,
                    yellow = StatusYellowLight,
                    green = StatusGreenLight,
                    teal = StatusTealLight,
                    blue = StatusBlueLight,
                    indigo = StatusIndigoLight,
                    purple = StatusPurpleLight,
                    pink = StatusPinkLight,
                    gradeFive = GradeFive,
                    gradeFour = GradeFour,
                    gradeThree = GradeThree,
                    gradeTwo = GradeTwo,
                    gradeOne = GradeOne
                )
            }
        }
    }
}
