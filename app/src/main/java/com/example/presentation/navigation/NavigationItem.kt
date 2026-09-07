package com.example.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Grade
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A Filc navigáció: Kezdőlap, Órarend, Jegyek, Pénzügyek, Üzenetek.
 * A beállítások (profil) a fejléc avatárjából nyílik meg – pont úgy, ahogy a
 * reFilcben a `ProfileButton` viselkedik.
 */
enum class NavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME(
        title = "Kezdőlap",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    TIMETABLE(
        title = "Órarend",
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth
    ),
    GRADES(
        title = "Jegyek",
        selectedIcon = Icons.Filled.Grade,
        unselectedIcon = Icons.Outlined.Grade
    ),
    FINANCES(
        title = "Pénzügyek",
        selectedIcon = Icons.Filled.AccountBalanceWallet,
        unselectedIcon = Icons.Outlined.AccountBalanceWallet
    ),
    MESSAGES(
        title = "Üzenetek",
        selectedIcon = Icons.Filled.Mail,
        unselectedIcon = Icons.Outlined.Mail
    );

    companion object {
        /** A legördülő sávban megjelenő tabok (a beállítások kivételével). */
        val bottomItems: List<NavigationItem> = listOf(
            HOME,
            TIMETABLE,
            GRADES,
            FINANCES,
            MESSAGES
        )
    }
}
