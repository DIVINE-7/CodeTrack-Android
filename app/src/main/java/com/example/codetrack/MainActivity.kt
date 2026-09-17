package com.example.codetrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.codetrack.data.repository.DsaQuestionBank
import com.example.codetrack.ui.screens.CodingPracticeScreen
import com.example.codetrack.ui.screens.ProblemDetailScreen
import com.example.codetrack.ui.theme.CodeTrackTheme

enum class Screen {
    Productivity,
    DsaPractice,
    ProblemDetail,
    CodingPractice
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            CodeTrackTheme {
                var currentScreen by remember { mutableStateOf(Screen.Productivity) }
                
                // Question Bank State holding all 1200 problems reactively
                var problemsState by remember {
                    mutableStateOf(DsaQuestionBank.getSampleProblems())
                }
                
                // Track selected problem ID for detail screen view
                var selectedProblemId by remember { mutableStateOf<Int?>(null) }

                val currentProblem = remember(problemsState, selectedProblemId) {
                    problemsState.find { it.id == selectedProblemId }
                }

                when (currentScreen) {
                    Screen.Productivity -> {
                        ProductivityScreen(
                            onNavigateToDsa = {
                                currentScreen = Screen.DsaPractice
                            }
                        )
                    }
                    Screen.DsaPractice -> {
                        DsaPracticeScreen(
                            problems = problemsState,
                            onBack = {
                                currentScreen = Screen.Productivity
                            },
                            onPracticeProblem = { problem ->
                                selectedProblemId = problem.id
                                currentScreen = Screen.ProblemDetail
                            }
                        )
                    }
                    Screen.ProblemDetail -> {
                        currentProblem?.let { problem ->
                            ProblemDetailScreen(
                                problem = problem,
                                onBack = {
                                    currentScreen = Screen.DsaPractice
                                },
                                onToggleSolved = { id ->
                                    problemsState = problemsState.map {
                                        if (it.id == id) it.copy(isSolved = !it.isSolved) else it
                                    }
                                },
                                onPrevious = {
                                    val index = problemsState.indexOfFirst { it.id == selectedProblemId }
                                    if (index > 0) {
                                        selectedProblemId = problemsState[index - 1].id
                                    }
                                },
                                onNext = {
                                    val index = problemsState.indexOfFirst { it.id == selectedProblemId }
                                    if (index >= 0 && index < problemsState.size - 1) {
                                        selectedProblemId = problemsState[index + 1].id
                                    }
                                },
                                onNavigateToCodingPractice = {
                                    currentScreen = Screen.CodingPractice
                                }
                            )
                        } ?: run {
                            currentScreen = Screen.DsaPractice
                        }
                    }
                    Screen.CodingPractice -> {
                        currentProblem?.let { problem ->
                            CodingPracticeScreen(
                                problem = problem,
                                onBack = {
                                    currentScreen = Screen.ProblemDetail
                                },
                                onSubmitSuccess = { id ->
                                    problemsState = problemsState.map {
                                        if (it.id == id) it.copy(isSolved = true) else it
                                    }
                                }
                            )
                        } ?: run {
                            currentScreen = Screen.DsaPractice
                        }
                    }
                }
            }
        }
    }
}
