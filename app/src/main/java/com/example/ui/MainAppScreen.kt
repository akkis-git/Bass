package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.DevPurple
import com.example.ui.theme.DevPurpleContainer

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Dashboard", Icons.Default.Home)
    object Mark : Screen("mark_attendance", "Mark Attendance", Icons.Default.Fingerprint)
    object History : Screen("history", "Logs", Icons.Default.History)
    object DevOptions : Screen("dev_options", "Dev Options", Icons.Default.DeveloperMode)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: AttendanceViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val unsyncedCount by viewModel.unsyncedCount.collectAsState()
    var showAboutDialog by remember { mutableStateOf(false) }

    val items = listOf(
        Screen.Home,
        Screen.Mark,
        Screen.History,
        Screen.DevOptions
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AadhaarBAS Dev",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showAboutDialog = true },
                        modifier = Modifier.testTag("about_app_button")
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "About App")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                items.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            if (screen == Screen.History && unsyncedCount > 0) {
                                BadgedBox(
                                    badge = { Badge { Text(unsyncedCount.toString()) } }
                                ) {
                                    Icon(imageVector = screen.icon, contentDescription = screen.title)
                                }
                            } else {
                                Icon(imageVector = screen.icon, contentDescription = screen.title)
                            }
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (screen == Screen.DevOptions) DevPurple else MaterialTheme.colorScheme.primary,
                            selectedTextColor = if (screen == Screen.DevOptions) DevPurple else MaterialTheme.colorScheme.primary,
                            indicatorColor = if (screen == Screen.DevOptions) DevPurpleContainer else MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToMarkAttendance = { navController.navigate(Screen.Mark.route) },
                    onNavigateToDevSettings = { navController.navigate(Screen.DevOptions.route) }
                )
            }
            composable(Screen.Mark.route) {
                MarkAttendanceScreen(viewModel = viewModel)
            }
            composable(Screen.History.route) {
                AttendanceHistoryScreen(viewModel = viewModel)
            }
            composable(Screen.DevOptions.route) {
                DeveloperOptionsScreen(viewModel = viewModel)
            }
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About AadhaarBAS Dev Edition", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Aadhaar Based Attendance System (AadhaarBAS) Developer Edition.\n\n" +
                            "This application permits marking biometric attendance with Developer Options and Mock GPS location enabled for testing and development environments.\n\n" +
                            "Version: 3.2.0-DEV\n" +
                            "National Informatics Centre / MeitY"
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}
