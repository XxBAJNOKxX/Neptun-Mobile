package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class ThemeMode(val title: String, val description: String) {
    SYSTEM("Rendszer", "Követi a telefon beállításait"),
    LIGHT("Világos", "Állandó világos megjelenés"),
    DARK("Sötét", "Kíméli a szemet sötétben")
}

enum class AppAccentColor(
    val id: String,
    val title: String,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val lightColorScheme: ColorScheme,
    val darkColorScheme: ColorScheme
) {
    BLUE(
        id = "blue",
        title = "Neptun Kék",
        primary = Color(0xFF1E40AF),
        secondary = Color(0xFF3B82F6),
        tertiary = Color(0xFF0284C7),
        lightColorScheme = lightColorScheme(
            primary = Color(0xFF1E40AF),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFDBEAFE),
            onPrimaryContainer = Color(0xFF1E3A8A),
            secondary = Color(0xFF2563EB),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFEFF6FF),
            onSecondaryContainer = Color(0xFF1E40AF),
            tertiary = Color(0xFF0284C7),
            surfaceVariant = Color(0xFFF1F5F9),
            onSurfaceVariant = Color(0xFF475569)
        ),
        darkColorScheme = darkColorScheme(
            primary = Color(0xFF93C5FD),
            onPrimary = Color(0xFF1E3A8A),
            primaryContainer = Color(0xFF1E40AF),
            onPrimaryContainer = Color(0xFFDBEAFE),
            secondary = Color(0xFF60A5FA),
            onSecondary = Color(0xFF172554),
            secondaryContainer = Color(0xFF1E3A8A),
            onSecondaryContainer = Color(0xFFBFDBFE),
            tertiary = Color(0xFF67E8F9),
            surfaceVariant = Color(0xFF1E293B),
            onSurfaceVariant = Color(0xFFCBD5E1)
        )
    ),
    INDIGO(
        id = "indigo",
        title = "Zafír Indigó",
        primary = Color(0xFF4338CA),
        secondary = Color(0xFF6366F1),
        tertiary = Color(0xFF818CF8),
        lightColorScheme = lightColorScheme(
            primary = Color(0xFF4338CA),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFE0E7FF),
            onPrimaryContainer = Color(0xFF312E81),
            secondary = Color(0xFF4F46E5),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFEEF2FF),
            onSecondaryContainer = Color(0xFF3730A3),
            tertiary = Color(0xFF6366F1),
            surfaceVariant = Color(0xFFF3F4F6),
            onSurfaceVariant = Color(0xFF4B5563)
        ),
        darkColorScheme = darkColorScheme(
            primary = Color(0xFFA5B4FC),
            onPrimary = Color(0xFF312E81),
            primaryContainer = Color(0xFF3730A3),
            onPrimaryContainer = Color(0xFFE0E7FF),
            secondary = Color(0xFF818CF8),
            onSecondary = Color(0xFF1E1B4B),
            secondaryContainer = Color(0xFF4338CA),
            onSecondaryContainer = Color(0xFFC7D2FE),
            tertiary = Color(0xFFC4B5FD),
            surfaceVariant = Color(0xFF1F2937),
            onSurfaceVariant = Color(0xFFD1D5DB)
        )
    ),
    CYAN(
        id = "cyan",
        title = "Óceán Cián",
        primary = Color(0xFF0284C7),
        secondary = Color(0xFF0EA5E9),
        tertiary = Color(0xFF06B6D4),
        lightColorScheme = lightColorScheme(
            primary = Color(0xFF0284C7),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFE0F2FE),
            onPrimaryContainer = Color(0xFF075985),
            secondary = Color(0xFF0284C7),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFF0F9FF),
            onSecondaryContainer = Color(0xFF0369A1),
            tertiary = Color(0xFF0D9488),
            surfaceVariant = Color(0xFFF0FDF4),
            onSurfaceVariant = Color(0xFF334155)
        ),
        darkColorScheme = darkColorScheme(
            primary = Color(0xFF7DD3FC),
            onPrimary = Color(0xFF082F49),
            primaryContainer = Color(0xFF0369A1),
            onPrimaryContainer = Color(0xFFE0F2FE),
            secondary = Color(0xFF38BDF8),
            onSecondary = Color(0xFF082F49),
            secondaryContainer = Color(0xFF0284C7),
            onSecondaryContainer = Color(0xFFBAE6FD),
            tertiary = Color(0xFF5EEAD4),
            surfaceVariant = Color(0xFF0F172A),
            onSurfaceVariant = Color(0xFF94A3B8)
        )
    ),
    EMERALD(
        id = "emerald",
        title = "Smaragd Zöld",
        primary = Color(0xFF059669),
        secondary = Color(0xFF10B981),
        tertiary = Color(0xFF14B8A6),
        lightColorScheme = lightColorScheme(
            primary = Color(0xFF059669),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFD1FAE5),
            onPrimaryContainer = Color(0xFF064E3B),
            secondary = Color(0xFF10B981),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFECFDF5),
            onSecondaryContainer = Color(0xFF065F46),
            tertiary = Color(0xFF0D9488),
            surfaceVariant = Color(0xFFF1F5F9),
            onSurfaceVariant = Color(0xFF334155)
        ),
        darkColorScheme = darkColorScheme(
            primary = Color(0xFF6EE7B7),
            onPrimary = Color(0xFF064E3B),
            primaryContainer = Color(0xFF065F46),
            onPrimaryContainer = Color(0xFFD1FAE5),
            secondary = Color(0xFF34D399),
            onSecondary = Color(0xFF022C22),
            secondaryContainer = Color(0xFF059669),
            onSecondaryContainer = Color(0xFFA7F3D0),
            tertiary = Color(0xFF5EEAD4),
            surfaceVariant = Color(0xFF111827),
            onSurfaceVariant = Color(0xFF9CA3AF)
        )
    ),
    GOLD(
        id = "gold",
        title = "Borostyán Arany",
        primary = Color(0xFFD97706),
        secondary = Color(0xFFF59E0B),
        tertiary = Color(0xFFEAB308),
        lightColorScheme = lightColorScheme(
            primary = Color(0xFFD97706),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFEF3C7),
            onPrimaryContainer = Color(0xFF78350F),
            secondary = Color(0xFFB45309),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFFFFBEB),
            onSecondaryContainer = Color(0xFF92400E),
            tertiary = Color(0xFFCA8A04),
            surfaceVariant = Color(0xFFFAF5FF),
            onSurfaceVariant = Color(0xFF4A044E)
        ),
        darkColorScheme = darkColorScheme(
            primary = Color(0xFFFCD34D),
            onPrimary = Color(0xFF78350F),
            primaryContainer = Color(0xFF92400E),
            onPrimaryContainer = Color(0xFFFEF3C7),
            secondary = Color(0xFFFBBF24),
            onSecondary = Color(0xFF451A03),
            secondaryContainer = Color(0xFFB45309),
            onSecondaryContainer = Color(0xFFFDE68A),
            tertiary = Color(0xFFFDE047),
            surfaceVariant = Color(0xFF1C1917),
            onSurfaceVariant = Color(0xFFA8A29E)
        )
    ),
    PURPLE(
        id = "purple",
        title = "Királyi Lila",
        primary = Color(0xFF7C3AED),
        secondary = Color(0xFF8B5CF6),
        tertiary = Color(0xFFA855F7),
        lightColorScheme = lightColorScheme(
            primary = Color(0xFF7C3AED),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFEDE9FE),
            onPrimaryContainer = Color(0xFF4C1D95),
            secondary = Color(0xFF6D28D9),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFF5F3FF),
            onSecondaryContainer = Color(0xFF5B21B6),
            tertiary = Color(0xFF9333EA),
            surfaceVariant = Color(0xFFF8FAFC),
            onSurfaceVariant = Color(0xFF475569)
        ),
        darkColorScheme = darkColorScheme(
            primary = Color(0xFFC4B5FD),
            onPrimary = Color(0xFF4C1D95),
            primaryContainer = Color(0xFF5B21B6),
            onPrimaryContainer = Color(0xFFEDE9FE),
            secondary = Color(0xFFA78BFA),
            onSecondary = Color(0xFF2E1065),
            secondaryContainer = Color(0xFF6D28D9),
            onSecondaryContainer = Color(0xFFDDD6FE),
            tertiary = Color(0xFFD8B4FE),
            surfaceVariant = Color(0xFF18181B),
            onSurfaceVariant = Color(0xFFA1A1AA)
        )
    ),
    CRIMSON(
        id = "crimson",
        title = "Rubin Vörös",
        primary = Color(0xFFDC2626),
        secondary = Color(0xFFEF4444),
        tertiary = Color(0xFFF43F5E),
        lightColorScheme = lightColorScheme(
            primary = Color(0xFFDC2626),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFEE2E2),
            onPrimaryContainer = Color(0xFF7F1D1D),
            secondary = Color(0xFFB91C1C),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFFEF2F2),
            onSecondaryContainer = Color(0xFF991B1B),
            tertiary = Color(0xFFE11D48),
            surfaceVariant = Color(0xFFF1F5F9),
            onSurfaceVariant = Color(0xFF334155)
        ),
        darkColorScheme = darkColorScheme(
            primary = Color(0xFFFCA5A5),
            onPrimary = Color(0xFF7F1D1D),
            primaryContainer = Color(0xFF991B1B),
            onPrimaryContainer = Color(0xFFFEE2E2),
            secondary = Color(0xFFF87171),
            onSecondary = Color(0xFF450A0A),
            secondaryContainer = Color(0xFFB91C1C),
            onSecondaryContainer = Color(0xFFFECACA),
            tertiary = Color(0xFFFDA4AF),
            surfaceVariant = Color(0xFF1E293B),
            onSurfaceVariant = Color(0xFFCBD5E1)
        )
    ),
    ROSE(
        id = "rose",
        title = "Rózsa Korall",
        primary = Color(0xFFE11D48),
        secondary = Color(0xFFF43F5E),
        tertiary = Color(0xFFFB7185),
        lightColorScheme = lightColorScheme(
            primary = Color(0xFFE11D48),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFFE4E6),
            onPrimaryContainer = Color(0xFF881337),
            secondary = Color(0xFFBE123C),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFFFF1F2),
            onSecondaryContainer = Color(0xFF9F1239),
            tertiary = Color(0xFFDB2777),
            surfaceVariant = Color(0xFFFDF4FF),
            onSurfaceVariant = Color(0xFF701A75)
        ),
        darkColorScheme = darkColorScheme(
            primary = Color(0xFFFDA4AF),
            onPrimary = Color(0xFF881337),
            primaryContainer = Color(0xFF9F1239),
            onPrimaryContainer = Color(0xFFFFE4E6),
            secondary = Color(0xFFFB7185),
            onSecondary = Color(0xFF4C0519),
            secondaryContainer = Color(0xFFBE123C),
            onSecondaryContainer = Color(0xFFFECDD3),
            tertiary = Color(0xFFF472B6),
            surfaceVariant = Color(0xFF1F2937),
            onSurfaceVariant = Color(0xFFD1D5DB)
        )
    );

    companion object {
        fun fromId(id: String): AppAccentColor {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: BLUE
        }
    }
}

data class ThemeSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColor: Boolean = true,
    val accentColor: AppAccentColor = AppAccentColor.BLUE
)
