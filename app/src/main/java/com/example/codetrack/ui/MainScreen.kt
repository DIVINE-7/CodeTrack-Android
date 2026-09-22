package com.example.codetrack.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.codetrack.ui.components.CodeTrackTopBar
import com.example.codetrack.ui.models.UiProblem
import com.example.codetrack.ui.screens.HomeScreen
import com.example.codetrack.ui.screens.ProblemDetailScreen
import com.example.codetrack.ui.screens.ProblemsScreen
import com.example.codetrack.ui.screens.ProgressScreen
import com.example.codetrack.ui.screens.WelcomeScreen

sealed class NavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : NavItem("home", Icons.Filled.Home, "Home")
    object Problems : NavItem("problems", Icons.AutoMirrored.Filled.List, "Problems")
    object Revision : NavItem("revision", Icons.Filled.Book, "Revision")
    object Progress : NavItem("progress", Icons.Filled.Analytics, "Progress")
    object Settings : NavItem("settings", Icons.Filled.Settings, "Settings")
}

@Composable
fun MainScreen() {
    var showWelcomeScreen by remember { mutableStateOf(true) }
    var selectedItem by remember { mutableIntStateOf(0) }
    var selectedDetailProblem by remember { mutableStateOf<UiProblem?>(null) }

    val items = listOf(
        NavItem.Home,
        NavItem.Problems,
        NavItem.Revision,
        NavItem.Progress,
        NavItem.Settings
    )

    if (showWelcomeScreen) {
        WelcomeScreen(
            onGetStarted = {
                showWelcomeScreen = false
                selectedItem = 0 // Navigate to Home
            }
        )
    } else {
        Scaffold(
            topBar = {
                // Show standard TopBar unless viewing Problem Detail (which has its own back top bar)
                if (selectedItem != 1 || selectedDetailProblem == null) {
                    CodeTrackTopBar()
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = selectedItem == index,
                            onClick = {
                                selectedItem = index
                                selectedDetailProblem = null // Reset detail view when changing tab
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier.padding(
                    if (selectedItem == 1 && selectedDetailProblem != null) PaddingValues(0.dp) else innerPadding
                ),
                color = MaterialTheme.colorScheme.background
            ) {
                when (selectedItem) {
                    0 -> HomeScreen()
                    1 -> {
                        val currentProblem = selectedDetailProblem
                        if (currentProblem == null) {
                            ProblemsScreen(
                                onProblemClick = { problem ->
                                    selectedDetailProblem = problem
                                }
                            )
                        } else {
                            ProblemDetailScreen(
                                problem = currentProblem,
                                onBackClick = { selectedDetailProblem = null },
                                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                            )
                        }
                    }
                    3 -> ProgressScreen()
                    else -> PlaceholderScreen(items[selectedItem].label)
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "$name Screen coming soon", style = MaterialTheme.typography.headlineSmall)
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    com.example.codetrack.ui.theme.CodeTrackTheme {
        MainScreen()
    }
}
