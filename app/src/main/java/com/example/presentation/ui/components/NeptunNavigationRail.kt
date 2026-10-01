package com.example.presentation.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.currentStrings
import com.example.presentation.navigation.NavigationItem

@Composable
fun NeptunNavigationRail(
    currentDestination: NavigationItem,
    items: List<NavigationItem> = NavigationItem.entries,
    unreadMessageCount: Int,
    onNavigate: (NavigationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = currentStrings()

    NavigationRail(
        modifier = modifier.testTag("neptun_navigation_rail"),
        header = {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.height(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    ) {
        items.forEach { item ->
            val selected = currentDestination == item
            val itemTitle = item.getLocalizedTitle(strings)

            NavigationRailItem(
                selected = selected,
                onClick = { onNavigate(item) },
                icon = {
                    if (item == NavigationItem.MESSAGES && unreadMessageCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge {
                                    Text(
                                        text = "$unreadMessageCount",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = itemTitle
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = itemTitle
                        )
                    }
                },
                label = {
                    Text(
                        text = itemTitle,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.testTag("nav_rail_item_${item.name.lowercase()}")
            )
        }
    }
}
