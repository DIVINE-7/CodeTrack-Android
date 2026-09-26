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

import com.example.codetrack.ProductivityViewModel
import com.example.codetrack.data.model.DsaProblem
import com.example.codetrack.data.model.RevisionCategory
import com.example.codetrack.data.repository.DsaQuestionBank
import com.example.codetrack.ui.components.CodeTrackTopBar
import com.example.codetrack.ui.models.UiProblem
import com.example.codetrack.ui.screens.HomeScreen
import com.example.codetrack.ui.screens.ProblemDetailScreen
import com.example.codetrack.ui.screens.ProblemsScreen
import com.example.codetrack.ui.screens.WelcomeScreen

sealed class NavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Home : NavItem(
        "home",
        Icons.Filled.Home,
        "Home"
    )

    object Problems : NavItem(
        "problems",
        Icons.AutoMirrored.Filled.List,
        "Problems"
    )

    object Revision : NavItem(
        "revision",
        Icons.Filled.Book,
        "Revision"
    )

    object Progress : NavItem(
        "progress",
        Icons.Filled.Analytics,
        "Progress"
    )

    object Settings : NavItem(
        "settings",
        Icons.Filled.Settings,
        "Settings"
    )
}

@Composable
fun MainScreen() {

    var showWelcomeScreen by remember {
        mutableStateOf(true)
    }

    var selectedItem by remember {
        mutableIntStateOf(0)
    }

    var selectedProblem by remember {
        mutableStateOf<DsaProblem?>(null)
    }

    val productivityViewModel = remember {
        ProductivityViewModel()
    }

    val revisions by productivityViewModel.revisions.collectAsState()

    val problems = remember {
        DsaQuestionBank.getSampleProblems()
    }

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
                selectedItem = 0
            }
        )

    } else {

        Scaffold(

            topBar = {

                if (selectedProblem == null) {
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

                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label
                                )
                            },

                            label = {
                                Text(item.label)
                            },

                            selected = selectedItem == index,

                            onClick = {
                                selectedItem = index
                                selectedProblem = null
                            },

                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor =
                                    MaterialTheme.colorScheme.primary,

                                selectedTextColor =
                                    MaterialTheme.colorScheme.primary,

                                unselectedIconColor =
                                    MaterialTheme.colorScheme.onSurfaceVariant,

                                unselectedTextColor =
                                    MaterialTheme.colorScheme.onSurfaceVariant,

                                indicatorColor =
                                    MaterialTheme.colorScheme.primary.copy(
                                        alpha = 0.1f
                                    )
                            )
                        )
                    }
                }
            }

        ) { innerPadding ->

            Surface(
                modifier = Modifier.padding(
                    if (selectedProblem != null) {
                        PaddingValues(0.dp)
                    } else {
                        innerPadding
                    }
                ),

                color = MaterialTheme.colorScheme.background
            ) {

                if (selectedProblem != null) {

                    val currentProblem = selectedProblem!!

                    val isInRevision = revisions.any {
                        it.problemId == currentProblem.id &&
                                it.category == RevisionCategory.DSA
                    }

                    ProblemDetailScreen(

                        problem = currentProblem,

                        onBack = {
                            selectedProblem = null
                        },

                        onToggleSolved = { id ->

                            val index = problems.indexOfFirst {
                                it.id == id
                            }

                            if (index >= 0) {

                                val updated =
                                    problems[index].copy(
                                        isSolved =
                                            !problems[index].isSolved
                                    )

                                selectedProblem = updated
                            }
                        },

                        onPrevious = {

                            val index = problems.indexOfFirst {
                                it.id == currentProblem.id
                            }

                            if (index > 0) {
                                selectedProblem =
                                    problems[index - 1]
                            }
                        },

                        onNext = {

                            val index = problems.indexOfFirst {
                                it.id == currentProblem.id
                            }

                            if (
                                index >= 0 &&
                                index < problems.size - 1
                            ) {
                                selectedProblem =
                                    problems[index + 1]
                            }
                        },

                        onNavigateToCodingPractice = {
                            // Coding Practice will be connected
                            // after the main UI is compiling.
                        },

                        onAddToRevision = { dsaProblem ->

                            productivityViewModel.addRevision(
                                problemId = dsaProblem.id,
                                title = dsaProblem.title,
                                category = RevisionCategory.DSA,
                                topic = dsaProblem.topic
                            )
                        },

                        onRemoveFromRevision = { dsaProblem ->

                            productivityViewModel.removeRevisionByProblemId(
                                problemId = dsaProblem.id,
                                category = RevisionCategory.DSA
                            )
                        },

                        isInRevision = isInRevision
                    )

                } else {

                    when (selectedItem) {

                        // HOME
                        0 -> {
                            HomeScreen()
                        }

                        // PROBLEMS
                        1 -> {

                            ProblemsScreen(

                                onProblemClick = { uiProblem: UiProblem ->

                                    /*
                                     * Friend's ProblemsScreen uses UiProblem.
                                     *
                                     * Our real integration uses DsaProblem.
                                     * Match the UI problem with the real
                                     * DsaQuestionBank problem by title.
                                     */

                                    val matchingProblem =
                                        problems.firstOrNull {
                                            it.title.equals(
                                                uiProblem.title,
                                                ignoreCase = true
                                            )
                                        }

                                    if (matchingProblem != null) {
                                        selectedProblem =
                                            matchingProblem
                                    }
                                }
                            )
                        }

                        // REVISION
                        2 -> {

                            PlaceholderScreen(
                                "Revision"
                            )
                        }

                        // PROGRESS
                        3 -> {

                            PlaceholderScreen(
                                "Progress"
                            )
                        }

                        // SETTINGS
                        4 -> {

                            PlaceholderScreen(
                                "Settings"
                            )
                        }
                    }
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

        Text(
            text = "$name Screen coming soon",
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true
)
@Composable
fun MainScreenPreview() {

    com.example.codetrack.ui.theme.CodeTrackTheme {
        MainScreen()
    }
}