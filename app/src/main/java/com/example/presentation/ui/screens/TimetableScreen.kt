package com.example.presentation.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CalendarEvent
import com.example.presentation.ui.components.FilcBottomSheet
import com.example.presentation.ui.components.FilcChip
import com.example.presentation.ui.components.FilcEmptyState
import com.example.presentation.ui.components.FilcFilterBar
import com.example.presentation.ui.components.FilcKeyValue
import com.example.presentation.ui.components.FilcIconButton
import com.example.presentation.ui.components.FilcLessonTile
import com.example.presentation.ui.components.FilcPanel
import com.example.presentation.ui.components.FilcRefreshButton
import com.example.presentation.viewmodel.TimetableUiState
import com.example.presentation.viewmodel.WeekDayInfo
import com.example.ui.theme.filcColors
import kotlinx.coroutines.launch

/**
 * Órarend – Filc stílus: pilula napsáv, ízlészes hetesválasztó, órarend-sorok
 * időpont-guttával és színes sínttel. A napok között húzással lehet lépkedni,
 * a sorra koppintva részletes kártya nyílik alulról.
 */
@Composable
fun TimetableScreen(
    uiState: TimetableUiState,
    onDaySelect: (Int) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onCurrentWeek: () -> Unit,
    onToggleWeekView: () -> Unit,
    onRefresh: () -> Unit,
    onScheduleReminder: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val filc = filcColors()
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = (uiState.selectedDayOfWeek - 1).coerceIn(0, 4),
        pageCount = { 5 }
    )
    var detailEvent by remember { mutableStateOf<CalendarEvent?>(null) }

    LaunchedEffect(uiState.selectedDayOfWeek) {
        val target = (uiState.selectedDayOfWeek - 1).coerceIn(0, 4)
        if (pagerState.currentPage != target && !pagerState.isScrollInProgress) {
            pagerState.scrollToPage(target)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val day = pagerState.currentPage + 1
        if (day != uiState.selectedDayOfWeek && !uiState.isWeekView) {
            onDaySelect(day)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(filc.background)
    ) {
        FilcScreenHeader(
            title = "Órarend",
            subtitle = if (uiState.isWeekView) "Heti összesítés" else uiState.weekLabel,
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            actions = {
                FilcIconButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    description = "Előző hét",
                    onClick = onPreviousWeek,
                    modifier = Modifier.testTag("timetable_prev_week_button")
                )
                Spacer(modifier = Modifier.width(4.dp))
                FilcIconButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    description = "Következő hét",
                    onClick = onNextWeek,
                    modifier = Modifier.testTag("timetable_next_week_button")
                )
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilcFilterBar(
                options = uiState.weekDays.map { day ->
                    "${day.dayName.take(3)} · ${day.dateFormatted}"
                },
                selectedIndex = (uiState.selectedDayOfWeek - 1).coerceIn(0, 4),
                onSelect = { index ->
                    coroutineScope.launch { pagerState.animateScrollToPage(index) }
                    onDaySelect(index + 1)
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("timetable_day_filter")
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilcChip(
                text = if (uiState.selectedWeekOffset == 0) "ez a hét" else "hét ${uiState.selectedWeekOffset}",
                color = if (uiState.selectedWeekOffset == 0) filc.textMuted else filc.accent,
                background = if (uiState.selectedWeekOffset == 0) filc.text.copy(alpha = 0.06f) else filc.accent.copy(alpha = 0.16f),
                modifier = Modifier.clickable(onClick = onCurrentWeek)
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilcChip(
                text = if (uiState.isWeekView) "het nézet" else "napi nézet",
                color = filc.accent,
                background = filc.accent.copy(alpha = 0.12f),
                modifier = Modifier
                    .clickable(onClick = onToggleWeekView)
                    .testTag("timetable_toggle_week")
            )
        }

        if (uiState.isWeekView) {
            WeekTimetableList(
                uiState = uiState,
                onEventClick = { detailEvent = it }
            )
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(),
                beyondViewportPageCount = 1
            ) { page ->
                val dayNumber = page + 1
                val dayEvents = uiState.events
                    .filter { it.dayOfWeek == dayNumber && it.isActualAttendedClass }
                    .sortedWith(compareBy({ it.startHour }, { it.startMinute }))

                DayTimetableList(
                    day = uiState.weekDays.getOrNull(page),
                    events = dayEvents,
                    ongoingId = uiState.ongoingEvent?.id,
                    nextId = uiState.nextUpcomingEvent?.id,
                    onEventClick = { detailEvent = it }
                )
            }
        }
    }

    FilcLessonDetailSheet(
        event = detailEvent,
        reminderScheduled = detailEvent != null && uiState.notificationScheduledId == detailEvent?.id,
        onDismiss = { detailEvent = null },
        onScheduleReminder = { event -> onScheduleReminder(event) }
    )
}

@Composable
private fun DayTimetableList(
    day: WeekDayInfo?,
    events: List<CalendarEvent>,
    ongoingId: String?,
    nextId: String?,
    onEventClick: (CalendarEvent) -> Unit
) {
    val filc = filcColors()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = day?.let { "${it.dayName}, ${it.dateFormatted}" } ?: "Nap",
                    style = MaterialTheme.typography.titleMedium,
                    color = filc.text.copy(alpha = 0.65f)
                )
                if (day?.isToday == true) {
                    Spacer(modifier = Modifier.width(8.dp))
                    FilcChip(text = "ma", color = filc.accent, background = filc.accent.copy(alpha = 0.18f))
                }
            }
        }

        if (events.isEmpty()) {
            item {
                FilcPanel {
                    FilcEmptyState(
                        icon = Icons.Default.EventBusy,
                        title = "Erre a napra nincs óra",
                        description = "Az egyetemi naptár üres – pihenőnap."
                    )
                }
            }
        } else {
            items(events) { event ->
                FilcPanel(contentPadding = PaddingValues(6.dp)) {
                    FilcLessonTile(
                        event = event,
                        isOngoing = event.id == ongoingId,
                        isNext = event.id == nextId,
                        onClick = { onEventClick(event) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekTimetableList(
    uiState: TimetableUiState,
    onEventClick: (CalendarEvent) -> Unit
) {
    val filc = filcColors()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        uiState.weekDays.forEach { day ->
            val dayEvents = uiState.events
                .filter { it.dayOfWeek == day.dayOfWeek && it.isActualAttendedClass }
                .sortedWith(compareBy({ it.startHour }, { it.startMinute }))

            item {
                Row(
                    modifier = Modifier.padding(top = if (day.dayOfWeek == 1) 0.dp else 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = day.dayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = filc.text.copy(alpha = 0.65f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = day.dateFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = filc.textMuted
                    )
                    if (day.isToday) {
                        Spacer(modifier = Modifier.width(8.dp))
                        FilcChip(text = "ma", color = filc.accent, background = filc.accent.copy(alpha = 0.18f))
                    }
                }
            }

            if (dayEvents.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(filc.text.copy(alpha = 0.035f))
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Tanítás nélküli nap",
                            style = MaterialTheme.typography.bodySmall,
                            color = filc.textMuted
                        )
                    }
                }
            } else {
                item {
                    FilcPanel(contentPadding = PaddingValues(vertical = 4.dp)) {
                        dayEvents.forEach { event ->
                            FilcLessonTile(
                                event = event,
                                isOngoing = event.id == uiState.ongoingEvent?.id,
                                isNext = event.id == uiState.nextUpcomingEvent?.id,
                                compact = true,
                                onClick = { onEventClick(event) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Órarend-sor részletei – a Filc "bottom card" formában. */
@Composable
private fun FilcLessonDetailSheet(
    event: CalendarEvent?,
    reminderScheduled: Boolean,
    onDismiss: () -> Unit,
    onScheduleReminder: (CalendarEvent) -> Unit
) {
    val filc = filcColors()
    FilcBottomSheet(visible = event != null, onDismiss = onDismiss) {
        val shown = event ?: return@FilcBottomSheet
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(filc.accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = filc.accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = shown.subjectName,
                    style = MaterialTheme.typography.titleLarge,
                    color = filc.text,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${shown.courseCode} · ${shown.subjectCode}",
                    style = MaterialTheme.typography.labelSmall,
                    color = filc.textMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        FilcKeyValue(label = "Időpont", value = shown.timeFormatted)
        FilcKeyValue(
            label = "Helyszín",
            value = listOf(shown.room, shown.location).filter { it.isNotBlank() }.joinToString(" · ")
                .ifBlank { "-" }
        )
        FilcKeyValue(label = "Oktató", value = shown.teacherName.ifBlank { "-" })
        FilcKeyValue(label = "Típus", value = shown.courseType.displayName)

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (reminderScheduled) filc.green.copy(alpha = 0.16f) else filc.accent.copy(alpha = 0.14f)
                )
                .clickable { onScheduleReminder(shown) }
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (reminderScheduled) Icons.Default.Check else Icons.Default.Alarm,
                contentDescription = null,
                tint = if (reminderScheduled) filc.green else filc.accent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (reminderScheduled) "Emlékeztető beállítva" else "Emlékeztető beállítása",
                style = MaterialTheme.typography.titleSmall,
                color = if (reminderScheduled) filc.green else filc.text,
                modifier = Modifier.weight(1f)
            )
            if (!reminderScheduled) {
                Icon(
                    imageVector = Icons.Default.AlarmOn,
                    contentDescription = null,
                    tint = filc.accent,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        AnimatedVisibility(visible = reminderScheduled, enter = fadeIn(), exit = fadeOut()) {
            Text(
                text = "15 perccel az óra kezdete előtt szólunk.",
                style = MaterialTheme.typography.labelSmall,
                color = filc.textMuted,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

/** Általános fejléc a többi lap tetejére: cím + akciógombok. */
@Composable
fun FilcScreenHeader(
    title: String,
    subtitle: String?,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable (() -> Unit)? = null
) {
    val filc = filcColors()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(start = 20.dp, end = 14.dp, top = 10.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
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
        if (actions != null) actions()
        Spacer(modifier = Modifier.width(6.dp))
        FilcRefreshButton(isRefreshing = isRefreshing, onRefresh = onRefresh)
    }
}
