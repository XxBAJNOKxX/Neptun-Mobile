package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext

@Composable
fun MyApplicationTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    darkTheme: Boolean = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    },
    dynamicColor: Boolean = true,
    accentColor: AppAccentColor = AppAccentColor.BLUE,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val baseScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> accentColor.darkColorScheme
        else -> accentColor.lightColorScheme
    }

    // Sötét módban az árnyékok nem látszanak, így a "panelek" (kártyák, felső sáv)
    // eltűnnének. Ezért a hátteret sötétítjük és a felületeket finoman világosítjuk,
    // így a blokkok színkontraszttal elválnak.
    val colorScheme = if (darkTheme) {
        baseScheme.copy(
            background = lerp(baseScheme.background, Color.Black, 0.45f),
            surface = lerp(baseScheme.surface, Color.White, 0.055f),
            surfaceVariant = lerp(baseScheme.surfaceVariant, Color.White, 0.05f)
        )
    } else {
        baseScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

