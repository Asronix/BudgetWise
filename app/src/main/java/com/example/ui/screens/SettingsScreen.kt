package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.preferences.AppThemeMode
import com.example.data.preferences.CurrencyOption
import com.example.data.preferences.SupportedCurrencies
import com.example.ui.components.Formatters
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IncomeGreen
import com.example.ui.viewmodel.BudgetViewModel

@Composable
fun SettingsScreen(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val currentCurrency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val allGoals by viewModel.savingsGoals.collectAsStateWithLifecycle()

    val morningEnabled by viewModel.morningReminderEnabled.collectAsStateWithLifecycle()
    val morningTime by viewModel.morningReminderTime.collectAsStateWithLifecycle()
    val eveningEnabled by viewModel.eveningReminderEnabled.collectAsStateWithLifecycle()
    val eveningTime by viewModel.eveningReminderTime.collectAsStateWithLifecycle()

    // Accordion expansion states
    var isThemeExpanded by remember { mutableStateOf(false) }
    var isCurrencyExpanded by remember { mutableStateOf(false) }
    var isNotificationExpanded by remember { mutableStateOf(true) }
    var isAboutExpanded by remember { mutableStateOf(true) }
    var isDataExpanded by remember { mutableStateOf(false) }

    var showMorningTimeDialog by remember { mutableStateOf(false) }
    var showEveningTimeDialog by remember { mutableStateOf(false) }
    var testNotificationSentBanner by remember { mutableStateOf(false) }

    var showResetDialog by remember { mutableStateOf(false) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    var showImportExportDialog by remember { mutableStateOf(false) }

    if (showImportExportDialog) {
        ImportExportDialog(
            viewModel = viewModel,
            onDismiss = { showImportExportDialog = false }
        )
    }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Clear All Records?") },
            text = { Text("Are you sure you want to delete all transactions, budgets, and savings goals? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllRecords()
                    showClearAllDialog = false
                }) {
                    Text("Delete All", color = ExpenseRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showMorningTimeDialog) {
        ReminderTimePickerDialog(
            title = "Morning Reminder Time",
            initialHour = morningTime.first,
            initialMinute = morningTime.second,
            onDismiss = { showMorningTimeDialog = false },
            onConfirm = { h, m ->
                viewModel.setMorningReminderTime(h, m)
                showMorningTimeDialog = false
            }
        )
    }

    if (showEveningTimeDialog) {
        ReminderTimePickerDialog(
            title = "Evening Reminder Time",
            initialHour = eveningTime.first,
            initialMinute = eveningTime.second,
            onDismiss = { showEveningTimeDialog = false },
            onConfirm = { h, m ->
                viewModel.setEveningReminderTime(h, m)
                showEveningTimeDialog = false
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Title Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldDark)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Preferences & App Info",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Customize theme, currency, and explore app details",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Accordion 1: Theme Mode (Dark / Light)
        item {
            AccordionCard(
                title = "Appearance & Theme",
                subtitle = themeMode.title,
                icon = Icons.Default.Palette,
                isExpanded = isThemeExpanded,
                onToggle = { isThemeExpanded = !isThemeExpanded },
                testTag = "accordion_theme"
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Choose your preferred visual style:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ThemeOptionRow(
                        title = "Light Mode",
                        description = "Crisp white background with emerald accents",
                        icon = Icons.Default.LightMode,
                        isSelected = themeMode == AppThemeMode.LIGHT,
                        onClick = { viewModel.setThemeMode(AppThemeMode.LIGHT) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ThemeOptionRow(
                        title = "Dark Mode",
                        description = "Deep slate theme designed for nighttime comfort",
                        icon = Icons.Default.DarkMode,
                        isSelected = themeMode == AppThemeMode.DARK,
                        onClick = { viewModel.setThemeMode(AppThemeMode.DARK) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ThemeOptionRow(
                        title = "System Default",
                        description = "Matches your Android device's system appearance",
                        icon = Icons.Default.SettingsBrightness,
                        isSelected = themeMode == AppThemeMode.SYSTEM,
                        onClick = { viewModel.setThemeMode(AppThemeMode.SYSTEM) }
                    )
                }
            }
        }

        // Accordion 2: Currency Changer
        item {
            AccordionCard(
                title = "Currency Changer",
                subtitle = "${currentCurrency.flag} ${currentCurrency.code} (${currentCurrency.symbol})",
                icon = Icons.Default.AttachMoney,
                isExpanded = isCurrencyExpanded,
                onToggle = { isCurrencyExpanded = !isCurrencyExpanded },
                testTag = "accordion_currency"
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Select your preferred currency symbol. Formats update across all screens instantly.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Live preview badge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Live Sample: ${Formatters.formatCurrency(1250.00, currentCurrency.symbol)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Currency grid
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SupportedCurrencies.chunked(2).forEach { rowCurrencies ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowCurrencies.forEach { currency ->
                                    val isSelected = currentCurrency.code == currency.code
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            )
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { viewModel.setCurrency(currency) }
                                            .padding(horizontal = 12.dp, vertical = 10.dp)
                                            .testTag("currency_option_${currency.code}")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(currency.flag, fontSize = 20.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = currency.code,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                                Text(
                                                    text = currency.symbol,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = EmeraldPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Accordion: Daily Transaction Reminders (Start of Day & End of Day)
        item {
            val morningFormatted = String.format("%02d:%02d %s",
                if (morningTime.first == 0) 12 else if (morningTime.first > 12) morningTime.first - 12 else morningTime.first,
                morningTime.second,
                if (morningTime.first < 12) "AM" else "PM"
            )
            val eveningFormatted = String.format("%02d:%02d %s",
                if (eveningTime.first == 0) 12 else if (eveningTime.first > 12) eveningTime.first - 12 else eveningTime.first,
                eveningTime.second,
                if (eveningTime.first < 12) "AM" else "PM"
            )

            val summarySubtitle = when {
                morningEnabled && eveningEnabled -> "Morning ($morningFormatted) & Evening ($eveningFormatted)"
                morningEnabled -> "Morning only ($morningFormatted)"
                eveningEnabled -> "Evening only ($eveningFormatted)"
                else -> "Reminders paused"
            }

            AccordionCard(
                title = "Daily Transaction Reminders",
                subtitle = summarySubtitle,
                icon = Icons.Default.NotificationsActive,
                isExpanded = isNotificationExpanded,
                onToggle = { isNotificationExpanded = !isNotificationExpanded },
                testTag = "accordion_notifications"
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Automatic alerts to remind you to log income & expenses so your budget is always accurate:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Morning Reminder Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(GoldAccent.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.WbSunny,
                                            contentDescription = "Morning",
                                            tint = GoldAccent,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Start of Day (Morning)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Plan daily budget & morning expenses",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Switch(
                                    checked = morningEnabled,
                                    onCheckedChange = { viewModel.setMorningReminderEnabled(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = EmeraldPrimary
                                    ),
                                    modifier = Modifier.testTag("switch_morning_reminder")
                                )
                            }

                            if (morningEnabled) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .clickable { showMorningTimeDialog = true }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Schedule,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = EmeraldPrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Reminder Time", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(EmeraldPrimary.copy(alpha = 0.12f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = morningFormatted,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = EmeraldPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Evening Reminder Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldDark.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.NightsStay,
                                            contentDescription = "Evening",
                                            tint = EmeraldDark,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "End of Day (Evening)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Wrap up & review today's receipts",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Switch(
                                    checked = eveningEnabled,
                                    onCheckedChange = { viewModel.setEveningReminderEnabled(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = EmeraldPrimary
                                    ),
                                    modifier = Modifier.testTag("switch_evening_reminder")
                                )
                            }

                            if (eveningEnabled) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .clickable { showEveningTimeDialog = true }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Schedule,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = EmeraldPrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Reminder Time", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(EmeraldPrimary.copy(alpha = 0.12f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = eveningFormatted,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = EmeraldPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Test Reminder Button
                    Button(
                        onClick = {
                            viewModel.triggerTestNotification()
                            testNotificationSentBanner = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_test_notification")
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Test Notification Now", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    if (testNotificationSentBanner) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ Test notification dispatched! Check your notification shade.",
                            color = IncomeGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Accordion: About & Developer Credits (Jerry Espanueva)
        item {
            AccordionCard(
                title = "About BudgetWise",
                subtitle = "Made by Jerry Espanueva",
                icon = Icons.Default.Info,
                isExpanded = isAboutExpanded,
                onToggle = { isAboutExpanded = !isAboutExpanded },
                testTag = "accordion_about"
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Developer spotlight card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Developer",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Jerry Espanueva",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Creator & Software Engineer",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = EmeraldPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Designed & built this application for effortless budgeting and smart savings.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "App Overview & Mission",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "BudgetWise is a modern personal finance application built to help you track monthly cash flows, manage category budgets, and systematically conquer your savings goals.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Application Version", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("1.0.0 (Release)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Architecture", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Kotlin • Jetpack Compose • M3", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Persistence", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Local Room SQLite • Coroutines Flow", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Data Privacy", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("100% On-Device & Private", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = IncomeGreen)
                    }
                }
            }
        }

        // Accordion 4: Data & Local Storage
        item {
            AccordionCard(
                title = "Data & Storage",
                subtitle = "${allTransactions.size} transactions, ${allGoals.size} goals",
                icon = Icons.Default.Storage,
                isExpanded = isDataExpanded,
                onToggle = { isDataExpanded = !isDataExpanded },
                testTag = "accordion_data"
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Database Status", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("On-device SQLite database active", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(IncomeGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Active", color = IncomeGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Import / Export Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable { showImportExportDialog = true }
                            .padding(12.dp)
                            .testTag("settings_import_export_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ImportExport,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Import & Export (JSON / CSV)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Backup all data or restore from spreadsheet/JSON", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Refresh sample data
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable { showResetDialog = true }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Verify / Refresh Sample Data", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Check database tables and verify default categories", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Clear All Records Button (Danger)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.08f))
                            .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                            .clickable { showClearAllDialog = true }
                            .padding(12.dp)
                            .testTag("settings_clear_all_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Clear All Records", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                            Text("Erase all transactions, budgets, and goals permanently", fontSize = 11.sp, color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccordionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    testTag: String,
    content: @Composable () -> Unit
) {
    val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "arrowRotation")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Clickable Accordion Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotationAngle)
                )
            }

            // Expandable Content Section
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(14.dp))
                    content()
                }
            }
        }
    }
}

@Composable
fun ThemeOptionRow(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Selected",
                tint = EmeraldPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ReminderTimePickerDialog(
    title: String,
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select hour and minute for this daily reminder:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Time display badge
                val formattedDisplay = String.format("%02d:%02d %s",
                    if (selectedHour == 0) 12 else if (selectedHour > 12) selectedHour - 12 else selectedHour,
                    selectedMinute,
                    if (selectedHour < 12) "AM" else "PM"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldPrimary.copy(alpha = 0.12f))
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formattedDisplay,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldPrimary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Preset popular times
                Text("Quick Select:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        Triple(7, 0, "7:00 AM"),
                        Triple(8, 30, "8:30 AM"),
                        Triple(19, 0, "7:00 PM"),
                        Triple(20, 0, "8:00 PM"),
                        Triple(21, 30, "9:30 PM")
                    ).forEach { (h, m, label) ->
                        val isSelected = selectedHour == h && selectedMinute == m
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable {
                                    selectedHour = h
                                    selectedMinute = m
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hour adjustment row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Hour (0-23):", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { if (selectedHour > 0) selectedHour-- else selectedHour = 23 },
                            modifier = Modifier.size(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = String.format("%02d", selectedHour),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.width(30.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { if (selectedHour < 23) selectedHour++ else selectedHour = 0 },
                            modifier = Modifier.size(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Minute adjustment row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Minute (0-59):", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = {
                                selectedMinute = if (selectedMinute >= 5) selectedMinute - 5 else 55
                            },
                            modifier = Modifier.size(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("-5", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = String.format("%02d", selectedMinute),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.width(30.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                selectedMinute = if (selectedMinute <= 50) selectedMinute + 5 else 0
                            },
                            modifier = Modifier.size(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("+5", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedHour, selectedMinute) },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("Save Time", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
