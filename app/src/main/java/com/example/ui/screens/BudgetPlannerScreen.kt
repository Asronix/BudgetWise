package com.example.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CategoryBudgetEntity
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.Formatters
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.WarningOrange
import com.example.ui.viewmodel.BudgetViewModel

@Composable
fun BudgetPlannerScreen(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val monthYear by viewModel.currentMonthYear.collectAsStateWithLifecycle()
    val budgets by viewModel.categoryBudgets.collectAsStateWithLifecycle()
    val summary by viewModel.monthSummary.collectAsStateWithLifecycle()

    var editingBudget by remember { mutableStateOf<CategoryBudgetEntity?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    // Dialog for editing existing category limit
    if (editingBudget != null) {
        val b = editingBudget!!
        var limitText by remember { mutableStateOf(b.monthlyLimit.toInt().toString()) }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { editingBudget = null },
            title = { Text("Edit ${b.categoryName} Budget") },
            text = {
                Column {
                    Text(
                        "Set monthly spending allowance for this category:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = limitText,
                        onValueChange = {
                            if (it.all { c -> c.isDigit() || c == '.' }) {
                                limitText = it
                                errorMsg = null
                            }
                        },
                        label = { Text("Monthly Limit") },
                        prefix = { Text("$", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_budget_limit_input")
                    )
                    if (errorMsg != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(errorMsg!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = limitText.toDoubleOrNull()
                        if (parsed == null || parsed < 0) {
                            errorMsg = "Enter a valid amount"
                            return@Button
                        }
                        viewModel.updateCategoryBudget(b.categoryName, parsed)
                        editingBudget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingBudget = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog for adding a new custom category
    if (showAddCategoryDialog) {
        var categoryName by remember { mutableStateOf("") }
        var limitText by remember { mutableStateOf("100") }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Add Category Budget") },
            text = {
                Column {
                    OutlinedTextField(
                        value = categoryName,
                        onValueChange = {
                            categoryName = it
                            errorMsg = null
                        },
                        label = { Text("Category Name") },
                        placeholder = { Text("e.g. Subscriptions, Pet Care") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_category_name_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = limitText,
                        onValueChange = {
                            if (it.all { c -> c.isDigit() || c == '.' }) {
                                limitText = it
                                errorMsg = null
                            }
                        },
                        label = { Text("Monthly Limit") },
                        prefix = { Text("$", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_category_limit_input")
                    )
                    if (errorMsg != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(errorMsg!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (categoryName.isBlank()) {
                            errorMsg = "Category name cannot be empty"
                            return@Button
                        }
                        val parsed = limitText.toDoubleOrNull()
                        if (parsed == null || parsed < 0) {
                            errorMsg = "Enter a valid amount"
                            return@Button
                        }
                        viewModel.updateCategoryBudget(categoryName.trim(), parsed)
                        showAddCategoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("budget_planner_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Month Selector Header
        item {
            MonthSelectorHeader(
                year = monthYear.year,
                month = monthYear.month,
                onPrev = { viewModel.previousMonth() },
                onNext = { viewModel.nextMonth() },
                onToday = { viewModel.resetToCurrentMonth() }
            )
        }

        // Budget Overview Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "Total Monthly Spending Cap",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        Formatters.formatCurrency(summary.totalBudgetCap),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${Formatters.formatCurrency(summary.totalExpenses)} spent",
                            color = if (summary.totalExpenses > summary.totalBudgetCap) ExpenseRed else GoldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "${Formatters.formatCurrency(summary.remainingBudget)} remaining",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { summary.budgetUsedFraction.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (summary.budgetUsedFraction > 1.0f) ExpenseRed else GoldAccent,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                }
            }
        }

        // Section Title + Add Category Button
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category Limits (${budgets.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { showAddCategoryDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Category", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Budget Items
        items(budgets) { budget ->
            val spent = summary.categorySpending[budget.categoryName] ?: 0.0
            val fraction = if (budget.monthlyLimit > 0) (spent / budget.monthlyLimit).toFloat() else 0f
            val isOver = spent > budget.monthlyLimit
            val remaining = (budget.monthlyLimit - spent).coerceAtLeast(0.0)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 5.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { editingBudget = budget }
                    .testTag("budget_category_${budget.categoryName}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CategoryIconBadge(category = budget.categoryName, size = 42)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = budget.categoryName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isOver) {
                                    "Over by ${Formatters.formatCurrency(spent - budget.monthlyLimit)}"
                                } else {
                                    "${Formatters.formatCurrency(remaining)} left"
                                },
                                fontSize = 12.sp,
                                color = if (isOver) ExpenseRed else IncomeGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = Formatters.formatCurrency(budget.monthlyLimit),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit budget",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "${Formatters.formatCurrency(spent)} spent (${(fraction * 100).toInt()}%)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { fraction.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = when {
                            isOver -> ExpenseRed
                            fraction > 0.85f -> WarningOrange
                            else -> EmeraldPrimary
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}
