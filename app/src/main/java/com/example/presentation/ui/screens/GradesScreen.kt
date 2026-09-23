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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.i18n.currentStrings
import com.example.domain.model.ExamItem
import com.example.domain.model.SubjectGrade
import com.example.presentation.ui.components.GradeBadge
import com.example.presentation.ui.components.NeptunTopBar
import com.example.presentation.viewmodel.GradesUiState
import com.example.ui.theme.NeptunBlue40
import com.example.ui.theme.NeptunCyan40
import com.example.ui.theme.NeptunGold
import com.example.ui.theme.NeptunGreen
import com.example.ui.theme.NeptunPurple
import com.example.ui.theme.NeptunRed

@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
fun GradesScreen(
    uiState: GradesUiState,
    onSelectTerm: (String) -> Unit,
    onOpenGhostDialog: (SubjectGrade) -> Unit,
    onCloseGhostDialog: () -> Unit,
    onSetGhostGrade: (String, Int?) -> Unit,
    onResetAllGhostGrades: () -> Unit,
    onTabSelect: (Int) -> Unit = {},
    onExamFilterChange: (Int) -> Unit = {},
    onRefresh: () -> Unit,
    onRefreshExams: () -> Unit = {},
    onRefreshProgress: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = currentStrings()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NeptunTopBar(
            title = strings.gradesTitle,
            subtitle = when (uiState.selectedTab) {
                1 -> strings.tabExams
                2 -> strings.tabProgress
                else -> if (strings.languageCode == "hu") "Kreditindex és Szellemjegy kalkulátor"
                        else if (strings.languageCode == "de") "Kreditindex und Notensimulator"
                        else "Credit Index & Grade Simulator"
            },
            isRefreshing = when (uiState.selectedTab) {
                1 -> uiState.isRefreshingExams
                2 -> uiState.isRefreshingProgress
                else -> uiState.isRefreshing
            },
            onRefresh = when (uiState.selectedTab) {
                1 -> onRefreshExams
                2 -> onRefreshProgress
                else -> onRefresh
            }
        )

        // Jegyek / Vizsgák / Haladás váltó (3 fül)
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            SegmentedButton(
                selected = uiState.selectedTab == 0,
                onClick = { onTabSelect(0) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
            ) {
                Text(strings.tabGrades)
            }
            SegmentedButton(
                selected = uiState.selectedTab == 1,
                onClick = { onTabSelect(1) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
            ) {
                Text(strings.tabExams)
            }
            SegmentedButton(
                selected = uiState.selectedTab == 2,
                onClick = { onTabSelect(2) },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
            ) {
                Text(strings.tabProgress)
            }
        }

        if (uiState.errorMessage != null) {
            val errorText = when (uiState.errorMessage) {
                "GRADES_LOAD_FAILED" -> strings.gradesLoadFailed
                "EXAMS_LOAD_FAILED" -> strings.examsLoadFailed
                "PROGRESS_LOAD_FAILED" -> strings.degreeProgressLoadFailed
                else -> uiState.errorMessage
            }
            com.example.presentation.ui.components.SyncErrorBanner(
                errorMessage = errorText,
                onRetry = when (uiState.selectedTab) {
                    1 -> onRefreshExams
                    2 -> onRefreshProgress
                    else -> onRefresh
                }
            )
        }

        if (uiState.selectedTab == 1) {
            ExamsTabContent(
                exams = uiState.exams,
                examFilter = uiState.examFilter,
                onFilterChange = onExamFilterChange,
                isRefreshing = uiState.isRefreshingExams,
                onRefresh = onRefreshExams
            )
            return@Column
        }

        if (uiState.selectedTab == 2) {
            DegreeProgressTabContent(
                progress = uiState.degreeProgress,
                termStats = uiState.termStats,
                isRefreshing = uiState.isRefreshingProgress,
                onRefresh = onRefreshProgress
            )
            return@Column
        }

        // Semester selector tabs
        if (uiState.availableTerms.isNotEmpty()) {
            ScrollableTabRow(
                selectedTabIndex = uiState.availableTerms.indexOf(uiState.selectedTerm).coerceAtLeast(0),
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                uiState.availableTerms.forEach { term ->
                    val isSelected = term == uiState.selectedTerm
                    Tab(
                        selected = isSelected,
                        onClick = { onSelectTerm(term) },
                        text = {
                            Text(
                                text = term,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.testTag("term_tab_$term")
                    )
                }
            }
        }

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Academic KPI Card (Average, Credit index, Ghost simulation)
            item {
                AcademicSummaryCard(
                    uiState = uiState,
                    onResetAllGhostGrades = onResetAllGhostGrades
                )
            }

            // Féléves statisztika: kreditindex diagram + kredithaladás
            item {
                TermStatisticsCard(
                    termStats = uiState.termStats,
                    totalCompletedCredits = uiState.totalCompletedCredits,
                    targetCredits = uiState.targetCredits
                )
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val subjectsHeader = when (strings.languageCode) {
                        "de" -> "Belegte Fächer (${uiState.termGrades.size})"
                        "en" -> "Enrolled subjects (${uiState.termGrades.size})"
                        else -> "Felvett tárgyak (${uiState.termGrades.size} db)"
                    }
                    Text(
                        text = subjectsHeader,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (uiState.calculation?.ghostCount ?: 0 > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = NeptunPurple.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { onResetAllGhostGrades() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = strings.delete,
                                    tint = NeptunPurple,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = strings.resetGhostGrades,
                                    color = NeptunPurple,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Subject Cards
            items(uiState.termGrades) { subject ->
                SubjectGradeCard(
                    subject = subject,
                    onOpenGhostDialog = { onOpenGhostDialog(subject) }
                )
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
        }
    }

    // Ghost Mark Dialog
    if (uiState.ghostMarkDialogSubject != null) {
        GhostMarkPickerModal(
            subject = uiState.ghostMarkDialogSubject,
            onClose = onCloseGhostDialog,
            onSelectGrade = { grade ->
                onSetGhostGrade(uiState.ghostMarkDialogSubject.id, grade)
            }
        )
    }
}

@Composable
private fun AcademicSummaryCard(
    uiState: GradesUiState,
    onResetAllGhostGrades: () -> Unit
) {
    val calc = uiState.calculation
    val hasGhost = (calc?.ghostCount ?: 0) > 0
    val ghostDiff = if (calc != null && calc.weightedAverage > 0) {
        Math.round((calc.ghostWeightedAverage - calc.weightedAverage) * 100.0) / 100.0
    } else 0.0

    val strings = currentStrings()
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.5.dp,
            if (hasGhost) NeptunPurple else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = strings.weightedAverage,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = if (strings.languageCode == "de") "Studienergebnisse" else if (strings.languageCode == "hu") "Tanulmányi Eredmények" else "Academic Results",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (strings.languageCode == "de") "Formel: (Σ Note * Credits) / Σ Erreichte Credits" else if (strings.languageCode == "hu") "Formula: (Σ Jegy * Kredit) / Σ Teljesített Kredit" else "Formula: (Σ Grade * Credits) / Σ Completed Credits",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (hasGhost) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = NeptunPurple.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        val ghostActiveText = when (strings.languageCode) {
                            "de" -> "${calc?.ghostCount} Noten simuliert"
                            "en" -> "${calc?.ghostCount} simulated"
                            else -> "${calc?.ghostCount} szellemjegy"
                        }
                        Text(
                            text = ghostActiveText,
                            color = NeptunPurple,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main KPI numbers: Súlyozott átlag, Kreditindex, Kreditek
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Súlyozott Átlag (KGI)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.weightedAverage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "%.2f".format(calc?.weightedAverage ?: 0.0),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Kreditindex
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.creditIndex,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "%.2f".format(calc?.creditIndex ?: 0.0),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeptunCyan40
                    )
                }

                // Teljesített Kreditek
                Column(modifier = Modifier.weight(1f)) {
                    val creditsRatioLabel = when (strings.languageCode) {
                        "de" -> "Erreichte / Belegte"
                        "en" -> "Completed / Enrolled"
                        else -> "Teljesített / Felvett"
                    }
                    Text(
                        text = creditsRatioLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${calc?.completedCredits ?: 0} / ${calc?.totalCreditsEnrolled ?: 0}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Credit Progress Bar
            val progress = if ((calc?.totalCreditsEnrolled ?: 0) > 0) {
                (calc?.completedCredits ?: 0).toFloat() / calc!!.totalCreditsEnrolled.toFloat()
            } else 0f

            LinearProgressIndicator(
                progress = { progress },
                color = NeptunGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
            )

            // Ghost Mark Projected Preview Banner
            if (hasGhost) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = NeptunPurple.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, NeptunPurple.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = strings.ghostGrades,
                                tint = NeptunPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                val expectedAvgLabel = when (strings.languageCode) {
                                    "de" -> "Erwarteter Durchschnitt mit Notensimulation"
                                    "en" -> "Expected average with simulated grades"
                                    else -> "Várható átlag szellemjegyekkel"
                                }
                                val simCreditIndexLabel = when (strings.languageCode) {
                                    "de" -> "Simulierter Kreditindex: ${calc?.ghostCreditIndex ?: 0.0}"
                                    "en" -> "Simulated credit index: ${calc?.ghostCreditIndex ?: 0.0}"
                                    else -> "Szimulált kreditindex: ${calc?.ghostCreditIndex ?: 0.0}"
                                }
                                Text(
                                    text = expectedAvgLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeptunPurple
                                )
                                Text(
                                    text = simCreditIndexLabel,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "%.2f".format(calc?.ghostWeightedAverage ?: 0.0),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeptunPurple
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            if (ghostDiff != 0.0) {
                                Surface(
                                    color = if (ghostDiff > 0) NeptunGreen.copy(alpha = 0.15f) else NeptunRed.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (ghostDiff > 0) "+%.2f".format(ghostDiff) else "%.2f".format(ghostDiff),
                                        color = if (ghostDiff > 0) NeptunGreen else NeptunRed,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectGradeCard(
    subject: SubjectGrade,
    onOpenGhostDialog: () -> Unit
) {
    val strings = currentStrings()
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (subject.ghostGrade != null) NeptunPurple.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("subject_card_${subject.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = strings.creditsCount(subject.credit),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (subject.isSigned) {
                        Surface(
                            color = NeptunGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = strings.signedStatus,
                                color = NeptunGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (subject.grade != null) {
                        GradeBadge(grade = subject.grade, gradeText = subject.gradeText, isGhost = false)
                    } else if (subject.ghostGrade != null) {
                        GradeBadge(grade = subject.ghostGrade, gradeText = "", isGhost = true)
                    } else {
                        GradeBadge(grade = null, gradeText = strings.noGradeYet, isGhost = false)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subject.subjectName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subject.subjectCode,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Ghost Mark Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onOpenGhostDialog,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        1.dp,
                        if (subject.ghostGrade != null) NeptunPurple else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.testTag("ghost_grade_btn_${subject.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = strings.ghostGrades,
                        tint = if (subject.ghostGrade != null) NeptunPurple else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (subject.ghostGrade != null) strings.editGhostGrade(subject.ghostGrade) else strings.addGhostGrade,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (subject.ghostGrade != null) NeptunPurple else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun GhostMarkPickerModal(
    subject: SubjectGrade,
    onClose: () -> Unit,
    onSelectGrade: (Int?) -> Unit
) {
    val strings = currentStrings()
    Dialog(onDismissRequest = onClose) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(NeptunPurple.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = strings.ghostGrades,
                        tint = NeptunPurple,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = strings.setGhostGradeTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = subject.subjectName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )

                Text(
                    text = strings.ghostGradeDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 5, 4, 3, 2, 1 buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(5, 4, 3, 2).forEach { gradeNum ->
                        val isSelected = subject.ghostGrade == gradeNum
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) NeptunPurple else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .size(46.dp)
                                .clickable { onSelectGrade(gradeNum) }
                                .testTag("select_ghost_grade_$gradeNum")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$gradeNum",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Clear Ghost button
                if (subject.ghostGrade != null) {
                    OutlinedButton(
                        onClick = { onSelectGrade(null) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text(strings.removeGhostGrade, color = MaterialTheme.colorScheme.error)
                    }
                }

                Button(
                    onClick = onClose,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = strings.cancel,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun TermStatisticsCard(
    termStats: List<com.example.presentation.viewmodel.TermStat>,
    totalCompletedCredits: Int,
    targetCredits: Int
) {
    val strings = currentStrings()
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.semesterStats,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (termStats.isEmpty()) {
                Text(
                    text = strings.notEnoughDataForStats,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Súlyozott átlag oszlopdiagram félévenként
                val maxAvg = termStats.maxOf { it.weightedAverage }.coerceAtLeast(1.0)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.Bottom
                ) {
                    termStats.forEach { stat ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (stat.weightedAverage > 0) "%.2f".format(stat.weightedAverage) else "-",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .height(((stat.weightedAverage / maxAvg) * 72.0).coerceIn(3.0, 72.0).dp)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(
                                        if (stat.weightedAverage >= 4.0) NeptunGreen
                                        else if (stat.weightedAverage >= 3.0) NeptunCyan40
                                        else MaterialTheme.colorScheme.primary
                                    )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatShortTermLabel(stat.termId),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Kredithaladás a célig
            val progress = if (targetCredits > 0) {
                (totalCompletedCredits.toFloat() / targetCredits.toFloat()).coerceIn(0f, 1f)
            } else 0f

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.creditProgress,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = strings.creditProgressFormat(totalCompletedCredits, targetCredits),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = NeptunGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                text = strings.creditProgressHint,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun ExamsTabContent(
    exams: List<ExamItem>,
    examFilter: Int,
    onFilterChange: (Int) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    val strings = currentStrings()

    val filteredExams = remember(exams, examFilter) {
        when (examFilter) {
            1 -> exams.filter { it.isSignedUp }
            2 -> exams.filter { (it.daysUntilExam ?: 0) >= 0 }
            else -> exams
        }
    }

    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = examFilter == 0,
                        onClick = { onFilterChange(0) },
                        label = { Text(strings.examFilterAll) }
                    )
                    FilterChip(
                        selected = examFilter == 1,
                        onClick = { onFilterChange(1) },
                        label = { Text(strings.examFilterSignedUp) }
                    )
                    FilterChip(
                        selected = examFilter == 2,
                        onClick = { onFilterChange(2) },
                        label = { Text(strings.examFilterUpcoming) }
                    )
                }
            }

            if (filteredExams.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.HourglassEmpty,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = strings.noExamsFound,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = strings.examsNotAvailableNotice,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(filteredExams) { exam ->
                    ExamCard(exam = exam)
                }
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun ExamCard(exam: ExamItem) {
    val strings = currentStrings()
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (exam.examType.isNotBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = exam.examType,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    if (exam.isSignedUp) {
                        Surface(
                            color = NeptunGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = NeptunGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = strings.signedStatus,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeptunGreen
                                )
                            }
                        }
                    } else {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = strings.notRegisteredStatus,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Countdown badge
                val days = exam.daysUntilExam
                if (days != null) {
                    val (badgeBg, badgeFg, label) = when {
                        days < 0 -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, strings.examFinished)
                        days == 0L -> Triple(NeptunRed.copy(alpha = 0.15f), NeptunRed, strings.examCountdownToday)
                        days == 1L -> Triple(NeptunGold.copy(alpha = 0.2f), NeptunGold, strings.examCountdownTomorrow)
                        else -> Triple(NeptunBlue40.copy(alpha = 0.15f), NeptunBlue40, strings.examCountdownDays(days))
                    }
                    Surface(
                        color = badgeBg,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = badgeFg,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = exam.subjectName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (exam.subjectCode.isNotBlank() || exam.courseCode.isNotBlank()) {
                val codeText = listOf(exam.subjectCode, exam.courseCode).filter { it.isNotBlank() }.joinToString(" • ")
                Text(
                    text = codeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time / Date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = exam.fullDateTimeString,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Room / Location
                val place = listOf(exam.room, exam.location).filter { it.isNotBlank() }.distinct().joinToString(", ")
                if (place.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = place,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (exam.teacherName.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${strings.examinerLabel}: ${exam.teacherName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun DegreeProgressTabContent(
    progress: com.example.domain.model.DegreeProgress?,
    termStats: List<com.example.presentation.viewmodel.TermStat>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    val strings = currentStrings()
    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        if (progress == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = strings.loading,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // Hero Completion Card
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = strings.degreeProgressTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "${progress.completedCredits} / ${progress.totalRequiredCredits} kredit",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape,
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${progress.progressPercentage}%",
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { progress.progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }

                // Cumulative KPIs Card
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = strings.cumulativeAverageLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "%.2f".format(progress.cumulativeWeightedAverage),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = NeptunGreen
                                )
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = strings.cumulativeCreditIndexLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "%.2f".format(progress.cumulativeCreditIndex),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = NeptunPurple
                                )
                            }
                        }
                    }
                }

                // Completed Curriculums Summary
                if (progress.totalCurriculums > 0 || progress.completedCurriculums > 0) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = strings.completedCurriculumsLabel,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${progress.completedCurriculums} / ${progress.totalCurriculums} db teljesítve",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    color = NeptunGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${progress.completedCurriculums} / ${progress.totalCurriculums}",
                                        color = NeptunGreen,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Curriculum Templates List
                if (progress.templates.isNotEmpty()) {
                    item {
                        Text(
                            text = strings.curriculumTemplatesTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    items(progress.templates) { template ->
                        CurriculumTemplateCard(template = template)
                    }
                }

                // Category Breakdowns (Only rendered if real requirements are defined)
                val hasCategoryTotals = progress.compulsoryTotal > 0 ||
                        progress.compulsoryElectiveTotal > 0 ||
                        progress.freeElectiveTotal > 0 ||
                        progress.thesisTotal > 0 ||
                        progress.criteriaTotalCount > 0

                if (hasCategoryTotals) {
                    item {
                        Text(
                            text = strings.details,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (progress.compulsoryTotal > 0) {
                        item {
                            ProgressCategoryCard(
                                title = strings.compulsoryCreditsLabel,
                                completed = progress.compulsoryCompleted,
                                total = progress.compulsoryTotal,
                                accentColor = NeptunGreen
                            )
                        }
                    }

                    if (progress.compulsoryElectiveTotal > 0) {
                        item {
                            ProgressCategoryCard(
                                title = strings.compulsoryElectiveCreditsLabel,
                                completed = progress.compulsoryElectiveCompleted,
                                total = progress.compulsoryElectiveTotal,
                                accentColor = NeptunBlue40
                            )
                        }
                    }

                    if (progress.freeElectiveTotal > 0) {
                        item {
                            ProgressCategoryCard(
                                title = strings.freeElectiveCreditsLabel,
                                completed = progress.freeElectiveCompleted,
                                total = progress.freeElectiveTotal,
                                accentColor = NeptunPurple
                            )
                        }
                    }

                    if (progress.thesisTotal > 0) {
                        item {
                            ProgressCategoryCard(
                                title = strings.thesisCreditsLabel,
                                completed = progress.thesisCompleted,
                                total = progress.thesisTotal,
                                accentColor = NeptunGold
                            )
                        }
                    }

                    if (progress.criteriaTotalCount > 0) {
                        item {
                            ProgressCategoryCard(
                                title = strings.criteriaLabel,
                                completed = progress.criteriaPassedCount,
                                total = progress.criteriaTotalCount,
                                unit = "db",
                                accentColor = NeptunCyan40
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
private fun CurriculumTemplateCard(
    template: com.example.domain.model.CurriculumTemplateItem
) {
    val strings = currentStrings()
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (template.isCompleted) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = NeptunGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = NeptunGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strings.completedBadge,
                                color = NeptunGreen,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            if (template.totalSubjects > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = strings.compulsoryCreditsLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = strings.subjectsCompletedFormat(template.completedSubjects, template.totalSubjects),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                val subjFraction = (template.completedSubjects.toFloat() / template.totalSubjects.toFloat()).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { subjFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (template.isCompleted) NeptunGreen else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            if (template.totalCredits > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = strings.targetCreditsLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = strings.creditsCompletedFormat(template.completedCredits, template.totalCredits),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                val credFraction = (template.completedCredits.toFloat() / template.totalCredits.toFloat()).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { credFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (template.isCompleted) NeptunGreen else NeptunPurple,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProgressCategoryCard(
    title: String,
    completed: Int,
    total: Int,
    unit: String = "kredit",
    accentColor: Color
) {
    val fraction = if (total > 0) (completed.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
    val pct = (fraction * 100).toInt()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$completed / $total $unit ($pct%)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = accentColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

private fun formatShortTermLabel(raw: String): String {
    if (raw.isBlank()) return ""
    val cleaned = raw.trim()
        .replace(Regex("""(?i)\s*félév.*"""), "")
        .replace(Regex("""(?i)\s*felev.*"""), "")
        .replace(Regex("""(?i)\s*semester.*"""), "")
        .trim()

    // e.g. "2025/26/1", "2025/2026/1", "25/26/1" -> "26/1"
    val regexFull = Regex("""(?:20)?(\d{2})/(?:20)?(\d{2})/(\d)""")
    val matchFull = regexFull.find(cleaned)
    if (matchFull != null) {
        val secondYear = matchFull.groupValues[2]
        val termNum = matchFull.groupValues[3]
        return "$secondYear/$termNum"
    }

    // e.g. "2025/1" or "2026/2" -> "25/1", "26/2"
    val regexSingleYear = Regex("""(?:20)?(\d{2})/(\d)""")
    val matchSingleYear = regexSingleYear.find(cleaned)
    if (matchSingleYear != null) {
        val year = matchSingleYear.groupValues[1]
        val termNum = matchSingleYear.groupValues[2]
        return "$year/$termNum"
    }

    // e.g. "26/1"
    if (Regex("""^\d{2}/\d$""").matches(cleaned)) {
        return cleaned
    }

    if (cleaned.length <= 4) {
        return cleaned
    }

    return cleaned.takeLast(4)
}

