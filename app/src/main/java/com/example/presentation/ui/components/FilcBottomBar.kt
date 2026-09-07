package com.example.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.navigation.NavigationItem
import com.example.ui.theme.filcColors

/**
 * Filc navigációs sáv: 76 dp magas, sárga pilula-indikátor az aktív ikon
 * mögött, minden tabnál látható felirattal – a reFilc `NavigationBarTheme`
 * (`height: 76.0`, `indicatorColor: accent.withValues(alpha: .8)`) mása.
 */
@Composable
fun FilcBottomBar(
    currentDestination: NavigationItem,
    unreadMessageCount: Int,
    onNavigate: (NavigationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val filc = filcColors()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(filc.backgroundDeep)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .height(76.dp)
            .testTag("neptun_bottom_navigation"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        NavigationItem.bottomItems.forEach { item ->
            val selected = currentDestination == item
            Box(modifier = Modifier.weight(1f)) {
                FilcNavItem(
                    item = item,
                    selected = selected,
                    badge = if (item == NavigationItem.MESSAGES) unreadMessageCount else 0,
                    onClick = { onNavigate(item) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("nav_item_${item.name.lowercase()}")
                )
            }
        }
    }
}

@Composable
private fun FilcNavItem(
    item: NavigationItem,
    selected: Boolean,
    badge: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filc = filcColors()
    val indicatorColor by animateColorAsState(
        targetValue = if (selected) filc.accent.copy(alpha = 0.85f) else filc.accent.copy(alpha = 0f),
        animationSpec = tween(220),
        label = "nav_indicator"
    )
    val contentColor = if (selected) filc.onAccent else filc.text.copy(alpha = 0.75f)
    val iconPad by animateDpAsState(
        targetValue = if (selected) 6.dp else 8.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "nav_icon_pad"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(45.dp))
            .background(indicatorColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = iconPad),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.title,
                tint = contentColor,
                modifier = Modifier
                    .size(21.dp)
                    .padding(top = 1.dp)
            )
            if (badge > 0) {
                Box(
                    modifier = Modifier
                        .size(15.dp)
                        .clip(CircleShape)
                        .background(filc.red),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (badge > 9) "9+" else badge.toString(),
                        color = Color.White,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Text(
            text = item.title,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
