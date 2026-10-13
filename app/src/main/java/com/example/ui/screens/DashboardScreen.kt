package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CategoryBudgetEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.Formatters
import com.example.ui.components.TransactionTypeBadge
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.WarningOrange
import com.example.ui.viewmodel.BudgetViewModel

@Composable
fun DashboardScreen(
    viewModel: BudgetViewModel,
    onNavigateToTransactions: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToReports: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onAddIncomeClick: () -> Unit,
    onGoalClick: (SavingsGoalEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthYear by viewModel.currentMonthYear.collectAsStateWithLifecycle()
    val summary by viewModel.monthSummary.collectAsStateWithLifecycle()
    val transactions by viewModel.monthlyTransactions.collectAsStateWithLifecycle()
    val budgets by viewModel.categoryBudgets.collectAsStateWithLifecycle()
    val goals by viewModel.savingsGoals.collectAsStateWithLifecycle()
    val recurringBills by viewModel.recurringBills.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 90.dp)
    ) {
        // 1. Month Selector Header
        item {
            MonthSelectorHeader(
                year = monthYear.year,
                month = monthYear.month,
                onPrev = { viewModel.previousMonth() },
                onNext = { viewModel.nextMonth() },
                onToday = { viewModel.resetToCurrentMonth() }
            )
        }

        // 2. Monthly Summary Hero Card
        item {
            MonthlyOverviewHero(
                totalIncome = summary.totalIncome,
                totalExpenses = summary.totalExpenses,
                netBalance = summary.netBalance,
                totalBudget = summary.totalBudgetCap,
                remainingBudget = summary.remainingBudget,
                budgetUsedFraction = summary.budgetUsedFraction,
                onAddExpenseClick = onAddExpenseClick,
                onAddIncomeClick = onAddIncomeClick
            )
        }

        // Detailed Transaction Report Launcher Banner (Day/Week/Month/Year)
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onNavigateToReports() }
                    .testTag("open_reports_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Detailed Reports",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(EmeraldPrimary)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Day • Wk • Mo • Yr", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Analyze spending trends, daily pace, and visual cash flow charts",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "View Reports",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Financial Health Score Widget
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(GoldAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Savings,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Financial Health Score", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (summary.healthScore >= 75) IncomeGreen.copy(alpha = 0.2f) else GoldAccent.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${summary.healthScore}/100",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = if (summary.healthScore >= 75) IncomeGreen else GoldAccent
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { (summary.healthScore / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (summary.healthScore >= 75) IncomeGreen else GoldAccent,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (summary.healthScore >= 75)
                            "Great job! Your savings rate and budget adherence are in strong standing."
                        else "Tip: Keeping discretionary expenses under 80% will boost your health score.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Recurring Bills preview
        if (recurringBills.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "Recurring Bills & Subscriptions")
                Spacer(modifier = Modifier.height(6.dp))
            }
            items(recurringBills.take(3)) { bill ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 3.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CategoryIconBadge(category = bill.category, size = 34)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(bill.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Due day ${bill.dueDayOfMonth} of month", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(Formatters.formatCurrency(bill.amount), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                if (bill.isPaidThisMonth) "Paid ✓" else "Pending",
                                fontSize = 10.sp,
                                color = if (bill.isPaidThisMonth) IncomeGreen else WarningOrange,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // 3. Savings Goals Spotlight
        if (goals.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(18.dp))
                SectionHeader(
                    title = "Savings Goals",
                    actionText = "See All (${goals.size})",
                    onActionClick = onNavigateToGoals
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(goals) { goal ->
                        SavingsGoalMiniCard(goal = goal, onClick = { onGoalClick(goal) })
                    }
                }
            }
        }

        // 4. Monthly Category Budgets
        item {
            Spacer(modifier = Modifier.height(20.dp))
            SectionHeader(
                title = "Monthly Category Budgets",
                actionText = "Manage Budgets",
                onActionClick = onNavigateToBudgets
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (budgets.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "No category budgets set yet.",
                    actionText = "Set Category Limits",
                    onAction = onNavigateToBudgets
                )
            }
        } else {
            val topBudgets = budgets.take(5)
            items(topBudgets) { budget ->
                val spent = summary.categorySpending[budget.categoryName] ?: 0.0
                CategoryBudgetRow(
                    budget = budget,
                    spent = spent,
                    onClick = onNavigateToBudgets
                )
            }
        }

        // 5. Recent Transactions
        item {
            Spacer(modifier = Modifier.height(20.dp))
            SectionHeader(
                title = "Recent Transactions",
                actionText = "View All (${transactions.size})",
                onActionClick = onNavigateToTransactions
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (transactions.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "No transactions recorded for this month.",
                    actionText = "Add First Transaction",
                    onAction = onAddExpenseClick
                )
            }
        } else {
            val recent = transactions.take(6)
            items(recent) { transaction ->
                RecentTransactionItem(
                    transaction = transaction,
                    onClick = onNavigateToTransactions
                )
            }
        }
    }
}

@Composable
fun MonthSelectorHeader(
    year: Int,
    month: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onPrev,
                modifier = Modifier.size(36.dp).testTag("prev_month_button")
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous month"
                )
            }
            Text(
                text = Formatters.formatMonthYear(year, month),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToday() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            IconButton(
                onClick = onNext,
                modifier = Modifier.size(36.dp).testTag("next_month_button")
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next month"
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable { onToday() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Today",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MonthlyOverviewHero(
    totalIncome: Double,
    totalExpenses: Double,
    netBalance: Double,
    totalBudget: Double,
    remainingBudget: Double,
    budgetUsedFraction: Float,
    onAddExpenseClick: () -> Unit,
    onAddIncomeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("monthly_overview_hero"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Net Balance Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Net Monthly Cash Flow",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                val statusText = when {
                    budgetUsedFraction > 1.0f -> "Over Budget"
                    budgetUsedFraction > 0.85f -> "Near Limit"
                    else -> "On Track"
                }
                val statusColor = when {
                    budgetUsedFraction > 1.0f -> ExpenseRed
                    budgetUsedFraction > 0.85f -> WarningOrange
                    else -> IncomeGreen
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusColor.copy(alpha = 0.25f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Net amount
            Text(
                text = Formatters.formatCurrency(netBalance),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Income & Expense breakdown pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Income card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.10f))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(IncomeGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = IncomeGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Income", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            Text(
                                Formatters.formatCurrency(totalIncome),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Expense card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.10f))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ExpenseRed.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = ExpenseRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Spent", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            Text(
                                Formatters.formatCurrency(totalExpenses),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Overall Budget Progress Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Monthly Budget Allowance",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${Formatters.formatCurrency(totalExpenses)} / ${Formatters.formatCurrency(totalBudget)}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { budgetUsedFraction.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (budgetUsedFraction > 1.0f) ExpenseRed else GoldAccent,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${Formatters.formatCurrency(remainingBudget)} remaining",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Add Buttons inside hero
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ExpenseRed)
                        .clickable { onAddExpenseClick() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Expense", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(IncomeGreen)
                        .clickable { onAddIncomeClick() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Income", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SavingsGoalMiniCard(
    goal: SavingsGoalEntity,
    onClick: () -> Unit
) {
    val percent = (goal.progressFraction * 100).toInt()
    Card(
        modifier = Modifier
            .width(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryIconBadge(category = goal.categoryIcon, size = 34)
                Text(
                    text = "$percent%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = EmeraldPrimary
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = goal.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${Formatters.formatCurrency(goal.currentAmount)} of ${Formatters.formatCurrency(goal.targetAmount)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { goal.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = EmeraldPrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun CategoryBudgetRow(
    budget: CategoryBudgetEntity,
    spent: Double,
    onClick: () -> Unit
) {
    val fraction = if (budget.monthlyLimit > 0) (spent / budget.monthlyLimit).toFloat() else 0f
    val isOver = spent > budget.monthlyLimit

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryIconBadge(category = budget.categoryName, size = 38)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = budget.categoryName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${Formatters.formatCurrency(spent)} spent of ${Formatters.formatCurrency(budget.monthlyLimit)} limit",
                        fontSize = 12.sp,
                        color = if (isOver) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${(fraction * 100).toInt()}%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isOver) ExpenseRed else MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { fraction.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = when {
                    isOver -> ExpenseRed
                    fraction > 0.8f -> WarningOrange
                    else -> EmeraldPrimary
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun RecentTransactionItem(
    transaction: TransactionEntity,
    onClick: () -> Unit
) {
    val isIncome = transaction.type.equals(TransactionType.INCOME.name, ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIconBadge(category = transaction.category, size = 42)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transaction.category,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " • ${Formatters.formatDate(transaction.dateMillis)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isIncome) "+" else "-"}${Formatters.formatCurrency(transaction.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isIncome) IncomeGreen else ExpenseRed
                )
                Spacer(modifier = Modifier.height(2.dp))
                TransactionTypeBadge(type = transaction.type)
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    message: String,
    actionText: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onAction) {
                Text(actionText, fontWeight = FontWeight.Bold)
            }
        }
    }
}
