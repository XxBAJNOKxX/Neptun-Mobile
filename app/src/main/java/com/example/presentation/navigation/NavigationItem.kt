package com.example.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Grading
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Grading
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Person
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.R

enum class NavigationItem(
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME(
        labelRes = R.string.nav_home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    TIMETABLE(
        labelRes = R.string.nav_timetable,
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth
    ),
    GRADES(
        labelRes = R.string.nav_grades,
        selectedIcon = Icons.Filled.Grading,
        unselectedIcon = Icons.Outlined.Grading
    ),
    MESSAGES(
        labelRes = R.string.nav_messages,
        selectedIcon = Icons.Filled.Mail,
        unselectedIcon = Icons.Outlined.Mail
    ),
    FINANCES(
        labelRes = R.string.nav_finances,
        selectedIcon = Icons.Filled.AccountBalanceWallet,
        unselectedIcon = Icons.Outlined.AccountBalanceWallet
    ),
    SETTINGS(
        labelRes = R.string.nav_profile,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    );

    companion object {
        fun fromName(name: String?): NavigationItem {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: HOME
        }
    }
}
