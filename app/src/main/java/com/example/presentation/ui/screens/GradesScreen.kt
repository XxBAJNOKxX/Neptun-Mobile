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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
    onRefresh: () -> Unit,
    onRefreshExams: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NeptunTopBar(
            title = "Jegyek & Átlagszámítás",
            subtitle = "Kreditindex és Szellemjegy kalkulátor",
            isRefreshing = if (uiState.selectedTab == 0) uiState.isRefreshing else uiState.isRefreshingExams,
            onRefresh = if (uiState.selectedTab == 0) onRefresh else onRefreshExams
        )

        // Jegyek / Vizsgák váltó
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            SegmentedButton(
                selected = uiState.selectedTab == 0,
                onClick = { onTabSelect(0) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text("Jegyek")
            }
            SegmentedButton(
                selected = uiState.selectedTab == 1,
                onClick = { onTabSelect(1) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text("Vizsgák")
            }
        }

        if (uiState.selectedTab == 1) {
            ExamsTabContent(exams = uiState.exams, isRefreshing = uiState.isRefreshingExams, onRefresh = onRefreshExams)
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
                    Text(
                        text = "Felvett tárgyak (${uiState.termGrades.size} db)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (uiState.calculation?.ghostCount ?: 0 > 0) {
                        Surface(
                            color = Color(0xFFEDE9FE),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { onResetAllGhostGrades() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Törlés",
                                    tint = NeptunPurple,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Szellemjegyek törlése",
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Átlagszámítás",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Tanulmányi Eredmények",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Formula: (Σ Jegy * Kredit) / Σ Teljesített Kredit",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                if (hasGhost) {
                    Surface(
                        color = Color(0xFFEDE9FE),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${calc?.ghostCount} szellemjegy aktív",
                            color = NeptunPurple,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
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
                        text = "Súlyozott Átlag",
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
                        text = "Kreditindex",
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
                    Text(
                        text = "Teljesített / Felvett",
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
                                contentDescription = "Szellemjegy hatás",
                                tint = NeptunPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Várható átlag szellemjegyekkel",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeptunPurple
                                )
                                Text(
                                    text = "Szimulált kreditindex: ${calc?.ghostCreditIndex ?: 0.0}",
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
                        text = "${subject.credit} kredit",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (subject.isSigned) {
                        Surface(
                            color = Color(0xFFF0FDF4),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = "Aláírva",
                                color = NeptunGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (subject.grade != null) {
                        GradeBadge(grade = subject.grade, gradeText = subject.gradeText, isGhost = false)
                    } else if (subject.ghostGrade != null) {
                        GradeBadge(grade = subject.ghostGrade, gradeText = "", isGhost = true)
                    } else {
                        GradeBadge(grade = null, gradeText = "Még nincs jegy", isGhost = false)
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
                        contentDescription = "Szellemjegy",
                        tint = if (subject.ghostGrade != null) NeptunPurple else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (subject.ghostGrade != null) "Szellemjegy módosítása (${subject.ghostGrade})" else "Szellemjegy hozzáadása",
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
                        .background(Color(0xFFEDE9FE))
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Szellemjegy",
                        tint = NeptunPurple,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Szellemjegy beállítása",
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
                    text = "Adj meg egy virtuális jegyet a várható féléves átlag és kreditindex azonnali szimulálásához.",
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
                        Text("Szellemjegy eltávolítása", color = MaterialTheme.colorScheme.error)
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
                        text = "Mégse",
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
                    text = "Féléves statisztika",
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
                    text = "Még nincs elég adat a statisztikához.",
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
                    text = "Kredithaladás",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$totalCompletedCredits / $targetCredits kredit",
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
                text = "A célt a Beállítások → Tanulmányok menüben módosíthatod.",
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
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        if (exams.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
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
                        text = "Nem található vizsgaadat.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "A vizsgalista ezen az egyetemi szerveren nem elérhető, vagy nincs felvett vizsgád. (Kísérleti funkció)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
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
                items(exams) { exam ->
                    ExamCard(exam = exam)
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
private fun ExamCard(exam: ExamItem) {
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
                if (exam.examType.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = exam.examType,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                if (!exam.isSignedUp) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Nincs jelentkezve",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
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
                exam.examDate.takeIf { it.isNotBlank() },
                exam.startTime.takeIf { it.isNotBlank() },
                listOf(exam.room, exam.location).filter { it.isNotBlank() }.distinct().joinToString(", ").takeIf { it.isNotBlank() },
                exam.courseCode.takeIf { it.isNotBlank() }
            ).joinToString(" · ")

            Text(
                text = details.ifEmpty { "Nincs részletinformáció" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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

