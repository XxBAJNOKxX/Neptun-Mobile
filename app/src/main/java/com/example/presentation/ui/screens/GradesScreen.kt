package com.example.presentation.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.GradeCalculation
import com.example.domain.model.SubjectGrade
import com.example.presentation.ui.components.AveragePill
import com.example.presentation.ui.components.FilcBottomSheet
import com.example.presentation.ui.components.FilcChip
import com.example.presentation.ui.components.FilcEmptyState
import com.example.presentation.ui.components.FilcFilterBar
import com.example.presentation.ui.components.FilcPanel
import com.example.presentation.ui.components.FilcProgressBar
import com.example.presentation.ui.components.GradeBadge
import com.example.presentation.ui.components.TrendDisplay
import com.example.presentation.ui.components.filcCard
import com.example.presentation.ui.components.formatHungarian
import com.example.presentation.viewmodel.GradesUiState
import com.example.ui.theme.filcColors
import kotlin.math.roundToInt

/**
 * Jegyek – Filc stílus. Felül az átlagok kártyája (súlyozott átlag, kreditindex,
 * kredit-folyam), alul a tantárgyak listája jegykörökkel. Egy tantárgyra
 * koppintva a reFilc-féle "szellemjegy" kalkulátor nyílik meg alulról.
 */
@Composable
fun GradesScreen(
    uiState: GradesUiState,
    onSelectTerm: (String) -> Unit,
    onOpenGhostDialog: (SubjectGrade) -> Unit,
    onCloseGhostDialog: () -> Unit,
    onSetGhostGrade: (String, Int?) -> Unit,
    onResetAllGhostGrades: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filc = filcColors()
    val calc = uiState.calculation
    val subjects = uiState.termGrades.groupBy { it.subjectName }
    val previousAverage = uiState.allGrades
        .filter { it.termId.isNotBlank() && it.termId != uiState.selectedTerm }
        .let { previous ->
            val weighted = previous.filter { (it.grade ?: 0) >= 2 }
            val sum = weighted.sumOf { (it.grade ?: 0) * it.credit }
            val credits = weighted.sumOf { it.credit }
            if (credits > 0) (sum.toDouble() / credits * 100.0).roundToInt() / 100.0 else 0.0
        }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(filc.background)
    ) {
        FilcScreenHeader(
            title = "Jegyek",
            subtitle = uiState.selectedTerm.ifBlank { "átlagok és kreditjeid" },
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            actions = {
                if (uiState.calculation?.ghostCount?.let { it > 0 } == true) {
                    Box(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clip(RoundedCornerShape(45.dp))
                            .background(filc.purple.copy(alpha = 0.14f))
                            .clickable(onClick = onResetAllGhostGrades)
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                            .testTag("grades_reset_ghosts"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = filc.purple,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "szellemek törlése",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = filc.purple
                            )
                        }
                    }
                }
            }
        )

        if (uiState.availableTerms.size > 1) {
            FilcFilterBar(
                options = uiState.availableTerms,
                selectedIndex = uiState.availableTerms.indexOf(uiState.selectedTerm),
                onSelect = { index -> onSelectTerm(uiState.availableTerms[index]) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp)
                    .testTag("grades_term_filter")
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                GradesAverageCard(
                    calculation = calc,
                    trend = if (previousAverage > 0 && calc != null) {
                        calc.weightedAverage - previousAverage
                    } else {
                        0.0
                    },
                    modifier = Modifier.testTag("grades_average_card")
                )
            }

            if (subjects.isEmpty()) {
                item {
                    FilcPanel {
                        FilcEmptyState(
                            icon = Icons.Default.School,
                            title = "Ebben a félévben még nincs jegy",
                            description = "Amint bekerül az első jegy a Neptunba, itt fogja látni."
                        )
                    }
                }
            } else {
                item {
                    Text(
                        text = "${subjects.size} tantárgy · ${uiState.termGrades.sumOf { it.credit }} kredit",
                        style = MaterialTheme.typography.titleMedium,
                        color = filc.text.copy(alpha = 0.65f),
                        modifier = Modifier.padding(start = 14.dp, bottom = 0.dp)
                    )
                }
                items(subjects.entries.toList(), key = { it.first }) { entry ->
                    SubjectGradesRow(
                        subjectName = entry.key,
                        grades = entry.value,
                        onClick = { onOpenGhostDialog(entry.value.first()) }
                    )
                }
            }
        }
    }

    GhostGradeSheet(
        subject = uiState.ghostMarkDialogSubject,
        calculation = calc,
        onDismiss = onCloseGhostDialog,
        onApply = { subjectId, grade ->
            onSetGhostGrade(subjectId, grade)
        }
    )
}

