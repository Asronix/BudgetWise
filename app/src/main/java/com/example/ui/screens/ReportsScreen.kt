package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.Formatters
import com.example.ui.components.TransactionTypeBadge
import com.example.ui.components.getCategoryColor
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IncomeGreen
import com.example.ui.viewmodel.BudgetViewModel
import com.example.ui.viewmodel.ChartBucket
import com.example.ui.viewmodel.ReportTimeFrame

@Composable
fun ReportsScreen(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val timeFrame by viewModel.reportTimeFrame.collectAsStateWithLifecycle()
    val report by viewModel.timeframeReport.collectAsStateWithLifecycle()
    var txSearchQuery by remember { mutableStateOf("") }

    val filteredTransactions = report.transactions.filter { tx ->
        txSearchQuery.isBlank() ||
                tx.title.contains(txSearchQuery, ignoreCase = true) ||
                tx.notes.contains(txSearchQuery, ignoreCase = true) ||
                tx.category.contains(txSearchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Timeframe Tabs: Day | Week | Month | Year
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ReportTimeFrame.values().forEach { tf ->
                    val isSelected = timeFrame == tf
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) EmeraldPrimary else Color.Transparent)
                            .clickable { viewModel.setReportTimeFrame(tf) }
                            .padding(vertical = 8.dp)
                            .testTag("report_tab_${tf.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tf.label,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // 2. Period Navigation Header (< October 2026 >)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.prevReportPeriod() }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous period")
                }
                Text(
                    text = report.periodLabel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { viewModel.nextReportPeriod() }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next period")
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { viewModel.resetReportPeriodToToday() }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        "Today",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 3. Metric Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("report_summary_hero"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${timeFrame.label} Cash Flow Summary",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (report.netSavings >= 0) IncomeGreen.copy(alpha = 0.25f) else ExpenseRed.copy(alpha = 0.25f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (report.netSavings >= 0) "+ Net Gain" else "- Net Deficit",
                                color = if (report.netSavings >= 0) IncomeGreen else ExpenseRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = Formatters.formatCurrency(report.netSavings),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Inflow & Outflow pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = IncomeGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Total Inflow", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                                    Text(Formatters.formatCurrency(report.totalIncome), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Total Outflow", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                                    Text(Formatters.formatCurrency(report.totalExpense), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Daily Avg & Volume Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Daily Avg: ${Formatters.formatCurrency(report.dailyAverageExpense)}/day",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${report.transactionCount} transactions logged",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 4. Interactive Bar Chart
        if (report.chartBuckets.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Spending & Inflow Trend",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(IncomeGreen))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Income", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ExpenseRed))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Expense", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        FinancialBarChart(buckets = report.chartBuckets)
                    }
                }
            }
        }

        // 5. Largest Transaction Callout (if available)
        if (report.largestExpense != null) {
            val maxTx = report.largestExpense!!
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CategoryIconBadge(category = maxTx.category, size = 38)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Largest Single Expense", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(maxTx.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(maxTx.category, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            Formatters.formatCurrency(maxTx.amount),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = ExpenseRed
                        )
                    }
                }
            }
        }

        // 6. Category Spending Distribution in this period
        item {
            Text(
                text = "Period Category Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (report.categoryBreakdown.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("No spending logged in this period", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(report.categoryBreakdown) { (cat, amount) ->
                val frac = if (report.totalExpense > 0) (amount / report.totalExpense).toFloat() else 0f
                val color = getCategoryColor(cat)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryIconBadge(category = cat, size = 34)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(cat, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text(Formatters.formatCurrency(amount), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${(frac * 100).toInt()}%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { frac },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = color,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }

        // 7. Transaction ledger for this period
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (txSearchQuery.isNotBlank()) "Transactions (${filteredTransactions.size} of ${report.transactions.size})"
                        else "Transactions Logged (${report.transactions.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (report.transactions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = txSearchQuery,
                        onValueChange = { txSearchQuery = it },
                        placeholder = { Text("Filter report by merchant or notes...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (txSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { txSearchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("report_transaction_search")
                    )
                }
            }
        }

        if (filteredTransactions.isEmpty() && txSearchQuery.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "No report transactions matching \"$txSearchQuery\"",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredTransactions) { tx ->
                val isInc = tx.type.equals("INCOME", true)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryIconBadge(category = tx.category, size = 38)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tx.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${tx.category} • ${Formatters.formatDate(tx.dateMillis)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = "${if (isInc) "+" else "-"}${Formatters.formatCurrency(tx.amount)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isInc) IncomeGreen else ExpenseRed
                            )
                        }

                        if (tx.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Notes,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = tx.notes,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FinancialBarChart(
    buckets: List<ChartBucket>,
    modifier: Modifier = Modifier
) {
    val maxVal = buckets.maxOfOrNull { maxOf(it.expense, it.income) }?.coerceAtLeast(10.0) ?: 100.0

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height - 20f
                val numBuckets = buckets.size
                val bucketWidth = canvasWidth / numBuckets
                val barWidth = (bucketWidth * 0.30f).coerceIn(4f, 22f)

                buckets.forEachIndexed { index, b ->
                    val centerX = (index * bucketWidth) + (bucketWidth / 2f)

                    // Expense bar (left)
                    val expHeight = ((b.expense / maxVal) * canvasHeight).toFloat().coerceIn(0f, canvasHeight)
                    if (expHeight > 0) {
                        drawRoundRect(
                            color = ExpenseRed,
                            topLeft = Offset(centerX - barWidth - 2f, canvasHeight - expHeight),
                            size = Size(barWidth, expHeight),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }

                    // Income bar (right)
                    val incHeight = ((b.income / maxVal) * canvasHeight).toFloat().coerceIn(0f, canvasHeight)
                    if (incHeight > 0) {
                        drawRoundRect(
                            color = IncomeGreen,
                            topLeft = Offset(centerX + 2f, canvasHeight - incHeight),
                            size = Size(barWidth, incHeight),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }
                }
            }
        }

        // Labels row below canvas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            buckets.forEach { b ->
                Text(
                    text = b.label,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
