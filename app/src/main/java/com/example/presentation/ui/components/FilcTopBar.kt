package com.example.presentation.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.filcColors

/** Forgós frissítés-gomb – a Filc fejlécek jobb szélén. */
@Composable
fun FilcRefreshButton(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 20.dp
) {
    val filc = filcColors()
    val rotation by if (isRefreshing) {
        val transition = rememberInfiniteTransition(label = "refresh")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
            label = "spin"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    Box(
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(filc.text.copy(alpha = 0.05f))
            .clickable(enabled = !isRefreshing, onClick = onRefresh)
            .testTag("topbar_refresh_button"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Frissítés",
            tint = filc.icon,
            modifier = Modifier
                .size(iconSize)
                .rotate(rotation)
        )
    }
}

/**
 * A reFilc nyitófejléce: üdvözlő szöveg + dátum balra, jobb oldalt üzenet,
 * frissítés és profilkép. A fejléc átlátszó, alatta a lap háttere látszik.
 */
@Composable
fun FilcTopBar(
    greeting: String,
    dateLine: String,
    avatarName: String,
    onAvatarClick: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    isRefreshing: Boolean = false,
    onMessagesClick: (() -> Unit)? = null,
    unreadCount: Int = 0,
    avatarBadge: Boolean = false
) {
    val filc = filcColors()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(filc.background)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(start = 20.dp, end = 16.dp, top = 8.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.titleLarge,
                color = filc.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = dateLine,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = filc.textMuted,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (onMessagesClick != null) {
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(filc.text.copy(alpha = 0.05f))
                    .clickable(onClick = onMessagesClick)
                    .testTag("topbar_messages_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Message,
                    contentDescription = "Üzenetek",
                    tint = filc.icon,
                    modifier = Modifier.size(20.dp)
                )
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(filc.red)
                    )
                }
            }
        }

        FilcRefreshButton(isRefreshing = isRefreshing, onRefresh = onRefresh)

        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .clip(CircleShape)
                .clickable(onClick = onAvatarClick)
                .testTag("topbar_profile_button")
        ) {
            FilcAvatar(name = avatarName, size = 38.dp, showBadge = avatarBadge)
        }
    }
}

/** Vissza gombos aloldal-fejléc (profil / beállítások). */
@Composable
fun FilcBackBar(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    val filc = filcColors()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(filc.background)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(start = 12.dp, end = 16.dp, top = 6.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(filc.text.copy(alpha = 0.05f))
                .clickable(onClick = onBack)
                .testTag("back_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Vissza",
                tint = filc.text,
                modifier = Modifier.size(19.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = filc.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = filc.textMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (action != null) action()
    }
}
