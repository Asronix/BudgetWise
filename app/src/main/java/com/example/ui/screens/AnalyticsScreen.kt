package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TransactionType
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.Formatters
import com.example.ui.components.getCategoryColor
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IncomeGreen
import com.example.ui.viewmodel.BudgetViewModel

@Composable
fun AnalyticsScreen(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val monthYear by viewModel.currentMonthYear.collectAsStateWithLifecycle()
    val summary by viewModel.monthSummary.collectAsStateWithLifecycle()
    val transactions by viewModel.monthlyTransactions.collectAsStateWithLifecycle()

    val expenses = transactions.filter { it.type.equals(TransactionType.EXPENSE.name, ignoreCase = true) }
    val totalExpense = summary.totalExpenses

    // Calculate category percentages sorted descending
    val categoryTotals = summary.categorySpending.entries
        .sortedByDescending { it.value }

    val savingsRate = if (summary.totalIncome > 0) {
        ((summary.netBalance / summary.totalIncome) * 100).coerceIn(-100.0, 100.0)
    } else 0.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("analytics_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Month Header
        item {
            MonthSelectorHeader(
                year = monthYear.year,
                month = monthYear.month,
                onPrev = { viewModel.previousMonth() },
                onNext = { viewModel.nextMonth() },
                onToday = { viewModel.resetToCurrentMonth() }
            )
        }

        // Financial Health & Savings Rate Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("savings_rate_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Monthly Savings Rate",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (savingsRate >= 20.0) IncomeGreen.copy(alpha = 0.25f) else GoldAccent.copy(alpha = 0.25f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (savingsRate >= 20.0) "Excellent" else "Moderate",
                                color = if (savingsRate >= 20.0) IncomeGreen else GoldAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${String.format("%.1f", savingsRate)}%",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (savingsRate >= 0) {
                            "You saved ${Formatters.formatCurrency(summary.netBalance)} of your ${Formatters.formatCurrency(summary.totalIncome)} income this month."
                        } else {
                            "Expenses exceeded income by ${Formatters.formatCurrency(-summary.netBalance)} this month."
                        },
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Smart Insights Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Budgeting Tip",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val tipText = when {
                            categoryTotals.isNotEmpty() && (categoryTotals.first().value / totalExpense) > 0.4 -> {
                                "${categoryTotals.first().key} represents ${(categoryTotals.first().value / totalExpense * 100).toInt()}% of your total spending."
                            }
                            savingsRate > 20 -> {
                                "Great job! Financial advisors recommend saving at least 20% of monthly income for long-term goals."
                            }
                            else -> {
                                "Review your flexible spending in Food & Shopping to increase your monthly savings contribution."
                            }
                        }
                        Text(
                            text = tipText,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        // Expense Category Distribution Section
        item {
            Text(
                text = "Spending by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (categoryTotals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No expenses recorded this month yet.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(categoryTotals) { (category, amount) ->
                val fraction = if (totalExpense > 0) (amount / totalExpense).toFloat() else 0f
                val percent = (fraction * 100).toInt()
                val color = getCategoryColor(category)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryIconBadge(category = category, size = 36)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = category,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = Formatters.formatCurrency(amount),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "$percent% of total",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = color,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}