/** Az átlagok kártyája: súlyozott átlag, kreditindex, kredit-folyam. */
@Composable
private fun GradesAverageCard(
    calculation: GradeCalculation?,
    trend: Double,
    modifier: Modifier = Modifier
) {
    val filc = filcColors()
    val weighted = calculation?.weightedAverage ?: 0.0
    val ghostWeighted = calculation?.ghostWeightedAverage ?: 0.0
    val creditIndex = calculation?.creditIndex ?: 0.0
    val completed = calculation?.completedCredits ?: 0
    val planned = calculation?.totalCreditsEnrolled ?: 0
    val progress = if (planned > 0) completed.toFloat() / planned.toFloat() else 0f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .filcCard(shape = RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Súlyozott tanulmányi átlag",
                    style = MaterialTheme.typography.labelSmall,
                    color = filc.textMuted,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (weighted > 0) formatHungarian(weighted) else "-",
                    style = MaterialTheme.typography.displaySmall,
                    color = filc.averageColor(weighted),
                    maxLines = 1
                )
                if (calculation != null && calculation.ghostCount > 0 && ghostWeighted > weighted) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AveragePill(average = ghostWeighted, scale = 0.8f, dashed = true, bordered = true)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ha minden szellem megjön",
                            style = MaterialTheme.typography.labelSmall,
                            color = filc.textMuted,
                            maxLines = 1
                        )
                    }
                }
            }
            if (trend != 0.0) {
                TrendDisplay(delta = trend)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(filc.hairline)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Kreditindex",
                    style = MaterialTheme.typography.labelSmall,
                    color = filc.textMuted,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (creditIndex > 0) formatHungarian(creditIndex) else "-",
                    style = MaterialTheme.typography.titleLarge,
                    color = filc.text
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Teljesített kredit",
                    style = MaterialTheme.typography.labelSmall,
                    color = filc.textMuted,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$completed / $planned",
                    style = MaterialTheme.typography.titleLarge,
                    color = filc.text
                )
            }
        }

        FilcProgressBar(progress = progress, barHeight = 7.dp)
        Text(
            text = if (progress >= 1f) {
                "Kreditköteled teljes – szép félév!"
            } else {
                "${(progress * 100).roundToInt()}%-on jársz a félév kreditjeivel."
            },
            style = MaterialTheme.typography.labelSmall,
            color = filc.textMuted
        )
    }
}

