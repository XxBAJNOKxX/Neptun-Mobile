package com.example.presentation.ui.screens

import android.text.Html
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.NeptunMessage
import com.example.presentation.ui.components.FilcChip
import com.example.presentation.ui.components.FilcEmptyState
import com.example.presentation.ui.components.FilcFilterBar
import com.example.presentation.ui.components.FilcIconButton
import com.example.presentation.ui.components.FilcPanel
import com.example.presentation.ui.components.UnreadDot
import com.example.presentation.ui.components.filcCard
import com.example.presentation.viewmodel.MessagesUiState
import com.example.ui.theme.filcColors

/**
 * Üzenetek – Filc stílus. A lista fölé pilula-szűrő kerül (Mind / Olvasatlan),
 * a kiválasztott levél pedig jobbról becsúszó, oldalnyi kártyán jelenik meg,
 * mint a reFilc `MessageViewable`.
 */
@Composable
fun MessagesScreen(
    uiState: MessagesUiState,
    onToggleUnreadFilter: () -> Unit,
    onOpenMessage: (NeptunMessage) -> Unit,
    onCloseMessage: () -> Unit,
    onReloadMessage: () -> Unit = {},
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filc = filcColors()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(filc.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FilcScreenHeader(
                title = "Üzenetek",
                subtitle = if (uiState.unreadCount > 0) {
                    "${uiState.unreadCount} olvasatlan"
                } else {
                    "Mind elolvasva"
                },
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh
            )

            FilcFilterBar(
                options = listOf("Mind", "Olvasatlan"),
                selectedIndex = if (uiState.showUnreadOnly) 1 else 0,
                onSelect = { index ->
                    val wantUnreadOnly = index == 1
                    if (wantUnreadOnly != uiState.showUnreadOnly) onToggleUnreadFilter()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("messages_filter")
            )

            if (uiState.filteredMessages.isEmpty()) {
                FilcEmptyState(
                    icon = Icons.Default.Inbox,
                    title = if (uiState.showUnreadOnly) "Nincs olvasatlan üzenet" else "Nincs üzenet",
                    description = "A Neptun postafiókod jelenleg üres.",
                    modifier = Modifier.padding(top = 24.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.filteredMessages, key = { it.id }) { message ->
                        FilcMessageTile(
                            message = message,
                            onClick = { onOpenMessage(message) }
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = uiState.selectedMessage != null,
            enter = slideInHorizontally(tween(260)) { it / 3 } + androidx.compose.animation.fadeIn(tween(200)),
            exit = slideOutHorizontally(tween(220)) { it / 3 } + androidx.compose.animation.fadeOut(tween(160)),
            modifier = Modifier.fillMaxSize()
        ) {
            val message = uiState.selectedMessage
            if (message != null) {
                FilcMessageDetail(
                    message = message,
                    isLoadingContent = uiState.isLoadingContent,
                    onBack = onCloseMessage,
                    onReload = onReloadMessage
                )
            }
        }
    }
}

@Composable
private fun FilcMessageTile(
    message: NeptunMessage,
    onClick: () -> Unit
) {
    val filc = filcColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .filcCard(shape = RoundedCornerShape(16.dp), elevation = 10.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(
                    if (message.isRead) filc.text.copy(alpha = 0.05f) else filc.accent.copy(alpha = 0.20f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Article,
                contentDescription = null,
                tint = if (message.isRead) filc.textMuted else filc.accent,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = message.sender,
                    style = MaterialTheme.typography.labelSmall,
                    color = filc.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (message.isOfficial) {
                    FilcChip(
                        text = "hivatalos",
                        color = filc.blue,
                        background = filc.blue.copy(alpha = 0.14f),
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = message.sendDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = filc.textMuted,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message.subject.ifBlank { "(tárgy nélkül)" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (message.isRead) FontWeight.Medium else FontWeight.Bold,
                color = filc.text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (message.previewText.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = message.previewText,
                    style = MaterialTheme.typography.bodySmall,
                    color = filc.textMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }
        }
        if (!message.isRead) {
            Spacer(modifier = Modifier.width(8.dp))
            UnreadDot(modifier = Modifier.padding(top = 6.dp))
        }
    }
}

/** Oldalnyi levélnézet: fejléc, törzs, visszalépés. */
@Composable
private fun FilcMessageDetail(
    message: NeptunMessage,
    isLoadingContent: Boolean,
    onBack: () -> Unit,
    onReload: () -> Unit
) {
    val filc = filcColors()
    val body = remember(message.bodyHtml) {
        if (message.bodyHtml.isNotBlank()) {
            try {
                Html.fromHtml(message.bodyHtml, Html.FROM_HTML_MODE_COMPACT).toString().trim()
            } catch (e: Exception) {
                message.bodyHtml.trim()
            }
        } else {
            ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(filc.background)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(top = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilcIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                description = "Vissza",
                onClick = onBack
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Beérkezett üzenet",
                style = MaterialTheme.typography.titleMedium,
                color = filc.text.copy(alpha = 0.7f),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            FilcIconButton(icon = Icons.Default.Refresh, description = "Újratöltés", onClick = onReload)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 28.dp)
        ) {
            item {
                FilcPanel(contentPadding = PaddingValues(16.dp)) {
                    Text(
                        text = message.subject.ifBlank { "(tárgy nélkül)" },
                        style = MaterialTheme.typography.headlineSmall,
                        color = filc.text
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = message.sender,
                            style = MaterialTheme.typography.bodyMedium,
                            color = filc.textSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "·",
                            color = filc.textMuted
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = message.sendDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = filc.textMuted
                        )
                    }
                    if (message.isOfficial) {
                        Spacer(modifier = Modifier.height(10.dp))
                        FilcChip(
                            text = "Hivatalos értesítés",
                            color = filc.blue,
                            background = filc.blue.copy(alpha = 0.14f)
                        )
                    }
                }
            }
            item {
                FilcPanel(contentPadding = PaddingValues(16.dp)) {
                    if (isLoadingContent) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = filc.accent,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Törzs betöltése…",
                                style = MaterialTheme.typography.bodySmall,
                                color = filc.textMuted
                            )
                        }
                    }
                    Text(
                        text = body.ifBlank { "Az üzenet törzse üres." },
                        style = MaterialTheme.typography.bodyLarge,
                        color = filc.text,
                        lineHeight = 24.sp
                    )
                    if (body.isBlank() && !isLoadingContent) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Húzd frissítésre a tartalomért.",
                            style = MaterialTheme.typography.labelSmall,
                            color = filc.textMuted
                        )
                    }
                }
            }
        }
    }
}
