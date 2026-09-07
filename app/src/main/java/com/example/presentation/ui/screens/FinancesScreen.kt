package com.example.presentation.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Paid
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.FinanceItem
import com.example.domain.model.FinanceStatus
import com.example.presentation.ui.components.FilcChip
import com.example.presentation.ui.components.FilcDot
import com.example.presentation.ui.components.FilcEmptyState
import com.example.presentation.ui.components.FilcFilterBar
import com.example.presentation.ui.components.FilcPanel
import com.example.presentation.ui.components.FilcProgressBar
import com.example.presentation.ui.components.FinanceStatusBadge
import com.example.presentation.ui.components.filcCard
import com.example.presentation.viewmodel.FinancesUiState
import com.example.ui.theme.filcColors
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Pénzügyek – Filc stílus. Összesítő kártya a fizetendő összeggel és a
 * félév "előköltség" haladásával, alul a tételek listája státuszszűrővel.
 */
@Composable
fun FinancesScreen(
    uiState: FinancesUiState,
    onFilterSelect: (FinanceStatus?) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filc = filcColors()
    val visibleItems = uiState.filteredFinances
    val paid = uiState.allFinances.filter { it.status == FinanceStatus.COMPLETED }.sumOf { it.amountHuf }
    val open = uiState.totalPendingHuf
    val total = paid + open
    val progress = if (total > 0) paid.toFloat() / total.toFloat() else 0f
    val filterIndex = when (uiState.selectedStatusFilter) {
        null -> 0
        FinanceStatus.PENDING -> 1
        FinanceStatus.OVERDUE -> 2
        FinanceStatus.COMPLETED -> 3
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(filc.background)
    ) {
        FilcScreenHeader(
            title = "Pénzügyek",
            subtitle = "Beadott költségek és befizetések",
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                FinancesSummaryCard(
                    openAmount = open,
                    paidAmount = paid,
                    progress = progress,
                    modifier = Modifier.testTag("finances_summary_card")
                )
            }

            item {
                FilcFilterBar(
                    options = listOf("Minden tétel", "Kiírva", "Késedelmes", "Teljesítve"),
                    selectedIndex = filterIndex,
                    onSelect = { index ->
                        onFilterSelect(
                            when (index) {
                                1 -> FinanceStatus.PENDING
                                2 -> FinanceStatus.OVERDUE
                                3 -> FinanceStatus.COMPLETED
                                else -> null
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("finances_filter")
                )
            }

            if (visibleItems.isEmpty()) {
                item {
                    FilcPanel {
                        FilcEmptyState(
                            icon = Icons.Default.AccountBalanceWallet,
                            title = "Nincs megjeleníthető tétel",
                            description = "Válts szűrőt, vagy frissítsd a Neptun adataidat."
                        )
                    }
                }
            } else {
                items(visibleItems, key = { it.id }) { financeItem ->
                    FinanceTile(item = financeItem)
                }
            }
        }
    }
}

@Composable
private fun FinancesSummaryCard(
    openAmount: Int,
    paidAmount: Int,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val filc = filcColors()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .filcCard(shape = RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (openAmount > 0) filc.orange.copy(alpha = 0.18f) else filc.green.copy(alpha = 0.18f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (openAmount > 0) Icons.Default.Paid else Icons.Default.EventAvailable,
                    contentDescription = null,
                    tint = if (openAmount > 0) filc.orange else filc.green,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (openAmount > 0) "Fizetendő összeg" else "Nincs tartozásod",
                    style = MaterialTheme.typography.labelSmall,
                    color = filc.textMuted,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatHufAmount(openAmount),
                    style = MaterialTheme.typography.displaySmall,
                    color = if (openAmount > 0) filc.text else filc.green,
                    maxLines = 1
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Befizetve",
                    style = MaterialTheme.typography.labelSmall,
                    color = filc.textMuted,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${(progress * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = filc.textSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
            FilcProgressBar(progress = progress, barHeight = 8.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilcDot(color = filc.green, size = 7.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formatHufAmount(paidAmount),
                    style = MaterialTheme.typography.labelSmall,
                    color = filc.textMuted
                )
                if (openAmount > 0) {
                    Spacer(modifier = Modifier.width(12.dp))
                    FilcDot(color = filc.orange, size = 7.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatHufAmount(openAmount),
                        style = MaterialTheme.typography.labelSmall,
                        color = filc.textMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun FinanceTile(item: FinanceItem) {
    val filc = filcColors()
    val accentForStatus: Color = when (item.status) {
        FinanceStatus.COMPLETED -> filc.green
        FinanceStatus.PENDING -> filc.yellow
        FinanceStatus.OVERDUE -> filc.red
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .filcCard(shape = RoundedCornerShape(16.dp), elevation = 10.dp)
            .clip(RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(38.dp)
                .clip(RoundedCornerShape(45.dp))
                .background(accentForStatus)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = filc.text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.termName,
                    style = MaterialTheme.typography.labelSmall,
                    color = filc.textMuted
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (item.status != FinanceStatus.COMPLETED) {
                    FilcChip(
                        text = "határidő: ${item.dueDate}",
                        color = accentForStatus,
                        background = accentForStatus.copy(alpha = 0.14f)
                    )
                } else if (!item.paymentDate.isNullOrBlank()) {
                    Text(
                        text = "befizetve: ${item.paymentDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = filc.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatHufAmount(item.amountHuf),
                style = MaterialTheme.typography.titleMedium,
                color = filc.text,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            FinanceStatusBadge(item.status)
        }
    }
}

private fun formatHufAmount(amount: Int): String =
    NumberFormat.getNumberInstance(Locale("hu", "HU")).format(amount.toLong()) + " Ft"
