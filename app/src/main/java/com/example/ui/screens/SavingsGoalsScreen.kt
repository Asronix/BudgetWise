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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SavingsGoalEntity
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.Formatters
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IncomeGreen
import com.example.ui.viewmodel.BudgetViewModel
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

@Composable
fun SavingsGoalsScreen(
    viewModel: BudgetViewModel,
    onAddGoalClick: () -> Unit,
    onContributeClick: (SavingsGoalEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val goals by viewModel.savingsGoals.collectAsStateWithLifecycle()

    var goalToDelete by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    val totalSaved = goals.sumOf { it.currentAmount }
    val totalTarget = goals.sumOf { it.targetAmount }
    val overallProgress = if (totalTarget > 0) (totalSaved / totalTarget).toFloat() else 0f

    if (goalToDelete != null) {
        val g = goalToDelete!!
        AlertDialog(
            onDismissRequest = { goalToDelete = null },
            title = { Text("Delete Savings Goal") },
            text = { Text("Are you sure you want to delete \"${g.title}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSavingsGoal(g.id)
                    goalToDelete = null
                }) {
                    Text("Delete", color = ExpenseRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { goalToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("savings_goals_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Savings Header Summary Hero
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("savings_summary_hero"),
                shape = RoundedCornerShape(24.dp),
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
                            text = "Total Accumulated Savings",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(GoldAccent.copy(alpha = 0.25f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${(overallProgress * 100).toInt()}% Total Goal",
                                color = GoldAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = Formatters.formatCurrency(totalSaved),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Combined Target: ${Formatters.formatCurrency(totalTarget)}",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${goals.count { it.isCompleted }} of ${goals.size} Completed",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { overallProgress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = GoldAccent,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                }
            }
        }

        // Section header with Create Goal button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Goals (${goals.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onAddGoalClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    modifier = Modifier.testTag("create_goal_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Goal", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // List of Savings Goals
        if (goals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Savings,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "No savings goals created yet",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Create targets like Emergency Fund, Vacation, or New Car to stay motivated!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(goals) { goal ->
                SavingsGoalDetailCard(
                    goal = goal,
                    onContribute = { onContributeClick(goal) },
                    onDelete = { goalToDelete = goal }
                )
            }
        }
    }
}

@Composable
fun SavingsGoalDetailCard(
    goal: SavingsGoalEntity,
    onContribute: () -> Unit,
    onDelete: () -> Unit
) {
    val percent = (goal.progressFraction * 100).toInt()
    val now = System.currentTimeMillis()
    val millisRemaining = (goal.targetDateMillis - now).coerceAtLeast(0L)
    val daysRemaining = TimeUnit.MILLISECONDS.toDays(millisRemaining)
    val monthsRemaining = (daysRemaining / 30.0).coerceAtLeast(1.0)
    val neededAmount = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
    val monthlyPace = if (!goal.isCompleted) neededAmount / monthsRemaining else 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .testTag("savings_goal_card_${goal.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryIconBadge(category = goal.categoryIcon, size = 46)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = goal.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        if (goal.isCompleted) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Completed",
                                tint = IncomeGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (goal.isCompleted) "Target Achieved!"
                        else "Target: ${Formatters.formatDate(goal.targetDateMillis)} ($daysRemaining days left)",
                        fontSize = 12.sp,
                        color = if (goal.isCompleted) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete goal",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Current vs Target figures
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("Saved So Far", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        Formatters.formatCurrency(goal.currentAmount),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = EmeraldPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Target Amount", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        Formatters.formatCurrency(goal.targetAmount),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { goal.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (goal.isCompleted) IncomeGreen else EmeraldPrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom info & Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!goal.isCompleted && monthlyPace > 0) {
                    Text(
                        text = "${Formatters.formatCurrency(monthlyPace)}/mo pace",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                } else if (goal.isCompleted) {
                    Text(
                        text = "100% Complete 🎉",
                        fontSize = 12.sp,
                        color = IncomeGreen,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = onContribute,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Deposit / Withdraw",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
