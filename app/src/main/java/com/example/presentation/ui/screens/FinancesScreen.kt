package com.example.presentation.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.FinanceItem
import com.example.domain.model.FinanceStatus
import com.example.presentation.ui.components.FinanceStatusBadge
import com.example.presentation.ui.components.NeptunTopBar
import com.example.presentation.viewmodel.FinancesUiState
import com.example.ui.theme.NeptunGreen
import com.example.ui.theme.NeptunRed
import java.text.NumberFormat
import java.util.Locale

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun FinancesScreen(
    uiState: FinancesUiState,
    onFilterSelect: (FinanceStatus?) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hungarianNumberFormat = NumberFormat.getNumberInstance(Locale("hu", "HU"))

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NeptunTopBar(
            title = "Pénzügyek",
            subtitle = "Tételek, befizetések és kötelezettségek",
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh
        )

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

            // Summary Card
            item {
                FinanceSummaryCard(
                    pendingHuf = uiState.totalPendingHuf,
                    completedHuf = uiState.totalCompletedHuf,
                    numberFormat = hungarianNumberFormat
                )
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.selectedStatusFilter == null,
                        onClick = { onFilterSelect(null) },
                        label = { Text("Összes (${uiState.allFinances.size})") },
                        modifier = Modifier.testTag("finance_filter_all")
                    )

                    FilterChip(
                        selected = uiState.selectedStatusFilter == FinanceStatus.PENDING,
                        onClick = { onFilterSelect(FinanceStatus.PENDING) },
                        label = { Text("Kiírva / Fizetendő") },
                        modifier = Modifier.testTag("finance_filter_pending")
                    )

                    FilterChip(
                        selected = uiState.selectedStatusFilter == FinanceStatus.COMPLETED,
                        onClick = { onFilterSelect(FinanceStatus.COMPLETED) },
                        label = { Text("Teljesítve") },
                        modifier = Modifier.testTag("finance_filter_completed")
                    )
                }
            }

            if (uiState.filteredFinances.isEmpty()) {
                item {
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
                                tint = NeptunGreen,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Nincs ilyen státuszú pénzügyi tétel!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(uiState.filteredFinances) { item ->
                    FinanceItemCard(
                        item = item,
                        formattedAmount = "${hungarianNumberFormat.format(item.amountHuf)} Ft"
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
        }
    }
}

@Composable
private fun FinanceSummaryCard(
    pendingHuf: Int,
    completedHuf: Int,
    numberFormat: NumberFormat
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Pénzügyek",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Pénzügyi Egyenleg",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Pending Amount
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Fizetendő kötelezettség",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${numberFormat.format(pendingHuf)} Ft",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (pendingHuf > 0) NeptunRed else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Completed Amount
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Rendezett tételek",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${numberFormat.format(completedHuf)} Ft",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeptunGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun FinanceItemCard(
    item: FinanceItem,
    formattedAmount: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (item.status == FinanceStatus.PENDING) Color(0xFFF59E0B).copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("finance_item_${item.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FinanceStatusBadge(status = item.status)

                Text(
                    text = formattedAmount,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (item.status == FinanceStatus.PENDING) NeptunRed else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Félév: ${item.termName} • Bizonylatszám: ${item.transactionId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Határidő",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Határidő: ${item.dueDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (item.paymentDate != null) {
                    Text(
                        text = "Fizetve: ${item.paymentDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeptunGreen,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
