package com.example.ui

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BiometricAttendanceRecord
import com.example.ui.theme.DevPurple
import com.example.ui.theme.DevPurpleContainer
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessContainer
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.StatusWarningContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AttendanceHistoryScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val records by viewModel.allRecords.collectAsState()
    val unsyncedCount by viewModel.unsyncedCount.collectAsState()

    var selectedRecordForDetail by remember { mutableStateOf<BiometricAttendanceRecord?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // History Actions & Sync Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Attendance Records (${records.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$unsyncedCount pending cloud sync",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (unsyncedCount > 0) StatusWarning else StatusSuccess
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (unsyncedCount > 0) {
                        Button(
                            onClick = { viewModel.syncAllRecords() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("sync_all_button")
                        ) {
                            Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Sync All", fontSize = 12.sp)
                        }
                    }

                    if (records.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { viewModel.clearAllHistory() },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("clear_history_button")
                        ) {
                            Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Attendance Records Found",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Mark attendance to record biometric logs in Room Database.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(records) { record ->
                    HistoryCardItem(
                        record = record,
                        onClick = { selectedRecordForDetail = record }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    // Detail Modal Dialog
    selectedRecordForDetail?.let { record ->
        RecordDetailDialog(
            record = record,
            onDismiss = { selectedRecordForDetail = null },
            onSyncClick = {
                viewModel.syncRecord(record.id)
                selectedRecordForDetail = null
            }
        )
    }
}

@Composable
fun HistoryCardItem(
    record: BiometricAttendanceRecord,
    onClick: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.US).format(Date(record.timestamp))

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("history_item_${record.id}"),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (record.status.contains("DEV") || record.status.contains("MOCK")) DevPurpleContainer
                        else StatusSuccessContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (record.status.contains("DEV")) Icons.Default.DeveloperMode else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (record.status.contains("DEV")) DevPurple else StatusSuccess,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${record.employeeName ?: record.employeeId}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${record.biometricType} • Vendor: ${record.deviceVendor}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = if (record.isSynced) StatusSuccessContainer else StatusWarningContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (record.isSynced) "SYNCED" else "PENDING",
                        color = if (record.isSynced) StatusSuccess else StatusWarning,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                if (record.status.contains("MOCK") || record.status.contains("DEV")) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "DEV MOCK",
                        color = DevPurple,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun RecordDetailDialog(
    record: BiometricAttendanceRecord,
    onDismiss: () -> Unit,
    onSyncClick: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMMM yyyy 'at' hh:mm:ss a", Locale.US).format(Date(record.timestamp))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Attendance Record #${record.id}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Employee ID: ${record.employeeId}")
                Text(text = "Name: ${record.employeeName ?: "N/A"}")
                Text(text = "Time: $dateStr")
                Text(text = "Biometric Mode: ${record.biometricType}")
                Text(text = "Device Vendor: ${record.deviceVendor}")
                Text(text = "Device Serial: ${record.deviceId}")
                Text(text = "Latitude: ${record.latitude}")
                Text(text = "Longitude: ${record.longitude}")
                Text(text = "Sync Status: ${if (record.isSynced) "SUCCESS" else "PENDING_SYNC"}")

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Captured PID XML Block:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    color = Color(0xFF1E1E1E),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = record.pidBlock ?: "No PID Payload",
                        color = Color(0xFF80CBC4),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            if (!record.isSynced) {
                Button(onClick = onSyncClick) {
                    Text("SYNC RECORD NOW")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("CLOSE")
                }
            }
        },
        dismissButton = {
            if (!record.isSynced) {
                TextButton(onClick = onDismiss) {
                    Text("CLOSE")
                }
            }
        }
    )
}
