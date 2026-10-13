package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.preferences.AppThemeMode
import com.example.notifications.TransactionReminderScheduler
import com.example.ui.BudgetWiseApp
import com.example.ui.theme.BudgetWiseTheme
import com.example.ui.viewmodel.BudgetViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ensure notification channels are set up and scheduled
        TransactionReminderScheduler.scheduleAllReminders(applicationContext)

        setContent {
            val budgetViewModel: BudgetViewModel = viewModel()
            val themeMode by budgetViewModel.themeMode.collectAsStateWithLifecycle()
            val isDarkTheme = when (themeMode) {
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            // Notification permission request launcher for Android 13+ (TIRAMISU)
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    TransactionReminderScheduler.scheduleAllReminders(applicationContext)
                }
            }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                    if (!hasPermission) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            BudgetWiseTheme(darkTheme = isDarkTheme) {
                BudgetWiseApp(viewModel = budgetViewModel)
            }
        }
    }
}
