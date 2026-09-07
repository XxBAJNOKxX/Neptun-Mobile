package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp

/** A Filc kártya- és gombsugarai: 12 dp gombok, 16 dp panelek, pilulák 45. */
val FilcShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/** Az aktuális, accentből levezetett paletta. */
val LocalFilcColors = staticCompositionLocalOf { FilcPalette.of(FilcLime, dark = false) }

/**
 * A Filc (reFilc) arculatot megvalósító Material3 téma.
 *
 * A dynamic color bekapcsolásakor a rendszerpaletta elsődleges színe válik accentté,
 * de a hátterek így is az accent tónusaiba csúsznak át – pont úgy, ahogy a
 * reFilc "adaptive" accentje csinálja.
 */
@Composable
fun MyApplicationTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    darkTheme: Boolean = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    },
    dynamicColor: Boolean = false,
    accentColor: AppAccentColor = AppAccentColor.FILC,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val paletteAccent = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val scheme = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        scheme.primary
    } else {
        accentColor.primary
    }

    val filcPalette = remember(paletteAccent, darkTheme) {
        FilcPalette.of(paletteAccent, darkTheme)
    }

    val baseScheme: ColorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else if (darkTheme) {
        accentColor.buildDarkScheme()
    } else {
        accentColor.buildLightScheme()
    }

    val colorScheme = baseScheme.copy(
        primary = filcPalette.accent,
        onPrimary = filcPalette.onAccent,
        primaryContainer = lerp(
            filcPalette.accent,
            if (darkTheme) Color.Black else Color.White,
            if (darkTheme) 0.55f else 0.72f
        ),
        onPrimaryContainer = if (darkTheme) {
            lerp(filcPalette.accent, Color.White, 0.85f)
        } else {
            lerp(filcPalette.accent, Color.Black, 0.72f)
        },
        secondary = filcPalette.accent,
        onSecondary = filcPalette.onAccent,
        tertiary = filcPalette.accent,
        onTertiary = filcPalette.onAccent,
        background = filcPalette.background,
        onBackground = filcPalette.text,
        surface = filcPalette.surface,
        onSurface = filcPalette.text,
        surfaceVariant = filcPalette.surface,
        onSurfaceVariant = filcPalette.textSecondary,
        outline = filcPalette.stroke,
        outlineVariant = filcPalette.hairline,
        error = filcPalette.red,
        onError = Color.White,
        scrim = if (darkTheme) Color(0x99000000) else Color(0x66000000)
    )

    CompositionLocalProvider(LocalFilcColors provides filcPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = FilcShapes,
            content = content
        )
    }
}

/** Rövidítés a panelekben: `val filc = filcColors()`. */
@Composable
fun filcColors(): FilcPalette = LocalFilcColors.current
