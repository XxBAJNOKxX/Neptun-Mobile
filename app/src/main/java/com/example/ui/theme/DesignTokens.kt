package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Központi tervezési tokenek a NeptunMobile felületéhez (spécifikus térközök, formák és állapot-színek).
 */
@Immutable
object NeptunSpacing {
    val none: Dp = 0.dp
    val extraSmall: Dp = 4.dp
    val small: Dp = 8.dp
    val medium: Dp = 12.dp
    val large: Dp = 16.dp
    val extraLarge: Dp = 24.dp
    val huge: Dp = 32.dp
}

@Immutable
object NeptunShapes {
    val extraSmall = RoundedCornerShape(4.dp)
    val small = RoundedCornerShape(8.dp)
    val medium = RoundedCornerShape(12.dp)
    val card = RoundedCornerShape(14.dp)
    val large = RoundedCornerShape(16.dp)
    val extraLarge = RoundedCornerShape(24.dp)
    val pill = RoundedCornerShape(50)
}

@Immutable
object NeptunElevation {
    val none: Dp = 0.dp
    val low: Dp = 1.dp
    val card: Dp = 2.dp
    val elevated: Dp = 4.dp
    val dialog: Dp = 6.dp
}

@Immutable
object NeptunStatusColors {
    val success = Color(0xFF10B981)
    val warning = Color(0xFFF59E0B)
    val error = Color(0xFFEF4444)
    val info = Color(0xFF3B82F6)
    val ghostGrade = Color(0xFF8B5CF6)

    // Jegy-specifikus színskála
    val grade5 = Color(0xFF10B981) // Jeles
    val grade4 = Color(0xFF3B82F6) // Jó
    val grade3 = Color(0xFFF59E0B) // Közepes
    val grade2 = Color(0xFFEA580C) // Elégséges
    val grade1 = Color(0xFFEF4444) // Elégtelen
}
