package com.example.presentation.ui.screens

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Upcoming
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CalendarEvent
import com.example.presentation.ui.components.CourseTypeBadge
import com.example.presentation.ui.components.NeptunTopBar
import com.example.presentation.viewmodel.TimetableUiState
import com.example.ui.theme.NeptunBlue40
import com.example.ui.theme.NeptunCyan40
import com.example.ui.theme.NeptunGreen
import kotlinx.coroutines.launch

private val DAYS = listOf(
    1 to "Hétfő",
    2 to "Kedd",
    3 to "Szerda",
    4 to "Csütörtök",
    5 to "Péntek"
)

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
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = (uiState.selectedDayOfWeek - 1).coerceIn(0, 4),
        pageCount = { 5 }
    )
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.selectedDayOfWeek) {
        val targetPage = (uiState.selectedDayOfWeek - 1).coerceIn(0, 4)
        if (pagerState.currentPage != targetPage && !pagerState.isScrollInProgress) {
            pagerState.scrollToPage(targetPage)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val day = pagerState.currentPage + 1
        if (day != uiState.selectedDayOfWeek) {
            onDaySelect(day)
        }
    }

    LaunchedEffect(uiState.notificationScheduledId) {
        if (uiState.notificationScheduledId != null) {
            snackbarHostState.showSnackbar("Értesítés beállítva 15 perccel az óra előtt!")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NeptunTopBar(
            title = "Heti Órarend",
            subtitle = if (uiState.isWeekView) "Heti összesített nézet" else "Napi bontás",
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh
        )

        // Week Navigation Bar (Switch weeks forward/backward)
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPreviousWeek,
                    modifier = Modifier.testTag("timetable_prev_week_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Előző hét",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = uiState.weekLabel.ifEmpty { "Órarend" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (uiState.selectedWeekOffset != 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.clickable { onCurrentWeek() }
                        ) {
                            Text(
                                text = "Mai hét",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onNextWeek,
                    modifier = Modifier.testTag("timetable_next_week_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Következő hét",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // View Mode Toggle and Day Selector Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val currentDayInfo = uiState.weekDays.getOrNull(uiState.selectedDayOfWeek - 1)
            Text(
                text = if (uiState.isWeekView) "Teljes heti nézet" else "${currentDayInfo?.dayName ?: ""}i órák (${currentDayInfo?.dateFormatted ?: ""})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            FilterChip(
                selected = uiState.isWeekView,
                onClick = onToggleWeekView,
                label = {
                    Text(if (uiState.isWeekView) "Napi nézet" else "Heti áttekintés")
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (uiState.isWeekView) Icons.Default.ViewAgenda else Icons.Default.ViewWeek,
                        contentDescription = "Nézet váltása",
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.testTag("timetable_view_toggle")
            )
        }

        // Day Tabs (Only in daily view)
        if (!uiState.isWeekView) {
            ScrollableTabRow(
                selectedTabIndex = (uiState.selectedDayOfWeek - 1).coerceIn(0, 4),
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                DAYS.forEachIndexed { index, pair ->
                    val isSelected = uiState.selectedDayOfWeek == pair.first
                    val dayInfo = uiState.weekDays.getOrNull(index)
                    val label = if (dayInfo != null) "${pair.second} (${dayInfo.dateFormatted})" else pair.second
                    Tab(
                        selected = isSelected,
                        onClick = {
                            onDaySelect(pair.first)
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                text = label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.testTag("day_tab_${pair.first}")
                    )
                }
            }
        }

        // Live Ongoing & Upcoming Highlight Cards Banner (Only for current week)
        if (uiState.selectedWeekOffset == 0 && (uiState.ongoingEvent != null || uiState.nextUpcomingEvent != null)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                uiState.ongoingEvent?.let { ongoing ->
                    LiveHighlightBanner(
                        title = "Éppen zajló óra",
                        event = ongoing,
                        badgeColor = NeptunGreen,
                        icon = Icons.Default.PlayCircle,
                        isOngoing = true,
                        onScheduleReminder = onScheduleReminder
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                uiState.nextUpcomingEvent?.let { nextUp ->
                    LiveHighlightBanner(
                        title = "Következő óra ma",
                        event = nextUp,
                        badgeColor = NeptunCyan40,
                        icon = Icons.Default.Upcoming,
                        isOngoing = false,
                        onScheduleReminder = onScheduleReminder
                    )
                }
            }
        }

        // Timetable Content (HorizontalPager for Days OR Full Week List)
        val hasDatedEvents = remember(uiState.events) {
            uiState.events.any { it.dateString.isNotBlank() }
        }

        if (uiState.isWeekView) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                DAYS.forEachIndexed { idx, (dayNum, dayName) ->
                    val weekDay = uiState.weekDays.getOrNull(idx)
                    val dayEvents = if (hasDatedEvents && weekDay != null) {
                        uiState.events.filter { it.dateString.startsWith(weekDay.isoDate) }
                    } else {
                        if (uiState.selectedWeekOffset == 0) uiState.events.filter { it.dayOfWeek == dayNum }
                        else emptyList()
                    }.sortedBy { it.startHour * 60 + it.startMinute }

                    item {
                        Text(
                            text = if (weekDay != null) "$dayName (${weekDay.dateFormatted})" else dayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }

                    if (dayEvents.isEmpty()) {
                        item {
                            EmptyDayNotice(message = "Ezen a napon nincs tanóra ezen a héten.")
                        }
                    } else {
                        items(dayEvents) { event ->
                            TimetableEventCard(
                                event = event,
                                isOngoing = event.id == uiState.ongoingEvent?.id,
                                isNextUpcoming = event.id == uiState.nextUpcomingEvent?.id,
                                onScheduleReminder = { onScheduleReminder(event) }
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val day = page + 1
                val weekDay = uiState.weekDays.getOrNull(page)
                val eventsForDay = if (hasDatedEvents && weekDay != null) {
                    uiState.events.filter { it.dateString.startsWith(weekDay.isoDate) }
                } else {
                    if (uiState.selectedWeekOffset == 0) uiState.events.filter { it.dayOfWeek == day }
                    else emptyList()
                }.sortedBy { it.startHour * 60 + it.startMinute }

                if (eventsForDay.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyDayNotice(message = "Erre a napra nincs felvett órád ezen a héten.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(eventsForDay) { event ->
                            TimetableEventCard(
                                event = event,
                                isOngoing = event.id == uiState.ongoingEvent?.id,
                                isNextUpcoming = event.id == uiState.nextUpcomingEvent?.id,
                                onScheduleReminder = { onScheduleReminder(event) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}

@Composable
private fun LiveHighlightBanner(
    title: String,
    event: CalendarEvent,
    badgeColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isOngoing: Boolean,
    onScheduleReminder: (CalendarEvent) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = badgeColor.copy(alpha = 0.12f)
        ),
        border = BorderStroke(1.5.dp, badgeColor.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = badgeColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = badgeColor
                )
                Text(
                    text = event.subjectName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${event.timeFormatted} • ${formatRoomAndLocation(event.room, event.location)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = { onScheduleReminder(event) },
                modifier = Modifier.testTag("alarm_button_${event.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = "Értesítés beállítása",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

fun formatRoomAndLocation(room: String, location: String): String {
    val r = room.trim()
    val l = location.trim()
    val isRGeneric = r.isEmpty() || r.equals("Nincs megadva", ignoreCase = true) || r.equals("Nincs terem", ignoreCase = true)
    val isLGeneric = l.isEmpty() || l.equals("Nincs megadva", ignoreCase = true) || l.equals("Nincs terem", ignoreCase = true)

    return when {
        isRGeneric && isLGeneric -> "Nincs terem megadva"
        !isRGeneric && isLGeneric -> r
        isRGeneric && !isLGeneric -> l
        r.equals(l, ignoreCase = true) -> r
        l.contains(r, ignoreCase = true) -> l
        r.contains(l, ignoreCase = true) -> r
        else -> "$r ($l)"
    }
}

fun formatTeacherNames(teacherStr: String): String {
    if (teacherStr.isBlank() ||
        teacherStr.equals("Nincs megadva", ignoreCase = true) ||
        teacherStr.equals("Oktató nincs megadva", ignoreCase = true) ||
        teacherStr.equals("Nincs tanár", ignoreCase = true)) {
        return ""
    }
    val names = teacherStr.split(";", ",")
        .map { it.trim() }
        .filter { it.isNotBlank() && !it.equals("Nincs megadva", ignoreCase = true) && !it.equals("Oktató nincs megadva", ignoreCase = true) && !it.equals("Nincs tanár", ignoreCase = true) }
        .distinct()
    return names.joinToString(", ")
}

@Composable
fun TimetableEventCard(
    event: CalendarEvent,
    isOngoing: Boolean,
    isNextUpcoming: Boolean,
    onScheduleReminder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isOngoing -> NeptunGreen
        isNextUpcoming -> NeptunCyan40
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }

    val borderWidth = if (isOngoing || isNextUpcoming) 2.dp else 1.dp

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(borderWidth, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isOngoing) 4.dp else 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("event_card_${event.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CourseTypeBadge(courseType = event.courseType)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isOngoing) {
                        Surface(
                            color = NeptunGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = "ÉPPEN ZAJLIK",
                                color = NeptunGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (isNextUpcoming) {
                        Surface(
                            color = NeptunCyan40.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = "KÖVETKEZŐ",
                                color = NeptunCyan40,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onScheduleReminder,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Értesítés emlékeztető",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = event.subjectName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            val subtitleText = remember(event.subjectCode, event.courseCode) {
                val sc = event.subjectCode.trim()
                val cc = event.courseCode.trim()
                when {
                    sc.isNotEmpty() && cc.isNotEmpty() && !sc.equals(cc, ignoreCase = true) && sc != "KÓD" && sc != "-" -> "$sc • $cc"
                    sc.isNotEmpty() && sc != "KÓD" && sc != "-" -> sc
                    cc.isNotEmpty() && cc != "-" -> cc
                    else -> ""
                }
            }

            if (subtitleText.isNotEmpty()) {
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time, Location, Instructor Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Idősáv",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.timeFormatted,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Room / Location
                val cleanRoomText = remember(event.room, event.location) {
                    formatRoomAndLocation(event.room, event.location)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Terem",
                        tint = NeptunBlue40,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = cleanRoomText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            val formattedTeacher = remember(event.teacherName) {
                formatTeacherNames(event.teacherName)
            }
            if (formattedTeacher.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Oktató",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formattedTeacher,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyDayNotice(message: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
