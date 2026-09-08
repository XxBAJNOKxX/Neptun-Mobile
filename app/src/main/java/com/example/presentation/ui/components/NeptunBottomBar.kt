package com.example.presentation.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.presentation.navigation.NavigationItem

@Composable
fun NeptunBottomBar(
    currentDestination: NavigationItem,
    items: List<NavigationItem> = NavigationItem.entries,
    unreadMessageCount: Int,
    onNavigate: (NavigationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("neptun_bottom_navigation")
    ) {
        items.forEach { item ->
            val selected = currentDestination == item

            NavigationBarItem(
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
                                contentDescription = item.title
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.title
                        )
                    }
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.testTag("nav_item_${item.name.lowercase()}")
            )
        }
    }
}
