package com.example.presentation.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.NeptunMessage
import com.example.domain.usecase.StudyProfile
import com.example.presentation.navigation.NavigationItem
import com.example.presentation.ui.components.AveragePill
import com.example.presentation.ui.components.FilcAvatar
import com.example.presentation.ui.components.FilcCard
import com.example.presentation.ui.components.FilcChip
import com.example.presentation.ui.components.FilcDivider
import com.example.presentation.ui.components.FilcDot
import com.example.presentation.ui.components.FilcEmptyState
import com.example.presentation.ui.components.FilcIconBadge
import com.example.presentation.ui.components.FilcKeyValue
import com.example.presentation.ui.components.FilcLessonTile
import com.example.presentation.ui.components.FilcPanel
import com.example.presentation.ui.components.FilcProgressBar
import com.example.presentation.ui.components.FilcStatTile
import com.example.presentation.ui.components.FilcTopBar
import com.example.presentation.ui.components.GradeBadge
import com.example.presentation.ui.components.TrendDisplay
import com.example.presentation.ui.components.filcCard
import com.example.presentation.ui.components.formatHungarian
import com.example.presentation.viewmodel.HomeUiState
import com.example.presentation.viewmodel.LiveCardMode
import com.example.ui.theme.filcColors

/**
 * Kezdőlap – a Filc "home" képernyője: üdvözlő fejléc, élő kártya (aktuális /
 * következő óra), gyors statisztikák, mai órák, legfrissebb jegyek, üzenetek
 * és pénzügyek, alul a tanulmányi profillal.
 */
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onNavigate: (NavigationItem) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenMessage: (NeptunMessage) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filc = filcColors()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(filc.background)
    ) {
        FilcTopBar(
            greeting = "${uiState.greeting}, ${uiState.firstName}!",
            dateLine = uiState.todayLabel,
            avatarName = uiState.fullName.ifBlank { uiState.firstName },
            onAvatarClick = onOpenProfile,
            onRefresh = onRefresh,
            isRefreshing = uiState.isRefreshing,
            onMessagesClick = { onNavigate(NavigationItem.MESSAGES) },
            unreadCount = uiState.unreadCount
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                FilcLiveCard(
                    mode = uiState.liveCardMode,
                    title = uiState.liveCardTitle,
                    subtitle = uiState.liveCardSubtitle,
                    progress = uiState.liveCardProgress,
                    countdown = uiState.liveCardCountdown,
                    lessonCount = uiState.todayLessons.size,
                    onClick = { onNavigate(NavigationItem.TIMETABLE) },
                    modifier = Modifier.testTag("home_live_card")
                )
            }

            item {
                FilcQuickStats(
                    average = uiState.weightedAverage,
                    averageDelta = uiState.averageDelta,
                    credits = uiState.earnedCredits,
                    unread = uiState.unreadCount,
                    onGradesClick = { onNavigate(NavigationItem.GRADES) },
                    onMessagesClick = { onNavigate(NavigationItem.MESSAGES) },
                    modifier = Modifier.testTag("home_quick_stats")
                )
            }

            item {
                FilcPanel(
                    title = "Mai órák",
                    titleTrailing = {
                        Text(
                            text = "Órarend",
                            style = MaterialTheme.typography.labelMedium,
                            color = filc.accent,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(45.dp))
                                .clickable(onClick = { onNavigate(NavigationItem.TIMETABLE) })
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                ) {
                    if (uiState.todayLessons.isEmpty()) {
                        FilcEmptyState(
                            icon = Icons.Default.EventBusy,
                            title = "Ma nincs órád",
                            description = "Pihenj egyet, vagy nézd meg a hét többi napját.",
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    } else {
                        uiState.todayLessons.forEach { event ->
                            FilcLessonTile(event = event, compact = true)
                        }
                    }
                }
            }

            item {
                if (uiState.recentGrades.isEmpty()) {
                    FilcCard {
                        FilcEmptyState(
                            icon = Icons.Default.School,
                            title = "Még nem érkezett jegy",
                            description = "Ha a Neptunban osztanak jegyet, itt azonnal megjelenik.",
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                } else {
                    FilcPanel(
                        title = "Legutóbbi jegyek",
                        titleTrailing = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Jegyek",
                                tint = filc.textMuted,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = { onNavigate(NavigationItem.GRADES) })
                            )
                        }
                    ) {
                        uiState.recentGrades.forEach { grade ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(onClick = { onNavigate(NavigationItem.GRADES) })
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                GradeBadge(
                                    grade = grade.effectiveGrade,
                                    isGhost = grade.ghostGrade != null,
                                    size = 32.dp,
                                    text = if (grade.effectiveGrade == null) {
                                        grade.gradeText.take(1)
                                    } else {
                                        null
                                    }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = grade.subjectName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = filc.text,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${grade.credit} kredit · ${grade.termName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = filc.textMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                FilcPanel(
                    title = "Beérkezett üzenetek",
                    titleTrailing = {
                        Text(
                            text = if (uiState.unreadCount > 0) "${uiState.unreadCount} új" else "Mind",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (uiState.unreadCount > 0) filc.accent else filc.textMuted,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(45.dp))
                                .clickable(onClick = { onNavigate(NavigationItem.MESSAGES) })
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                ) {
                    if (uiState.latestMessages.isEmpty()) {
                        FilcEmptyState(
                            icon = Icons.Default.Inbox,
                            title = "Nincs új üzenet",
                            description = "A Neptun postafiókod üres.",
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    } else {
                        uiState.latestMessages.forEach { message ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(onClick = { onOpenMessage(message) })
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilcDot(
                                    color = if (message.isRead) filc.text.copy(alpha = 0.12f) else filc.accent,
                                    size = 8.dp,
                                    modifier = Modifier.padding(end = 10.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = message.subject.ifBlank { "(tárgy nélkül)" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (message.isRead) FontWeight.Medium else FontWeight.Bold,
                                        color = filc.text,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${message.sender} · ${message.sendDate}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = filc.textMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                FilcCard(onClick = { onNavigate(NavigationItem.FINANCES) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilcIconBadge(
                            icon = Icons.Default.AccountBalanceWallet,
                            color = if (uiState.pendingAmountHuf > 0) filc.orange else filc.green
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (uiState.pendingAmountHuf > 0) {
                                    "Fizetendő: ${formatHuf(uiState.pendingAmountHuf)}"
                                } else {
                                    "Nincs függő befizetésed"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = filc.text
                            )
                            Text(
                                text = uiState.nextDueItem?.let {
                                    "Határidő: ${it.dueDate} · ${it.title}"
                                } ?: "Minden rendben, nincs lejáró számlád.",
                                style = MaterialTheme.typography.bodySmall,
                                color = filc.textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (uiState.overdueAmountHuf > 0) {
                            FilcChip(
                                text = "késésben",
                                color = filc.red,
                                background = filc.red.copy(alpha = 0.16f)
                            )
                        }
                    }
                }
            }

            item {
                FilcPersonalityCard(profile = uiState.profile)
            }
        }
    }
}

/** Magyar formátumú összeg, pl. "128 000 Ft". */
internal fun formatHuf(amount: Int): String =
    java.text.NumberFormat.getNumberInstance(java.util.Locale("hu", "HU"))
        .format(amount.toLong()) + " Ft"

/**
 * Az "élő kártya" – a Kezdőlap ikonikus eleme: az aktuális vagy a következő
 * óra, haladássávval és visszaszámlálóval.
 */
@Composable
private fun FilcLiveCard(
    mode: LiveCardMode,
    title: String,
    subtitle: String,
    progress: Float,
    countdown: String,
    lessonCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filc = filcColors()
    val accentForMode: Color = when (mode) {
        LiveCardMode.IN_CLASS -> filc.accent
        LiveCardMode.SOON -> filc.yellow
        LiveCardMode.LATER_TODAY -> filc.blue
        LiveCardMode.DONE_TODAY -> filc.green
        LiveCardMode.NO_SCHOOL -> filc.purple
    }
    val cardBackground by animateColorAsState(
        targetValue = if (filc.isLight) {
            lerp(accentForMode, Color.White, 0.86f)
        } else {
            lerp(accentForMode, Color.Black, 0.84f)
        },
        animationSpec = tween(320),
        label = "live_card_bg"
    )
    val animatedProgress by animateFloatAsState(
        targetValue = if (progress.isNaN()) 0f else progress,
        animationSpec = tween(600),
        label = "live_card_progress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBackground)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(accentForMode.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                FilcDot(
                    color = accentForMode,
                    size = if (mode == LiveCardMode.IN_CLASS) 12.dp else 9.dp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = filc.text,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = filc.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (countdown.isNotBlank()) {
                Text(
                    text = countdown,
                    style = MaterialTheme.typography.titleSmall,
                    color = filc.text,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }

        if (mode == LiveCardMode.IN_CLASS) {
            FilcProgressBar(
                progress = animatedProgress,
                color = accentForMode,
                trackColor = filc.text.copy(alpha = 0.10f),
                barHeight = 7.dp
            )
        } else if (lessonCount > 0) {
            Text(
                text = "Ma még $lessonCount óra szerepel a listádon.",
                style = MaterialTheme.typography.labelMedium,
                color = filc.textMuted
            )
        }
    }
}

/** Gyors statisztikák: átlag (trenddel), kredit, olvasatlan üzenetek. */
@Composable
private fun FilcQuickStats(
    average: Double,
    averageDelta: Double,
    credits: Int,
    unread: Int,
    onGradesClick: () -> Unit,
    onMessagesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filc = filcColors()
    FilcCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 6.dp, end = 6.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onGradesClick)
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Tanulmányi átlag",
                    style = MaterialTheme.typography.labelSmall,
                    color = filc.textMuted,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(5.dp))
                AveragePill(average = average, scale = 0.95f)
                if (averageDelta != 0.0) {
                    Spacer(modifier = Modifier.height(5.dp))
                    TrendDisplay(delta = averageDelta)
                }
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(58.dp)
                    .background(filc.hairline)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onGradesClick)
                    .padding(horizontal = 8.dp)
            ) {
                FilcStatTile(label = "Kredit", value = "$credits", caption = "szerzett eddig")
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(58.dp)
                    .background(filc.hairline)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onMessagesClick)
                    .padding(horizontal = 8.dp)
            ) {
                FilcStatTile(
                    label = "Üzenet",
                    value = "$unread",
                    caption = "olvasatlan",
                    valueColor = if (unread > 0) filc.accent else filc.text
                )
            }
        }
    }
}

/**
 * "Tanulmányi profilom" – reFilc-féle személyiségkártya. A tartalom csak a
 * gomb megnyomására lepleződik le, ahogy az eredetiben a hosszú nyomás.
 */
@Composable
private fun FilcPersonalityCard(
    profile: StudyProfile,
    modifier: Modifier = Modifier
) {
    val filc = filcColors()
    var revealed by remember { mutableStateOf(false) }
    val gradient = Brush.linearGradient(
        listOf(
            filc.accent.copy(alpha = 0.30f),
            filc.purple.copy(alpha = 0.12f),
            Color.Transparent
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .filcCard(shape = RoundedCornerShape(20.dp))
            .background(gradient)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = filc.accent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Tanulmányi profilom",
                style = MaterialTheme.typography.titleMedium,
                color = filc.text.copy(alpha = 0.75f),
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(45.dp))
                    .background(filc.text.copy(alpha = 0.06f))
                    .clickable(onClick = { revealed = !revealed })
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("personality_toggle"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (revealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (revealed) "Elrejtés" else "Megjelenítés",
                    tint = filc.textMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        if (revealed) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = profile.emoji, fontSize = 34.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = filc.text
                    )
                    Text(
                        text = profile.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = filc.textSecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            FilcDivider()
            FilcKeyValue(label = "Kedvenc tantárgy", value = profile.bestSubject.ifBlank { "-" })
            FilcKeyValue(
                label = "Legjobb tárgyátlag",
                value = if (profile.bestSubjectAverage > 0) formatHungarian(profile.bestSubjectAverage) else "-",
                valueColor = filc.gradeColor(5)
            )
            FilcKeyValue(
                label = "Legnehezebb tárgy",
                value = profile.hardestSubject.ifBlank { "-" }
            )
            FilcKeyValue(
                label = "Szerzett kredit",
                value = "${profile.creditsEarned} / ${profile.creditsPlanned}"
            )
            FilcKeyValue(
                label = "Legjobb sorozat",
                value = "${profile.goodGradeStreak} jó jegy"
            )
            FilcProgressBar(
                progress = profile.creditProgress,
                color = filc.accent,
                barHeight = 6.dp,
                modifier = Modifier.padding(top = 4.dp)
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilcAvatar(name = profile.title, size = 38.dp, background = filc.accent)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Koppints a gombra – meglátod, milyen hallgató vagy valójában.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = filc.textMuted,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