/** Egy tantárgy sorai: minden jegy egy-egy színezett kör. */
@Composable
private fun SubjectGradesRow(
    subjectName: String,
    grades: List<SubjectGrade>,
    onClick: () -> Unit
) {
    val filc = filcColors()
    val totalCredit = grades.sumOf { it.credit }
    val average = grades
        .mapNotNull { it.effectiveGrade }
        .let { values -> if (values.isEmpty()) 0.0 else values.average() }
    val hasGhost = grades.any { it.ghostGrade != null }

    FilcPanel(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("grades_subject_row")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subjectName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = filc.text,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "$totalCredit kredit",
                        style = MaterialTheme.typography.labelSmall,
                        color = filc.textMuted
                    )
                    if (hasGhost) {
                        FilcChip(
                            text = "szellem",
                            color = filc.purple,
                            background = filc.purple.copy(alpha = 0.14f)
                        )
                    }
                    grades.firstOrNull { it.gradeText.isNotBlank() }?.let { g ->
                        FilcChip(
                            text = g.gradeText,
                            color = filc.textMuted,
                            background = filc.text.copy(alpha = 0.06f)
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                grades.forEach { grade ->
                    GradeBadge(
                        grade = grade.effectiveGrade,
                        isGhost = grade.ghostGrade != null,
                        filled = grade.effectiveGrade != null,
                        size = 30.dp,
                        text = if (grade.effectiveGrade == null) "?" else null
                    )
                }
            }

            if (average > 0) {
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = formatHungarian(average),
                    style = MaterialTheme.typography.titleMedium,
                    color = filc.averageColor(average),
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Szellemjegy-kalkulátor: kiválasztod, milyen jegyet kapsz, és az app kiszámolja,
 * hogyan befolyásolja az a félév átlagát.
 */
@Composable
private fun GhostGradeSheet(
    subject: SubjectGrade?,
    calculation: GradeCalculation?,
    onDismiss: () -> Unit,
    onApply: (String, Int?) -> Unit
) {
    val filc = filcColors()
    FilcBottomSheet(visible = subject != null, onDismiss = onDismiss) {
        val shown = subject ?: return@FilcBottomSheet

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = filc.purple,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = shown.subjectName,
                    style = MaterialTheme.typography.titleLarge,
                    color = filc.text,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${shown.credit} kredit · tervedben: ${shown.gradeText.ifBlank { "nincs jegy" }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = filc.textMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Milyen jegyet kapsz?",
            style = MaterialTheme.typography.titleSmall,
            color = filc.text.copy(alpha = 0.65f)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            (1..5).forEach { gradeValue ->
                val selected = shown.ghostGrade == gradeValue
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) {
                                filc.gradeColor(gradeValue)
                            } else {
                                filc.gradeColor(gradeValue).copy(alpha = 0.16f)
                            }
                        )
                        .clickable(onClick = { onApply(shown.id, gradeValue) })
                        .testTag("ghost_grade_$gradeValue"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = gradeValue.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) {
                            Color.Black.copy(alpha = 0.85f)
                        } else {
                            filc.gradeColor(gradeValue)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        val projected = calculation?.let {
            previewWeightedAverage(it, shown, shown.ghostGrade)
        } ?: 0.0
        if (projected > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(filc.text.copy(alpha = 0.035f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Jelenlegi átlag",
                        style = MaterialTheme.typography.labelSmall,
                        color = filc.textMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formatHungarian(calculation?.weightedAverage ?: 0.0),
                        style = MaterialTheme.typography.titleLarge,
                        color = filc.text
                    )
                }
                Text(
                    text = "→",
                    color = filc.textMuted,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Várható átlag",
                        style = MaterialTheme.typography.labelSmall,
                        color = filc.textMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formatHungarian(projected),
                        style = MaterialTheme.typography.titleLarge,
                        color = filc.averageColor(projected)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(filc.text.copy(alpha = 0.06f))
                    .clickable(onClick = onDismiss)
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Bezárás",
                    style = MaterialTheme.typography.titleSmall,
                    color = filc.text
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(filc.accent)
                    .clickable(onClick = { onApply(shown.id, null) })
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Szellem törlése",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (filc.isLight) Color.Black.copy(alpha = 0.85f) else filc.onAccent
                )
            }
        }
    }
}

/** A szellemjegyre várható súlyozott átlag (nagyságrendi becslés). */
private fun previewWeightedAverage(
    calculation: GradeCalculation,
    subject: SubjectGrade,
    ghost: Int?
): Double {
    if (ghost == null || ghost < 2) return calculation.weightedAverage
    val realGrade = subject.grade
    val baseSum = calculation.weightedAverage * calculation.completedCredits
    val adjustedSum = if (realGrade != null && realGrade >= 2) {
        baseSum - realGrade * subject.credit + ghost * subject.credit
    } else {
        baseSum + ghost * subject.credit
    }
    val adjustedCredits = if (realGrade != null && realGrade >= 2) {
        calculation.completedCredits
    } else {
        calculation.completedCredits + subject.credit
    }
    if (adjustedCredits <= 0) return calculation.weightedAverage
    return (adjustedSum / adjustedCredits * 100.0).roundToInt() / 100.0
}
