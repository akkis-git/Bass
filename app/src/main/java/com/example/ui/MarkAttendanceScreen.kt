package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BiometricAttendanceRecord
import com.example.model.BiometricMode
import com.example.model.DEFAULT_ENTRY_POINTS
import com.example.model.DeviceVendor
import com.example.ui.theme.DevPurple
import com.example.ui.theme.DevPurpleContainer
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkAttendanceScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val devSettings by viewModel.devSettings.collectAsState()
    val captureState by viewModel.captureState.collectAsState()

    var vendorExpanded by remember { mutableStateOf(false) }
    var entryPointExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Developer Mode Active Header Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DevPurpleContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DeveloperMode,
                        contentDescription = null,
                        tint = DevPurple,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Developer Options & Mock GPS Active",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = DevPurple
                        )
                        Text(
                            text = "Standard spoofing restrictions bypassed. You can mark attendance with test location/devices.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DevPurple.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        // 1. Biometric Mode Selection
        item {
            Column {
                Text(
                    text = "Select Biometric Authentication Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(BiometricMode.values()) { mode ->
                        val selected = devSettings.activeBiometricMode == mode
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.setBiometricMode(mode) },
                            label = { Text(text = mode.label) },
                            leadingIcon = {
                                val icon = when (mode) {
                                    BiometricMode.FINGERPRINT -> Icons.Default.Fingerprint
                                    BiometricMode.IRIS -> Icons.Default.RemoveRedEye
                                    BiometricMode.FACE_RD -> Icons.Default.Face
                                    BiometricMode.RFID -> Icons.Default.CreditCard
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("mode_chip_${mode.name.lowercase()}")
                        )
                    }
                }
            }
        }

        // 2. Device Vendor & RD Service Config
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Registered Biometric RD Device Vendor",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = vendorExpanded,
                        onExpandedChange = { vendorExpanded = !vendorExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = "${devSettings.selectedVendor.vendorName} (${devSettings.selectedVendor.model})",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vendorExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("vendor_dropdown"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = vendorExpanded,
                            onDismissRequest = { vendorExpanded = false }
                        ) {
                            DeviceVendor.values().forEach { vendor ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(text = vendor.vendorName, fontWeight = FontWeight.Bold)
                                            Text(text = vendor.model, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                    },
                                    onClick = {
                                        viewModel.setDeviceVendor(vendor)
                                        vendorExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Office Entry Point Dropdown
                    Text(
                        text = "Office Entry Point",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = entryPointExpanded,
                        onExpandedChange = { entryPointExpanded = !entryPointExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = devSettings.selectedEntryPoint.name,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = entryPointExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("entry_point_dropdown"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = entryPointExpanded,
                            onDismissRequest = { entryPointExpanded = false }
                        ) {
                            DEFAULT_ENTRY_POINTS.forEach { ep ->
                                DropdownMenuItem(
                                    text = { Text(text = ep.name) },
                                    onClick = {
                                        viewModel.setOfficeEntryPoint(ep)
                                        entryPointExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Aadhaar Consent Checkbox
        item {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = devSettings.consentAccepted,
                        onCheckedChange = { viewModel.setConsentAccepted(it) },
                        modifier = Modifier.testTag("aadhaar_consent_checkbox"),
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "I give my consent for my biometric Aadhaar authentication to mark attendance.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // 4. Capture Trigger Button
        item {
            Button(
                onClick = { viewModel.triggerBiometricCapture() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("scan_biometric_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val modeIcon = when (devSettings.activeBiometricMode) {
                        BiometricMode.FINGERPRINT -> Icons.Default.Fingerprint
                        BiometricMode.IRIS -> Icons.Default.RemoveRedEye
                        BiometricMode.FACE_RD -> Icons.Default.Face
                        BiometricMode.RFID -> Icons.Default.CreditCard
                    }
                    Icon(imageVector = modeIcon, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "CAPTURE ${devSettings.activeBiometricMode.label.uppercase()} & MARK",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    // Biometric Scanning / Result Dialogs
    when (val state = captureState) {
        is CaptureState.Scanning -> {
            BiometricScanDialog(
                progress = state.progress,
                statusMessage = state.statusMessage,
                mode = devSettings.activeBiometricMode
            )
        }
        is CaptureState.Success -> {
            CaptureSuccessDialog(
                record = state.record,
                qualityScore = state.qualityScore,
                isMockUsed = state.isMockLocationUsed,
                onDismiss = { viewModel.resetCaptureState() }
            )
        }
        is CaptureState.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetCaptureState() },
                title = { Text(text = "Capture / Location Error") },
                text = { Text(text = state.message) },
                confirmButton = {
                    TextButton(onClick = { viewModel.resetCaptureState() }) {
                        Text("OK")
                    }
                }
            )
        }
        CaptureState.Idle -> { /* Do nothing */ }
    }
}

@Composable
fun BiometricScanDialog(
    progress: Float,
    statusMessage: String,
    mode: BiometricMode
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    AlertDialog(
        onDismissRequest = {},
        confirmButton = {},
        title = {
            Text(
                text = "Capturing Biometrics (${mode.label})",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when (mode) {
                        BiometricMode.FINGERPRINT -> Icons.Default.Fingerprint
                        BiometricMode.IRIS -> Icons.Default.RemoveRedEye
                        BiometricMode.FACE_RD -> Icons.Default.Face
                        BiometricMode.RFID -> Icons.Default.CreditCard
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}

@Composable
fun CaptureSuccessDialog(
    record: BiometricAttendanceRecord,
    qualityScore: Int,
    isMockUsed: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = StatusSuccess,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Attendance Marked!", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = StatusSuccessContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Biometric Match Quality: $qualityScore% • Status: ${record.status}",
                        color = StatusSuccess,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                if (isMockUsed) {
                    Surface(
                        color = DevPurpleContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeveloperMode,
                                contentDescription = null,
                                tint = DevPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Developer Option / Mock GPS Mode Allowed",
                                color = DevPurple,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Text(text = "Employee: ${record.employeeName ?: record.employeeId}")
                Text(text = "Device Serial: ${record.deviceId}")
                Text(text = "Vendor: ${record.deviceVendor} (${record.biometricType})")
                Text(text = "Coordinates: ${record.latitude?.let { String.format("%.4f", it) }}, ${record.longitude?.let { String.format("%.4f", it) }}")

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Encrypted PID Block Preview:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    color = Color.DarkGray,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = record.pidBlock?.take(160) ?: "PID Captured",
                        color = Color.Green,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("DONE")
            }
        }
    )
}
