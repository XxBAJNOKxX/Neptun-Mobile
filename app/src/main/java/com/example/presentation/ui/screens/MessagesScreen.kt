package com.example.presentation.ui.screens

import android.text.Html
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.foundation.text.selection.SelectionContainer
import com.example.presentation.ui.util.HtmlBlock
import com.example.presentation.ui.util.HtmlTableView
import com.example.presentation.ui.util.parseHtmlBlocks
import com.example.presentation.ui.util.rememberHtmlAnnotatedString
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.example.core.i18n.AppStrings
import com.example.core.i18n.currentStrings
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.currentStrings
import com.example.domain.model.NeptunMessage
import com.example.presentation.ui.components.NeptunTopBar
import com.example.presentation.ui.util.rememberHtmlAnnotatedString
import com.example.presentation.viewmodel.MessagesUiState
import com.example.ui.theme.NeptunBlue40

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    uiState: MessagesUiState,
    onToggleUnreadFilter: () -> Unit,
    onSearchQueryChange: (String) -> Unit = {},
    onOpenMessage: (NeptunMessage) -> Unit,
    onCloseMessage: () -> Unit,
    onReloadMessage: () -> Unit = {},
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = currentStrings()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NeptunTopBar(
            title = strings.messagesTitle,
            subtitle = if (uiState.unreadCount > 0) strings.unreadMessagesCount(uiState.unreadCount)
                       else (if (strings.languageCode == "hu") "Minden üzenet elolvasva" else if (strings.languageCode == "de") "Alle Nachrichten gelesen" else "All messages read"),
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh
        )

        // Filter chips bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (uiState.showUnreadOnly) strings.unreadOnly
                       else (if (strings.languageCode == "hu") "Összes üzenet (${uiState.messages.size})" else if (strings.languageCode == "de") "Alle Nachrichten (${uiState.messages.size})" else "All Messages (${uiState.messages.size})"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            FilterChip(
                selected = uiState.showUnreadOnly,
                onClick = onToggleUnreadFilter,
                label = { Text(strings.unreadOnly) },
                leadingIcon = {
                    Icon(
                        imageVector = if (uiState.showUnreadOnly) Icons.Default.MarkEmailRead else Icons.Default.FilterList,
                        contentDescription = strings.unreadOnly,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.testTag("messages_unread_filter_chip")
            )
        }

        // Kereső mező
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            placeholder = { Text(strings.searchMessages) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = strings.search) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
        if (uiState.filteredMessages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = strings.noMessages,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (uiState.showUnreadOnly) strings.noUnreadMessages else strings.noMessagesInInbox,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                items(uiState.filteredMessages) { msg ->
                    MessageCard(
                        message = msg,
                        onClick = { onOpenMessage(msg) }
                    )
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
        }
    }

    // Message Detail Bottom Sheet
    if (uiState.selectedMessage != null) {
        ModalBottomSheet(
            onDismissRequest = onCloseMessage,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            MessageDetailContent(
                message = uiState.selectedMessage,
                isLoading = uiState.isLoadingContent,
                onClose = onCloseMessage,
                onReload = onReloadMessage
            )
        }
    }
}

private fun getDisplaySender(sender: String, strings: AppStrings): String {
    val trimmed = sender.trim()
    if (trimmed.isEmpty() ||
        trimmed.equals("Rendszerüzenet", ignoreCase = true) ||
        trimmed.equals("System message", ignoreCase = true) ||
        trimmed.equals("Systemnachricht", ignoreCase = true)
    ) {
        return strings.systemMessage
    }
    return trimmed
}

private fun getDisplayPreviewText(previewText: String, strings: AppStrings): String {
    val trimmed = previewText.trim()
    if (trimmed.isEmpty() ||
        trimmed.startsWith("Koppints a teljes üzenet") ||
        trimmed.startsWith("Tap to view") ||
        trimmed.startsWith("Tippen Sie")
    ) {
        return strings.tapToViewFullMessage
    }
    return trimmed
}

@Composable
private fun MessageCard(
    message: NeptunMessage,
    onClick: () -> Unit
) {
    val strings = currentStrings()
    val displaySender = getDisplaySender(message.sender, strings)
    val isSystem = message.isOfficial || displaySender.equals(strings.systemMessage, ignoreCase = true) || displaySender.contains("hivatal", ignoreCase = true)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!message.isRead) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            if (!message.isRead) 1.5.dp else 1.dp,
            if (!message.isRead) MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("message_card_${message.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (!message.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = displaySender,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (!message.isRead) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (isSystem) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = message.sendDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message.subject,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (!message.isRead) FontWeight.Bold else FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = getDisplayPreviewText(message.previewText, strings),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun MessageDetailContent(
    message: NeptunMessage,
    isLoading: Boolean,
    onClose: () -> Unit,
    onReload: () -> Unit
) {
    val strings = currentStrings()
    val displaySender = getDisplaySender(message.sender, strings)
    val isSystem = message.isOfficial || displaySender.equals(strings.systemMessage, ignoreCase = true) || displaySender.contains("hivatal", ignoreCase = true)
    
    val rawBody = message.bodyHtml.ifBlank { message.previewText }
    val htmlBlocks = remember(rawBody) { parseHtmlBlocks(rawBody) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (isSystem) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                val tagText = if (displaySender.equals(strings.systemMessage, ignoreCase = true)) {
                    strings.systemMessage
                } else if (message.isOfficial) {
                    strings.officialNotice
                } else {
                    strings.officialMessage
                }
                Text(
                    text = tagText,
                    color = if (isSystem) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = strings.close)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = message.subject,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Sender & Date info box
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isSystem) Icons.Default.Info else Icons.Default.Person,
                        contentDescription = null,
                        tint = if (isSystem) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${strings.sender}: $displaySender",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${strings.date}: ${message.sendDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = strings.downloadingMessageContent,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (htmlBlocks.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                for (block in htmlBlocks) {
                    when (block) {
                        is HtmlBlock.Text -> {
                            val annotated = rememberHtmlAnnotatedString(
                                htmlString = block.htmlText,
                                linkColor = MaterialTheme.colorScheme.primary
                            )
                            if (annotated.text.isNotBlank() &&
                                !annotated.text.startsWith("Koppints a teljes üzenet") &&
                                !annotated.text.startsWith("Tap to view") &&
                                !annotated.text.startsWith("Tippen Sie") &&
                                !annotated.text.startsWith(strings.tapToViewFullMessage)
                            ) {
                                SelectionContainer {
                                    Text(
                                        text = annotated,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 22.sp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        is HtmlBlock.Table -> {
                            SelectionContainer {
                                HtmlTableView(table = block)
                            }
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val emptyContentText = when (strings.languageCode) {
                    "de" -> "Der Inhalt der Nachricht ist leer oder konnte nicht von Neptun geladen werden."
                    "en" -> "The message content is empty or failed to load from Neptun."
                    else -> "A levél tartalma üres vagy nem sikerült közvetlenül betölteni a Neptunból."
                }
                Text(
                    text = emptyContentText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                FilledTonalButton(onClick = onReload) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.retry)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
