package com.example.presentation.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Grading
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Upcoming
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.currentStrings
import com.example.domain.model.AcademicPeriod
import com.example.domain.model.CalendarEvent
import com.example.domain.model.DegreeProgress
import com.example.domain.model.ExamItem
import com.example.presentation.navigation.NavigationItem
import com.example.presentation.ui.components.CourseTypeBadge
import com.example.presentation.ui.components.NeptunTopBar
import com.example.presentation.viewmodel.DashboardUiState
import com.example.ui.theme.NeptunBlue40
import com.example.ui.theme.NeptunGold
import com.example.ui.theme.NeptunGreen
import com.example.ui.theme.NeptunPurple
import com.example.ui.theme.NeptunRed
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    studentName: String,
    universityName: String = "",
    trainingProgram: String = "",
    isDemoData: Boolean,
    onNavigate: (NavigationItem) -> Unit,
    onOpenExams: () -> Unit = { onNavigate(NavigationItem.GRADES) },
    onOpenProgress: () -> Unit = { onNavigate(NavigationItem.GRADES) },
    onOpenPeriods: () -> Unit = { onNavigate(NavigationItem.TIMETABLE) },
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = currentStrings()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NeptunTopBar(
            title = strings.navDashboard,
            subtitle = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy. MMMM d. EEEE", Locale(strings.languageCode))),
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Üdvözlés
            item {
                Column {
                    val hour = java.time.LocalTime.now().hour
                    val greetingText = when {
                        hour < 5 -> if (strings.languageCode == "hu") "Jó éjszakát" else if (strings.languageCode == "de") "Gute Nacht" else "Good night"
                        hour < 9 -> if (strings.languageCode == "hu") "Jó reggelt" else if (strings.languageCode == "de") "Guten Morgen" else "Good morning"
                        hour < 18 -> strings.dashboardGreeting
                        else -> if (strings.languageCode == "hu") "Jó estét" else if (strings.languageCode == "de") "Guten Abend" else "Good evening"
                    }
                    Text(
                        text = "$greetingText, $studentName!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isDemoData) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (strings.languageCode == "hu") "Demo adatokat látsz – a valódi adataid bejelentkezés után szinkronizálódnak."
                            else if (strings.languageCode == "de") "Sie sehen Demo-Daten – echte Daten werden nach der Anmeldung synchronisiert."
                            else "Viewing demo data – your real data will synchronize after logging in.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Közelgő felvett vizsga (ha van)
            uiState.nextUpcomingExam?.let { exam ->
                item {
                    UpcomingExamDashboardCard(
                        exam = exam,
                        onOpenExams = onOpenExams
                    )
                }
            }

            // Következő / zajló óra
            item {
                NextClassCard(
                    ongoing = uiState.ongoingEvent,
                    next = uiState.nextEvent,
                    hasClassesToday = uiState.todayClasses.isNotEmpty(),
                    onOpenTimetable = { onNavigate(NavigationItem.TIMETABLE) }
                )
            }

            // Aktuális féléves időszakok & határidők (következő óra alatt)
            if (uiState.activePeriods.isNotEmpty()) {
                item {
                    AcademicPeriodsDashboardCard(
                        periods = uiState.activePeriods,
                        onOpenPeriods = onOpenPeriods
                    )
                }
            }

            // Gyors statisztikák
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickStatCard(
                        icon = Icons.Default.Mail,
                        label = if (strings.languageCode == "hu") "Olvasatlan\nüzenet" else if (strings.languageCode == "de") "Ungelesene\nNachrichten" else "Unread\nMessages",
                        value = "${uiState.unreadMessages}",
                        tint = MaterialTheme.colorScheme.primary,
                        onClick = { onNavigate(NavigationItem.MESSAGES) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickStatCard(
                        icon = Icons.Default.Grading,
                        label = if (strings.languageCode == "hu") "Legutóbbi\njegy" else if (strings.languageCode == "de") "Letzte\nNote" else "Latest\nGrade",
                        value = uiState.latestGrades.firstOrNull()?.grade?.toString() ?: "–",
                        tint = NeptunGreen,
                        onClick = { onNavigate(NavigationItem.GRADES) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickStatCard(
                        icon = Icons.Default.AccountBalanceWallet,
                        label = if (strings.languageCode == "hu") "Fizetendő\n(Ft)" else if (strings.languageCode == "de") "Zu zahlen\n(Ft)" else "Due\n(HUF)",
                        value = if (uiState.pendingFinanceHuf > 0) formatShort(uiState.pendingFinanceHuf) else "0",
                        tint = if (uiState.pendingFinanceHuf > 0) MaterialTheme.colorScheme.error else NeptunGreen,
                        onClick = { onNavigate(NavigationItem.FINANCES) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Diploma haladás mini-kártya (ha elérhető)
            uiState.degreeProgress?.let { progress ->
                item {
                    DegreeProgressMiniCard(
                        progress = progress,
                        onOpenProgress = onOpenProgress
                    )
                }
            }

            // Mai órák listája
            item {
                Text(
                    text = "${strings.todayClasses} (${uiState.todayClasses.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (uiState.todayClasses.isEmpty()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = strings.noClassesScheduledToday,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(uiState.todayClasses) { event ->
                    DashboardClassRow(event = event)
                }
            }

            // Következő fizetendő tétel
            uiState.nextDueFinance?.let { finance ->
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate(NavigationItem.FINANCES) }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                val dueLabel = when (strings.languageCode) {
                                    "de" -> "Zu zahlen"
                                    "en" -> "Due"
                                    else -> "Fizetendő"
                                }
                                Text(
                                    text = "$dueLabel: ${finance.title}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${formatHuf(finance.amountHuf)} Ft · ${strings.dueDate}: ${finance.dueDate}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun NextClassCard(
    ongoing: CalendarEvent?,
    next: CalendarEvent?,
    hasClassesToday: Boolean,
    onOpenTimetable: () -> Unit
) {
    val strings = currentStrings()
    val event = ongoing ?: next
    val isWeekend = remember {
        val dow = java.time.LocalDate.now().dayOfWeek
        dow == java.time.DayOfWeek.SATURDAY || dow == java.time.DayOfWeek.SUNDAY
    }

    val randomMessage = remember(hasClassesToday, isWeekend, strings.languageCode) {
        val dayOfYear = java.time.LocalDate.now().dayOfYear
        if (hasClassesToday) {
            val list = strings.classesDoneMessages
            list[dayOfYear % list.size]
        } else if (isWeekend) {
            val list = strings.weekendFreeMessages
            list[dayOfYear % list.size]
        } else {
            val list = strings.weekdayFreeMessages
            list[dayOfYear % list.size]
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (ongoing != null) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenTimetable() }
    ) {
        if (event == null) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (hasClassesToday) Icons.Default.CheckCircle else Icons.Default.DateRange,
                        contentDescription = null,
                        tint = if (hasClassesToday) NeptunGreen else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (hasClassesToday) strings.todayClasses
                               else if (isWeekend) (when (strings.languageCode) { "de" -> "Wochenende"; "en" -> "Weekend"; else -> "Hétvége" })
                               else strings.todayClasses,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (hasClassesToday) NeptunGreen else MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = randomMessage,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (ongoing != null) Icons.Default.PlayCircle else Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (ongoing != null) NeptunGreen else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (ongoing != null) strings.inProgressClass
                               else strings.nextClass,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (ongoing != null) NeptunGreen else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = event.timeFormatted,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = event.subjectName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CourseTypeBadge(courseType = event.courseType)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = listOf(event.room.takeIf { it.isNotBlank() }, event.teacherName.takeIf { it.isNotBlank() })
                            .filterNotNull()
                            .joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun DashboardClassRow(event: CalendarEvent) {
    val strings = currentStrings()
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(
                    text = "%02d:%02d".format(event.startHour, event.startMinute),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.subjectName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = listOf(event.courseType.getLocalizedName(strings), event.room.takeIf { it.isNotBlank() })
                        .filterNotNull()
                        .joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatHuf(amount: Int): String {
    return "%,d".format(Locale("hu"), amount).replace(',', ' ')
}

@Composable
private fun AcademicPeriodsDashboardCard(
    periods: List<AcademicPeriod>,
    onOpenPeriods: () -> Unit = {}
) {
    val strings = currentStrings()
    val activePeriod = periods.firstOrNull { it.isCurrentlyActive }
        ?: periods.firstOrNull { it.isUpcoming }
        ?: return
    val isActive = activePeriod.isCurrentlyActive
    val daysLeft = if (isActive) activePeriod.daysRemaining else activePeriod.daysUntilStart

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive && daysLeft != null && daysLeft <= 2) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenPeriods)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isActive && daysLeft != null && daysLeft <= 2) NeptunRed.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = if (isActive && daysLeft != null && daysLeft <= 2) NeptunRed else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isActive) NeptunGreen.copy(alpha = 0.2f) else NeptunBlue40.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (isActive) strings.academicPeriodActiveBadge else strings.academicPeriodUpcomingBadge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) NeptunGreen else NeptunBlue40,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                    Text(
                        text = strings.academicPeriodsTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = activePeriod.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = when {
                        isActive && daysLeft != null && daysLeft == 0L -> strings.deadlineEndingToday
                        isActive && daysLeft != null -> strings.deadlineDaysRemaining(daysLeft)
                        !isActive && daysLeft != null -> strings.periodStartsInDays(daysLeft)
                        else -> "${activePeriod.startDate} - ${activePeriod.endDate}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isActive && daysLeft != null && daysLeft <= 2) NeptunRed else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun UpcomingExamDashboardCard(
    exam: ExamItem,
    onOpenExams: () -> Unit
) {
    val strings = currentStrings()
    val days = exam.daysUntilExam

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenExams() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = strings.upcomingExamsDashboardTitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (days != null) {
                    val (badgeBg, badgeFg, label) = when {
                        days == 0L -> Triple(NeptunRed.copy(alpha = 0.15f), NeptunRed, strings.examCountdownToday)
                        days == 1L -> Triple(NeptunGold.copy(alpha = 0.2f), NeptunGold, strings.examCountdownTomorrow)
                        else -> Triple(NeptunBlue40.copy(alpha = 0.15f), NeptunBlue40, strings.examCountdownDays(days))
                    }
                    Surface(
                        color = badgeBg,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = badgeFg,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = exam.subjectName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            val details = listOfNotNull(
                exam.fullDateTimeString.takeIf { it.isNotBlank() },
                listOf(exam.room, exam.location).filter { it.isNotBlank() }.distinct().joinToString(", ").takeIf { it.isNotBlank() },
                exam.examType.takeIf { it.isNotBlank() }
            ).joinToString(" · ")

            Text(
                text = details,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DegreeProgressMiniCard(
    progress: DegreeProgress,
    onOpenProgress: () -> Unit
) {
    val strings = currentStrings()
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenProgress() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.degreeProgressTitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "${progress.completedCredits} / ${progress.totalRequiredCredits} kredit (${progress.progressPercentage}%)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = NeptunGreen
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = NeptunGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

private fun formatShort(amount: Int): String {
    return when {
        amount >= 1_000_000 -> {
            val m = amount / 100_000.0
            if (m >= 10) "${m.toInt()}M" else String.format(Locale("hu"), "%.1fM", m)
        }
        amount >= 1000 -> "${amount / 1000}e"
        else -> "$amount"
    }
}
