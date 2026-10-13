package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IncomeGreen
import com.example.ui.viewmodel.BudgetViewModel
import kotlinx.coroutines.launch

@Composable
fun ImportExportDialog(
    viewModel: BudgetViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isExportTab by remember { mutableStateOf(true) }
    var selectedFormat by remember { mutableStateOf("JSON") } // "JSON" or "CSV"

    var exportContent by remember { mutableStateOf("Generating export...") }
    var importInputText by remember { mutableStateOf("") }
    var importStatusMessage by remember { mutableStateOf<String?>(null) }
    var isImportSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(selectedFormat, isExportTab) {
        if (isExportTab) {
            exportContent = if (selectedFormat == "JSON") {
                viewModel.exportDataJson()
            } else {
                viewModel.exportDataCsv()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("import_export_dialog"),
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
                        text = "Data Backup & Migration",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Switcher (Export vs Import)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isExportTab) EmeraldPrimary else Color.Transparent)
                            .clickable { isExportTab = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = null,
                                tint = if (isExportTab) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Export Data",
                                color = if (isExportTab) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isExportTab) EmeraldPrimary else Color.Transparent)
                            .clickable { isExportTab = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Upload,
                                contentDescription = null,
                                tint = if (!isExportTab) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Import Data",
                                color = if (!isExportTab) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isExportTab) {
                    // Export Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Choose Export Format:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("JSON", "CSV").forEach { fmt ->
                                val isSelected = selectedFormat == fmt
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { selectedFormat = fmt }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = fmt,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = exportContent,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Export Payload ($selectedFormat)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .testTag("export_text_area"),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("BudgetWise Backup", exportContent)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "$selectedFormat backup copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("copy_export_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy $selectedFormat to Clipboard", fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Import Section
                    Text(
                        text = "Paste a JSON backup or CSV spreadsheet to restore your records into the database:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = importInputText,
                        onValueChange = {
                            importInputText = it
                            importStatusMessage = null
                        },
                        placeholder = { Text("Paste JSON or CSV data here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .testTag("import_text_area"),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (importStatusMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = importStatusMessage!!,
                            color = if (isImportSuccess) IncomeGreen else MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (importInputText.isBlank()) {
                                importStatusMessage = "Please paste data to import"
                                isImportSuccess = false
                                return@Button
                            }

                            coroutineScope.launch {
                                val trimmed = importInputText.trim()
                                if (trimmed.startsWith("{")) {
                                    val ok = viewModel.importDataJson(trimmed)
                                    if (ok) {
                                        isImportSuccess = true
                                        importStatusMessage = "JSON backup successfully imported and restored!"
                                        Toast.makeText(context, "Data restored successfully!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        isImportSuccess = false
                                        importStatusMessage = "Invalid JSON backup format"
                                    }
                                } else {
                                    val count = viewModel.importDataCsv(trimmed)
                                    if (count > 0) {
                                        isImportSuccess = true
                                        importStatusMessage = "Successfully imported $count transactions from CSV!"
                                        Toast.makeText(context, "Imported $count transactions!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        isImportSuccess = false
                                        importStatusMessage = "No valid records recognized in CSV"
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("execute_import_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Import & Restore Data", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
