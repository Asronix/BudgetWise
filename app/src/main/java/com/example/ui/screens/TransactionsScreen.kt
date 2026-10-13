package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.Formatters
import com.example.ui.components.TransactionTypeBadge
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.viewmodel.BudgetViewModel

@Composable
fun TransactionsScreen(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val monthYear by viewModel.currentMonthYear.collectAsStateWithLifecycle()
    val monthlyTransactions by viewModel.monthlyTransactions.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchFilterTarget by viewModel.searchFilterTarget.collectAsStateWithLifecycle() // "ALL", "MERCHANT", "NOTES"
    val searchScope by viewModel.searchScope.collectAsStateWithLifecycle() // "THIS_MONTH", "ALL_TIME"
    val typeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle()

    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    // Source list depends on whether user is browsing selected month or searching all time
    val baseTransactions = if (searchScope == "ALL_TIME") allTransactions else monthlyTransactions

    val query = searchQuery.trim()

    val filteredTransactions = baseTransactions.filter { t ->
        val matchesQuery = if (query.isEmpty()) {
            true
        } else {
            when (searchFilterTarget) {
                "MERCHANT" -> t.title.contains(query, ignoreCase = true)
                "NOTES" -> t.notes.contains(query, ignoreCase = true)
                else -> { // "ALL"
                    t.title.contains(query, ignoreCase = true) ||
                            t.notes.contains(query, ignoreCase = true) ||
                            t.category.contains(query, ignoreCase = true)
                }
            }
        }

        val matchesType = when (typeFilter) {
            "EXPENSE" -> t.type.equals(TransactionType.EXPENSE.name, ignoreCase = true)
            "INCOME" -> t.type.equals(TransactionType.INCOME.name, ignoreCase = true)
            else -> true
        }

        val matchesCategory = categoryFilter == null || t.category.equals(categoryFilter, ignoreCase = true)

        matchesQuery && matchesType && matchesCategory
    }

    // Check if matches exist across all time when this month has no hits
    val allTimeMatchesCount = if (query.isNotEmpty() && searchScope == "THIS_MONTH") {
        allTransactions.count { t ->
            val mQuery = when (searchFilterTarget) {
                "MERCHANT" -> t.title.contains(query, ignoreCase = true)
                "NOTES" -> t.notes.contains(query, ignoreCase = true)
                else -> t.title.contains(query, ignoreCase = true) ||
                        t.notes.contains(query, ignoreCase = true) ||
                        t.category.contains(query, ignoreCase = true)
            }
            val mType = when (typeFilter) {
                "EXPENSE" -> t.type.equals(TransactionType.EXPENSE.name, ignoreCase = true)
                "INCOME" -> t.type.equals(TransactionType.INCOME.name, ignoreCase = true)
                else -> true
            }
            val mCat = categoryFilter == null || t.category.equals(categoryFilter, ignoreCase = true)
            mQuery && mType && mCat
        }
    } else 0

    if (transactionToDelete != null) {
        val t = transactionToDelete!!
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete \"${t.title}\" (${Formatters.formatCurrency(t.amount)})?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTransaction(t.id)
                    transactionToDelete = null
                }) {
                    Text("Delete", color = ExpenseRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("transactions_screen")
    ) {
        // Date / Scope Header
        if (searchScope == "THIS_MONTH") {
            MonthSelectorHeader(
                year = monthYear.year,
                month = monthYear.month,
                onPrev = { viewModel.previousMonth() },
                onNext = { viewModel.nextMonth() },
                onToday = { viewModel.resetToCurrentMonth() }
            )
        } else {
            // All Time Header Bar
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "All Time Transaction History",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${allTransactions.size} total lifetime records",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.setSearchScope("THIS_MONTH") },
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = "Back to ${monthYear.monthName.take(3)}",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Search Bar (Filtered by Merchant Name or Notes)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = {
                Text(
                    text = when (searchFilterTarget) {
                        "MERCHANT" -> "Search by merchant name..."
                        "NOTES" -> "Search by transaction notes..."
                        else -> "Search by merchant name or notes..."
                    },
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search transactions",
                    tint = if (searchQuery.isNotEmpty()) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.setSearchQuery("") },
                        modifier = Modifier.testTag("clear_search_button")
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search query")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("search_transactions_input"),
            shape = RoundedCornerShape(16.dp)
        )

        // Filter Target & Scope Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Target Selector: All, Merchant Only, Notes Only
            item {
                FilterChip(
                    selected = searchFilterTarget == "ALL",
                    onClick = { viewModel.setSearchFilterTarget("ALL") },
                    label = { Text("All Fields") },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("filter_target_all")
                )
            }
            item {
                FilterChip(
                    selected = searchFilterTarget == "MERCHANT",
                    onClick = { viewModel.setSearchFilterTarget("MERCHANT") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Storefront,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text("Merchant") },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("filter_target_merchant")
                )
            }
            item {
                FilterChip(
                    selected = searchFilterTarget == "NOTES",
                    onClick = { viewModel.setSearchFilterTarget("NOTES") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Notes,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text("Notes") },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("filter_target_notes")
                )
            }

            // Scope Selector: This Month vs All Time
            item {
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }

            item {
                FilterChip(
                    selected = searchScope == "THIS_MONTH",
                    onClick = { viewModel.setSearchScope("THIS_MONTH") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    label = { Text(monthYear.monthName.take(3) + " " + monthYear.year) },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("scope_this_month")
                )
            }

            item {
                FilterChip(
                    selected = searchScope == "ALL_TIME",
                    onClick = { viewModel.setSearchScope("ALL_TIME") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    label = { Text("All Time") },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("scope_all_time")
                )
            }
        }

        // Secondary Row: Type Chips (All, Expense, Income) & Categories
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = typeFilter == "ALL",
                    onClick = { viewModel.setTypeFilter("ALL") },
                    label = { Text("All Types") },
                    shape = RoundedCornerShape(20.dp)
                )
            }
            item {
                FilterChip(
                    selected = typeFilter == "EXPENSE",
                    onClick = { viewModel.setTypeFilter("EXPENSE") },
                    label = { Text("Expenses") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ExpenseRed.copy(alpha = 0.15f),
                        selectedLabelColor = ExpenseRed
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
            item {
                FilterChip(
                    selected = typeFilter == "INCOME",
                    onClick = { viewModel.setTypeFilter("INCOME") },
                    label = { Text("Income") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IncomeGreen.copy(alpha = 0.15f),
                        selectedLabelColor = IncomeGreen
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }

            // Distinct categories chips in active scope
            val categoriesInScope = baseTransactions.map { it.category }.distinct()
            items(categoriesInScope) { cat ->
                val isSelected = categoryFilter == cat
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) viewModel.setCategoryFilter(null)
                        else viewModel.setCategoryFilter(cat)
                    },
                    label = { Text(cat) },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        // Search Results Summary Banner (shown when search is active or filtered)
        if (query.isNotEmpty() || categoryFilter != null || typeFilter != "ALL" || searchScope == "ALL_TIME") {
            val totalExpense = filteredTransactions.filter { it.type.equals("EXPENSE", true) }.sumOf { it.amount }
            val totalIncome = filteredTransactions.filter { it.type.equals("INCOME", true) }.sumOf { it.amount }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (query.isNotEmpty()) {
                                "Found ${filteredTransactions.size} matching \"$query\""
                            } else {
                                "${filteredTransactions.size} transactions filtered"
                            },
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (totalExpense > 0) {
                                Text(
                                    text = "Exp: -${Formatters.formatCurrency(totalExpense)}  ",
                                    fontSize = 11.sp,
                                    color = ExpenseRed,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (totalIncome > 0) {
                                Text(
                                    text = "Inc: +${Formatters.formatCurrency(totalIncome)}",
                                    fontSize = 11.sp,
                                    color = IncomeGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    TextButton(
                        onClick = { viewModel.clearSearchFilters() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Reset", fontSize = 12.sp, color = EmeraldPrimary)
                    }
                }
            }
        }

        // Transactions List or Empty State
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (query.isNotEmpty()) Icons.Default.SearchOff else Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (query.isNotEmpty()) {
                            "No transactions found matching \"$query\""
                        } else {
                            if (searchScope == "THIS_MONTH") "No transactions for ${monthYear.monthName} ${monthYear.year}"
                            else "No transactions recorded yet"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (query.isNotEmpty()) {
                            when (searchFilterTarget) {
                                "MERCHANT" -> "Checked against merchant / title records."
                                "NOTES" -> "Checked against transaction notes."
                                else -> "Checked against merchant names, notes, and categories."
                            }
                        } else {
                            "Tap the + button below to add your first transaction"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Cross-month search suggestion
                    if (query.isNotEmpty() && searchScope == "THIS_MONTH" && allTimeMatchesCount > 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.setSearchScope("ALL_TIME") },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Search All Records ($allTimeMatchesCount found)")
                        }
                    } else if (query.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { viewModel.setSearchQuery("") },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Clear Search")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = filteredTransactions,
                    key = { it.id }
                ) { transaction ->
                    TransactionListItem(
                        transaction = transaction,
                        searchQuery = query,
                        searchFilterTarget = searchFilterTarget,
                        showFullDate = searchScope == "ALL_TIME",
                        onDelete = { transactionToDelete = transaction }
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionListItem(
    transaction: TransactionEntity,
    searchQuery: String = "",
    searchFilterTarget: String = "ALL",
    showFullDate: Boolean = false,
    onDelete: () -> Unit
) {
    val isIncome = transaction.type.equals(TransactionType.INCOME.name, ignoreCase = true)
    val queryMatchesTitle = searchQuery.isNotBlank() && transaction.title.contains(searchQuery, ignoreCase = true)
    val queryMatchesNotes = searchQuery.isNotBlank() && transaction.notes.contains(searchQuery, ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .testTag("transaction_item_${transaction.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (queryMatchesTitle || queryMatchesNotes) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon
                CategoryIconBadge(category = transaction.category, size = 44)
                Spacer(modifier = Modifier.width(12.dp))

                // Merchant / Title and details
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = transaction.title,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (queryMatchesTitle) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                        )
                        if (queryMatchesTitle && searchFilterTarget != "NOTES") {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = EmeraldPrimary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Merchant Match",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

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
                        if (transaction.paymentMethod.isNotBlank()) {
                            Text(
                                text = " • ${transaction.paymentMethod}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Amount and Action
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (isIncome) "+" else "-"}${Formatters.formatCurrency(transaction.amount)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isIncome) IncomeGreen else ExpenseRed
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TransactionTypeBadge(type = transaction.type)
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete transaction",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Transaction Notes Display (with search highlighting / badge)
            if (transaction.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = if (queryMatchesNotes) {
                        EmeraldPrimary.copy(alpha = 0.12f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = "Note",
                            tint = if (queryMatchesNotes) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = transaction.notes,
                            fontSize = 12.sp,
                            color = if (queryMatchesNotes) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = if (queryMatchesNotes) FontWeight.Medium else FontWeight.Normal,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (queryMatchesNotes) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = EmeraldPrimary,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Notes Match",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
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
