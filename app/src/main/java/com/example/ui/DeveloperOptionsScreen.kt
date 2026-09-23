package com.example.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DevPurple
import com.example.ui.theme.DevPurpleContainer
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessContainer

@Composable
fun DeveloperOptionsScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val devSettings by viewModel.devSettings.collectAsState()

    var latText by remember(devSettings.customLatitude) { mutableStateOf(devSettings.customLatitude.toString()) }
    var lngText by remember(devSettings.customLongitude) { mutableStateOf(devSettings.customLongitude.toString()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Header Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DevPurpleContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(DevPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeveloperMode,
                            contentDescription = "Developer Mode",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Developer Options Control Center",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DevPurple
                        )
                        Text(
                            text = "Configure mock GPS overrides, bypass geofence/Wi-Fi checks, and test biometric RD services.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DevPurple.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        // 1. Master Developer Options Toggles
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Developer Policies & Spoofing Settings",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Toggle 1: Allow Dev Options
                    DevSettingSwitchRow(
                        title = "Allow Developer Options Mode",
                        subtitle = "Bypasses standard 'Please disable Developer options' block",
                        checked = devSettings.isDevOptionsAllowed,
                        onCheckedChange = { viewModel.setDevOptionsAllowed(it) },
                        testTag = "dev_options_master_switch"
                    )

                    Divider(modifier = Modifier.padding(vertical = 10.dp))

                    // Toggle 2: Allow Mock GPS
                    DevSettingSwitchRow(
                        title = "Allow Mock / Spoofed Location",
                        subtitle = "Permits isFromMockProvider & fake location apps",
                        checked = devSettings.isMockGpsAllowed,
                        onCheckedChange = { viewModel.setMockGpsAllowed(it) },
                        testTag = "mock_gps_switch"
                    )

                    Divider(modifier = Modifier.padding(vertical = 10.dp))

                    // Toggle 3: Bypass Geofence Range Check
                    DevSettingSwitchRow(
                        title = "Bypass Office Geofence Range",
                        subtitle = "Allows attendance marking regardless of distance from entry point",
                        checked = devSettings.isGeofenceBypassed,
                        onCheckedChange = { viewModel.setGeofenceBypassed(it) },
                        testTag = "geofence_bypass_switch"
                    )

                    Divider(modifier = Modifier.padding(vertical = 10.dp))

                    // Toggle 4: Bypass NICNET Wi-Fi
                    DevSettingSwitchRow(
                        title = "Bypass NICNET / Office Wi-Fi Check",
                        subtitle = "Allows marking attendance on cellular or home network",
                        checked = devSettings.isWifiCheckBypassed,
                        onCheckedChange = { viewModel.setWifiCheckBypassed(it) },
                        testTag = "wifi_bypass_switch"
                    )
                }
            }
        }

        // 2. Custom Location & Mock GPS Coordinate Override
        item {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.GpsFixed, contentDescription = null, tint = DevPurple)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Mock Location / GPS Override",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Switch(
                            checked = devSettings.isCustomGpsOverrideEnabled,
                            onCheckedChange = { viewModel.setCustomGpsOverride(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = DevPurple
                            ),
                            modifier = Modifier.testTag("custom_gps_override_switch")
                        )
                    }

                    Text(
                        text = "Inject custom GPS coordinates directly into attendance payload.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    if (devSettings.isCustomGpsOverrideEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = latText,
                                onValueChange = {
                                    latText = it
                                    it.toDoubleOrNull()?.let { lat ->
                                        lngText.toDoubleOrNull()?.let { lng ->
                                            viewModel.updateCustomCoordinates(lat, lng)
                                        }
                                    }
                                },
                                label = { Text("Latitude") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("latitude_input_field")
                            )

                            OutlinedTextField(
                                value = lngText,
                                onValueChange = {
                                    lngText = it
                                    it.toDoubleOrNull()?.let { lng ->
                                        latText.toDoubleOrNull()?.let { lat ->
                                            viewModel.updateCustomCoordinates(lat, lng)
                                        }
                                    }
                                },
                                label = { Text("Longitude") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("longitude_input_field")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Quick Office Presets:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    latText = "28.6139"
                                    lngText = "77.2090"
                                    viewModel.updateCustomCoordinates(28.613912, 77.209021)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Delhi", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    latText = "18.9220"
                                    lngText = "72.8347"
                                    viewModel.updateCustomCoordinates(18.9220, 72.8347)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Mumbai", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    latText = "12.9716"
                                    lngText = "77.5946"
                                    viewModel.updateCustomCoordinates(12.9716, 77.5946)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Bengaluru", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // 3. System Diagnostics & Security Inspection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Device Security & Diagnostic Flags",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    DiagnosticRow("Developer Options Enabled:", if (devSettings.isDevOptionsAllowed) "YES (Bypassed)" else "NO")
                    DiagnosticRow("Mock Location Provider:", if (devSettings.isMockGpsAllowed) "ALLOWED" else "BLOCKED")
                    DiagnosticRow("ADB USB Debugging:", "ACTIVE")
                    DiagnosticRow("Aadhaar BAS Client Version:", "v3.2.0-DEV")
                    DiagnosticRow("Virtual IP Address:", "192.168.1.105 (NICNET)")
                    DiagnosticRow("Registered Mac:", "02:00:00:00:00:00")
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun DevSettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = DevPurple
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (value.contains("YES") || value.contains("ALLOWED") || value.contains("ACTIVE")) StatusSuccess else MaterialTheme.colorScheme.onSurface
        )
    }
}
