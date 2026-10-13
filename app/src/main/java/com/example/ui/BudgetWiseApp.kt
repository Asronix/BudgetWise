package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.TransactionType
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.PieChart
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.AddGoalDialog
import com.example.ui.screens.AddTransactionDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BudgetPlannerScreen
import com.example.ui.screens.ContributeGoalDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SavingsGoalsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.BudgetViewModel

enum class AppScreen(val title: String, val icon: ImageVector) {
    DASHBOARD("Overview", Icons.Default.Dashboard),
    TRANSACTIONS("Transactions", Icons.Default.ReceiptLong),
    BUDGETS("Budgets", Icons.Default.AccountBalanceWallet),
    GOALS("Goals", Icons.Default.Savings),
    REPORTS("Reports", Icons.Default.BarChart),
    ANALYTICS("Analytics", Icons.Default.PieChart),
    SETTINGS("Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetWiseApp(
    viewModel: BudgetViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }

    // Dialog state
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var initialTransactionType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var selectedGoalForContribution by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    // Dialogs
    if (showAddTransactionDialog) {
        AddTransactionDialog(
            initialType = initialTransactionType,
            onDismiss = { showAddTransactionDialog = false },
            onSave = { title, amount, type, category, dateMillis, paymentMethod, notes ->
                viewModel.addTransaction(
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    dateMillis = dateMillis,
                    paymentMethod = paymentMethod,
                    notes = notes
                )
                showAddTransactionDialog = false
            }
        )
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onSave = { title, targetAmount, initialDeposit, targetDateMillis, categoryIcon, colorHex, notes ->
                viewModel.addSavingsGoal(
                    title = title,
                    targetAmount = targetAmount,
                    initialDeposit = initialDeposit,
                    targetDateMillis = targetDateMillis,
                    categoryIcon = categoryIcon,
                    colorHex = colorHex,
                    notes = notes
                )
                showAddGoalDialog = false
            }
        )
    }

    if (selectedGoalForContribution != null) {
        ContributeGoalDialog(
            goal = selectedGoalForContribution!!,
            onDismiss = { selectedGoalForContribution = null },
            onDeposit = { amount, note, deductFromBudget ->
                viewModel.depositToSavingsGoal(
                    goal = selectedGoalForContribution!!,
                    amount = amount,
                    note = note,
                    deductFromMonthlyBudget = deductFromBudget
                )
                selectedGoalForContribution = null
            },
            onWithdraw = { amount, note ->
                viewModel.withdrawFromSavingsGoal(
                    goal = selectedGoalForContribution!!,
                    amount = amount,
                    note = note
                )
                selectedGoalForContribution = null
            }
        )
    }

    BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
        currentScreen = AppScreen.DASHBOARD
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.budget_app_icon_1791863827366),
                            contentDescription = "App logo",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "BudgetWise",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            currentScreen = if (currentScreen == AppScreen.ANALYTICS) AppScreen.DASHBOARD else AppScreen.ANALYTICS
                        },
                        modifier = Modifier.testTag("top_analytics_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = "Analytics",
                            tint = if (currentScreen == AppScreen.ANALYTICS) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { currentScreen = AppScreen.SETTINGS },
                        modifier = Modifier.testTag("top_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings and About",
                            tint = if (currentScreen == AppScreen.SETTINGS) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                val navScreens = listOf(
                    AppScreen.DASHBOARD,
                    AppScreen.TRANSACTIONS,
                    AppScreen.BUDGETS,
                    AppScreen.GOALS,
                    AppScreen.REPORTS,
                    AppScreen.SETTINGS
                )
                navScreens.forEach { screen ->
                    val selected = currentScreen == screen
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 10.sp,
                                maxLines = 1,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldPrimary,
                            selectedTextColor = EmeraldPrimary,
                            indicatorColor = EmeraldPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentScreen != AppScreen.GOALS && currentScreen != AppScreen.SETTINGS && currentScreen != AppScreen.REPORTS) {
                FloatingActionButton(
                    onClick = {
                        initialTransactionType = TransactionType.EXPENSE
                        showAddTransactionDialog = true
                    },
                    containerColor = EmeraldPrimary,
                    contentColor = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.testTag("global_add_transaction_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToTransactions = { currentScreen = AppScreen.TRANSACTIONS },
                    onNavigateToBudgets = { currentScreen = AppScreen.BUDGETS },
                    onNavigateToGoals = { currentScreen = AppScreen.GOALS },
                    onNavigateToReports = { currentScreen = AppScreen.REPORTS },
                    onAddExpenseClick = {
                        initialTransactionType = TransactionType.EXPENSE
                        showAddTransactionDialog = true
                    },
                    onAddIncomeClick = {
                        initialTransactionType = TransactionType.INCOME
                        showAddTransactionDialog = true
                    },
                    onGoalClick = { goal ->
                        selectedGoalForContribution = goal
                    }
                )

                AppScreen.TRANSACTIONS -> TransactionsScreen(
                    viewModel = viewModel
                )

                AppScreen.BUDGETS -> BudgetPlannerScreen(
                    viewModel = viewModel
                )

                AppScreen.GOALS -> SavingsGoalsScreen(
                    viewModel = viewModel,
                    onAddGoalClick = { showAddGoalDialog = true },
                    onContributeClick = { goal -> selectedGoalForContribution = goal }
                )

                AppScreen.REPORTS -> ReportsScreen(
                    viewModel = viewModel
                )

                AppScreen.ANALYTICS -> AnalyticsScreen(
                    viewModel = viewModel
                )

                AppScreen.SETTINGS -> SettingsScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}
