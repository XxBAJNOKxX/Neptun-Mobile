package com.example.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * A Neptun Mobile "Filc" arculatának nyers palettája.
 *
 * A színek a QwIT Development által fejlesztett reFilc (Filc Napló) alkalmazás
 * `theme/colors/` mappájából származnak, azt a vizuális világot hozza le
 * Compose Material3-ra.
 */

// -- Márkaszín -------------------------------------------------------------
/** Eredeti "filc" lime. */
val FilcLime = Color(0xFFA7DC22)

/** Régi, Filc Napló-beli türkiz. */
val FilcOgTeal = Color(0xFF20AC9B)

// -- Palettareferenciák: a reFilc `ColorUtils().lighten/darken` képletének ---
// -- pontosan az eredménye a FILC accent esetén, ezért külön konstansban.   ---
internal val FilcLightBackground = Color(0xFFFAFFF0)
internal val FilcLightSurface = Color(0xFFF3FBDE)
internal val FilcLightRaised = Color(0xFFFEFFFD)
internal val FilcLightDeep = Color(0xFFFDFEF8)
internal val FilcDarkBackground = Color(0xFF0D1202)
internal val FilcDarkSurface = Color(0xFF141905)
internal val FilcDarkRaised = Color(0xFF20290B)
internal val FilcDarkDeep = Color(0xFF0A0E01)

// -- V5 "login" paletta ----------------------------------------------------
internal val FilcLoginPrimaryText = Color(0xFF394C0A)
internal val FilcLoginIconLight = Color(0xFF0A2456)
internal val FilcLoginIconDark = Color(0xFFBAD1FF)

// -- iOS-szerű állapotszínek (világos / sötét) -----------------------------
internal val StatusRedLight = Color(0xFFFF3B30)
internal val StatusOrangeLight = Color(0xFFFF9500)
internal val StatusYellowLight = Color(0xFFFFCC00)
internal val StatusGreenLight = Color(0xFF34C759)
internal val StatusTealLight = Color(0xFF5AC8FA)
internal val StatusBlueLight = Color(0xFF007AFF)
internal val StatusIndigoLight = Color(0xFF5856D6)
internal val StatusPurpleLight = Color(0xFFAF52DE)
internal val StatusPinkLight = Color(0xFFFF2D55)

internal val StatusRedDark = Color(0xFFFF453A)
internal val StatusOrangeDark = Color(0xFFFF9F0A)
internal val StatusYellowDark = Color(0xFFFFD60A)
internal val StatusGreenDark = Color(0xFF32D74B)
internal val StatusTealDark = Color(0xFF64D2FF)
internal val StatusBlueDark = Color(0xFF0A84FF)
internal val StatusIndigoDark = Color(0xFF5E5CE6)
internal val StatusPurpleDark = Color(0xFFBF5AF2)
internal val StatusPinkDark = Color(0xFFFF375F)

// -- Jegyszínek (5..1) -----------------------------------------------------
internal val GradeFive = Color(0xFF22CCAD)
internal val GradeFour = Color(0xFF92EA3B)
internal val GradeThree = Color(0xFFF9CF00)
internal val GradeTwo = Color(0xFFFFA046)
internal val GradeOne = Color(0xFFFF54A1)

/**
 * Egy tantárgy nevéből determinisztikus színt képez – a reFilc
 * `ColorUtils().stringToColor()` megfelelője. A profilképek / avatárok
 * háttérszínére használjuk.
 */
val AvatarPalette = listOf(
    Color(0xFFA7DC22),
    Color(0xFF22CCAD),
    Color(0xFF5AC8FA),
    Color(0xFF007AFF),
    Color(0xFF5856D6),
    Color(0xFFAF52DE),
    Color(0xFFFF2D55),
    Color(0xFFFF9500),
    Color(0xFFFFCC00),
    Color(0xFF34C759)
)

fun stringToAvatarColor(raw: String): Color {
    val key = raw.trim()
    if (key.isEmpty()) return FilcLime
    var hash = 0
    for (ch in key) {
        hash = (hash * 31 + ch.code) % 100003
    }
    return AvatarPalette[hash % AvatarPalette.size]
}
