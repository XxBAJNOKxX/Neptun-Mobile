package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

enum class ThemeMode(val title: String, val description: String) {
    SYSTEM("Rendszer", "Követi a telefon beállításait"),
    LIGHT("Világos", "Halvány lime hátterű nappali mód"),
    DARK("Sötét", "Éjszakai, szemkímélő Filc mód")
}

/**
 * Filc-féle accent választék – a reFilc `AccentColor` enumjának megfelelője.
 *
 * Minden accenthez tartozik egy levezetett háttér- és felületszín-paletta,
 * így a beállításokban választható szín az egész app arculását átszínezi.
 * A dynamic color (Material You) kapcsoló a reFilc `adaptive` accentjének
 * felel meg: abban a módban a rendszerpaletta elsődleges színe a kezdeti
 * accent, amihez a háttér ugyanúgy odaigazodik.
 */
enum class AppAccentColor(
    val id: String,
    val title: String,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color
) {
    FILC(
        id = "filc",
        title = "Filc Lime",
        primary = Color(0xFFA7DC22),
        secondary = Color(0xFFA7DC22),
        tertiary = Color(0xFF8FBE16)
    ),
    OG_FILC(
        id = "ogfilc",
        title = "Régi Filc",
        primary = Color(0xFF20AC9B),
        secondary = Color(0xFF20AC9B),
        tertiary = Color(0xFF198B7E)
    ),
    BLUE(
        id = "blue",
        title = "Kék",
        primary = Color(0xFF64A6F9),
        secondary = Color(0xFF64A6F9),
        tertiary = Color(0xFF4A8FE0)
    ),
    GREEN(
        id = "green",
        title = "Zöld",
        primary = Color(0xFF5BB97A),
        secondary = Color(0xFF5BB97A),
        tertiary = Color(0xFF479A61)
    ),
    LIME(
        id = "lime",
        title = "Lime",
        primary = Color(0xFF9CCC65),
        secondary = Color(0xFF9CCC65),
        tertiary = Color(0xFF83B24C)
    ),
    YELLOW(
        id = "yellow",
        title = "Narancssárga",
        primary = Color(0xFFFFB74D),
        secondary = Color(0xFFFFB74D),
        tertiary = Color(0xFFE39B30)
    ),
    ORANGE(
        id = "orange",
        title = "Vörösneszín",
        primary = Color(0xFFFF8A65),
        secondary = Color(0xFFFF8A65),
        tertiary = Color(0xFFE56B45)
    ),
    RED(
        id = "red",
        title = "Piros",
        primary = Color(0xFFEF5350),
        secondary = Color(0xFFEF5350),
        tertiary = Color(0xFFCE3B38)
    ),
    PINK(
        id = "pink",
        title = "Rózsaszín",
        primary = Color(0xFFF06292),
        secondary = Color(0xFFF06292),
        tertiary = Color(0xFFD14A78)
    ),
    PURPLE(
        id = "purple",
        title = "Lila",
        primary = Color(0xFFB39DDB),
        secondary = Color(0xFFB39DDB),
        tertiary = Color(0xFF9575CD)
    ),
    MONO(
        id = "none",
        title = "Egyszínű",
        primary = Color(0xFF9E9E9E),
        secondary = Color(0xFF757575),
        tertiary = Color(0xFF616161)
    );

    val paletteLight: FilcPalette
        get() = FilcPalette.of(primary, dark = false)

    val paletteDark: FilcPalette
        get() = FilcPalette.of(primary, dark = true)

    fun buildLightScheme(): ColorScheme = buildScheme(dark = false)

    fun buildDarkScheme(): ColorScheme = buildScheme(dark = true)

    private fun buildScheme(dark: Boolean): ColorScheme {
        val p = FilcPalette.of(primary, dark)
        return if (dark) {
            darkColorScheme(
                primary = p.accent,
                onPrimary = p.onAccent,
                primaryContainer = lerp(p.accent, Color.Black, 0.55f),
                onPrimaryContainer = lerp(p.accent, Color.White, 0.85f),
                secondary = p.accent,
                onSecondary = p.onAccent,
                tertiary = p.accent,
                onTertiary = p.onAccent,
                background = p.background,
                onBackground = p.text,
                surface = p.surface,
                onSurface = p.text,
                surfaceVariant = p.surface,
                onSurfaceVariant = p.textSecondary,
                outline = p.stroke,
                outlineVariant = p.hairline,
                error = p.red,
                scrim = Color.Black
            )
        } else {
            lightColorScheme(
                primary = p.accent,
                onPrimary = p.onAccent,
                primaryContainer = lerp(p.accent, Color.White, 0.72f),
                onPrimaryContainer = lerp(p.accent, Color.Black, 0.72f),
                secondary = p.accent,
                onSecondary = p.onAccent,
                tertiary = p.accent,
                onTertiary = p.onAccent,
                background = p.background,
                onBackground = p.text,
                surface = p.surface,
                onSurface = p.text,
                surfaceVariant = p.surface,
                onSurfaceVariant = p.textSecondary,
                outline = p.stroke,
                outlineVariant = p.hairline,
                error = p.red,
                scrim = Color.Black
            )
        }
    }

    companion object {
        fun fromId(id: String): AppAccentColor {
            // A korábbi verziókbeli accent id-k (pl. "emerald", "gold") is
            // feloldhatók maradnak, hogy a mentett beállítások ne törjenek.
            val alias = when (id.lowercase()) {
                "emerald" -> "green"
                "gold" -> "yellow"
                "crimson" -> "red"
                "rose" -> "pink"
                "indigo" -> "purple"
                "cyan" -> "blue"
                else -> id.lowercase()
            }
            return entries.firstOrNull { it.id == alias } ?: FILC
        }
    }
}

data class ThemeSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColor: Boolean = false,
    val accentColor: AppAccentColor = AppAccentColor.FILC
)
