package navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import com.example.codetrack.ProductivityScreen
import com.example.codetrack.ProductivityViewModel
import com.example.codetrack.data.model.RevisionCategory
import com.example.codetrack.data.repository.DsaQuestionBank
import com.example.codetrack.ui.screens.CodingPracticeScreen
import com.example.codetrack.DsaPracticeScreen
import com.example.codetrack.ui.screens.ProblemDetailScreen
import ui.screens.DashboardScreen

@Composable
fun AppNavigation(
    navController: NavHostController
) {
    val productivityViewModel = remember {
        ProductivityViewModel()
    }

    val revisions by productivityViewModel.revisions.collectAsState()

    var problemsState by remember {
        mutableStateOf(DsaQuestionBank.getSampleProblems())
    }

    var selectedProblemId by remember {
        mutableStateOf<Int?>(null)
    }

    val currentProblem = problemsState.find {
        it.id == selectedProblemId
    }

    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {

        // DASHBOARD
        composable("dashboard") {
            DashboardScreen(
                onDsaClick = {
                    navController.navigate("dsa")
                },
                onProductivityClick = {
                    navController.navigate("productivity")
                },
                onProgressClick = {
                    navController.navigate("progress")
                }
            )
        }

        // PRODUCTIVITY
        composable("productivity") {
            ProductivityScreen(
                viewModel = productivityViewModel,
                onNavigateToDsa = {
                    navController.navigate("dsa")
                }
            )
        }

        // DSA PRACTICE
        composable("dsa") {
            DsaPracticeScreen(
                problems = problemsState,

                onBack = {
                    navController.popBackStack()
                },

                onPracticeProblem = { problem ->
                    selectedProblemId = problem.id
                    navController.navigate("problem")
                }
            )
        }

        // PROBLEM DETAIL
        composable("problem") {

            currentProblem?.let { problem ->

                val isInRevision = revisions.any {
                    it.problemId == problem.id &&
                            it.category == RevisionCategory.DSA
                }

                ProblemDetailScreen(
                    problem = problem,

                    onBack = {
                        navController.popBackStack()
                    },

                    onToggleSolved = { id ->
                        problemsState = problemsState.map {
                            if (it.id == id) {
                                it.copy(isSolved = !it.isSolved)
                            } else {
                                it
                            }
                        }
                    },

                    onPrevious = {
                        val index =
                            problemsState.indexOfFirst {
                                it.id == selectedProblemId
                            }

                        if (index > 0) {
                            selectedProblemId =
                                problemsState[index - 1].id
                        }
                    },

                    onNext = {
                        val index =
                            problemsState.indexOfFirst {
                                it.id == selectedProblemId
                            }

                        if (
                            index >= 0 &&
                            index < problemsState.size - 1
                        ) {
                            selectedProblemId =
                                problemsState[index + 1].id
                        }
                    },

                    onNavigateToCodingPractice = {
                        navController.navigate("coding")
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
            }
        }

        // CODING PRACTICE
        composable("coding") {

            currentProblem?.let { problem ->

                CodingPracticeScreen(
                    problem = problem,

                    onBack = {
                        navController.popBackStack()
                    },

                    onSubmitSuccess = { id ->
                        problemsState = problemsState.map {
                            if (it.id == id) {
                                it.copy(isSolved = true)
                            } else {
                                it
                            }
                        }
                    }
                )
            }
        }

        // TEMPORARY PROGRESS
        composable("progress") {
            androidx.compose.material3.Text(
                text = "Progress"
            )
        }
    }
}