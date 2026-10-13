package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.Formatters
import com.example.ui.theme.EmeraldPrimary
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        targetAmount: Double,
        initialDeposit: Double,
        targetDateMillis: Long,
        categoryIcon: String,
        colorHex: String,
        notes: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetAmountText by remember { mutableStateOf("") }
    var initialDepositText by remember { mutableStateOf("") }

    val defaultTargetDate = remember {
        Calendar.getInstance().apply { add(Calendar.MONTH, 6) }.timeInMillis
    }
    var selectedTargetDate by remember { mutableLongStateOf(defaultTargetDate) }
    var selectedIconName by remember { mutableStateOf("savings") }
    var selectedColorHex by remember { mutableStateOf("#F59E0B") }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val iconOptions = listOf(
        Triple("savings", Icons.Default.Savings, "Piggy Bank"),
        Triple("shield", Icons.Default.Security, "Emergency"),
        Triple("flight", Icons.Default.Flight, "Vacation"),
        Triple("laptop", Icons.Default.Laptop, "Gadget"),
        Triple("home", Icons.Default.Home, "House"),
        Triple("car", Icons.Default.DirectionsCar, "Vehicle"),
        Triple("shopping", Icons.Default.LocalMall, "Shopping")
    )

    val colorOptions = listOf(
        "#F59E0B", "#0F766E", "#3B82F6", "#8B5CF6", "#EC4899", "#10B981"
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedTargetDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedTargetDate = it }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("add_goal_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Savings Goal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close dialog")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Goal Title
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text("Goal Name") },
                    placeholder = { Text("e.g. Emergency Fund, Summer Trip") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_title_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Target Amount
                OutlinedTextField(
                    value = targetAmountText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' }) {
                            targetAmountText = input
                            errorMessage = null
                        }
                    },
                    label = { Text("Target Amount") },
                    prefix = { Text(Formatters.activeCurrencySymbol, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                    placeholder = { Text("5000.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_target_amount_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Initial Deposit
                OutlinedTextField(
                    value = initialDepositText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' }) {
                            initialDepositText = input
                        }
                    },
                    label = { Text("Initial Deposit (optional)") },
                    prefix = { Text(Formatters.activeCurrencySymbol, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Target Date Picker
                Text(
                    text = "Target Date",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = "Target date",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = Formatters.formatDate(selectedTargetDate),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "Change",
                        color = EmeraldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Icon Chooser
                Text(
                    text = "Icon",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(iconOptions) { (id, icon, label) ->
                        val isSelected = selectedIconName == id
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) EmeraldPrimary.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) EmeraldPrimary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedIconName = id },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Color Chooser
                Text(
                    text = "Color Accent",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(colorOptions) { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        val isSelected = selectedColorHex == hex
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hex }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Purpose (optional)") },
                    placeholder = { Text("Why are you saving for this?") },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Create Goal Button
                Button(
                    onClick = {
                        val parsedTarget = targetAmountText.toDoubleOrNull()
                        if (parsedTarget == null || parsedTarget <= 0) {
                            errorMessage = "Please enter a valid target amount greater than 0"
                            return@Button
                        }
                        if (title.isBlank()) {
                            errorMessage = "Please enter a goal name"
                            return@Button
                        }
                        val initialDeposit = initialDepositText.toDoubleOrNull() ?: 0.0

                        onSave(
                            title,
                            parsedTarget,
                            initialDeposit,
                            selectedTargetDate,
                            selectedIconName,
                            selectedColorHex,
                            notes
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_goal_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Create Goal", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
