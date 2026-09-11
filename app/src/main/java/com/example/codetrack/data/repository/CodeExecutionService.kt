package com.example.codetrack.data.repository

import com.example.codetrack.data.model.*
import kotlinx.coroutines.delay

/**
 * Raw execution result from a backend compiler/sandbox.
 */
data class RawExecutionResult(
    val status: ExecutionStatus,
    val stdout: String? = null,
    val stderr: String? = null,
    val compileOutput: String? = null,
    val executionTime: String = "0ms",
    val memory: String = "0MB"
)

/**
 * Abstract interface for code execution.
 * Can be implemented by a Mock service or a Remote API service.
 */
interface CodeExecutionService {
    suspend fun execute(
        language: CodingLanguage,
        sourceCode: String,
        stdin: String
    ): RawExecutionResult
}

/**
 * A Mock implementation that simulates code execution logic
 * based on deterministic markers and solution keywords.
 */
class MockCodeExecutionService(
    private val config: CodingProblemConfig?
) : CodeExecutionService {

    override suspend fun execute(
        language: CodingLanguage,
        sourceCode: String,
        stdin: String
    ): RawExecutionResult {
        delay(500) // Simulate network latency

        val trimmedCode = sourceCode.trim()
        val template = config?.starterCode?.get(language)?.trim() ?: ""

        // 1. DEMO: COMPILATION ERROR
        if (trimmedCode.contains("// MOCK_COMPILE_ERROR")) {
            return RawExecutionResult(
                status = ExecutionStatus.COMPILATION_ERROR,
                compileOutput = "line 1: error: mock syntax error near 'boundary'"
            )
        }

        // 2. DEMO: RUNTIME ERROR
        if (trimmedCode.contains("// MOCK_RUNTIME_ERROR")) {
            return RawExecutionResult(
                status = ExecutionStatus.RUNTIME_ERROR,
                stderr = "java.lang.NullPointerException: mock crash"
            )
        }

        // 3. INCOMPLETE DETECTION
        if (trimmedCode.isEmpty() || trimmedCode == template || !hasMeaningfulChanges(trimmedCode, template)) {
            return RawExecutionResult(
                status = ExecutionStatus.INCOMPLETE
            )
        }

        // 4. MOCK SUCCESS / FAILURE
        // In a real system, the backend would return stdout.
        // Here we simulate it based on whether the mockCorrectSolution keyword is present.
        val correctKeywords = config?.mockCorrectSolutions?.get(language) ?: emptyList()
        val isCorrect = correctKeywords.isNotEmpty() && correctKeywords.all { trimmedCode.contains(it) }

        return if (isCorrect) {
            // Find the test case matching this stdin to return its real expected output
            val expected = config?.testCases?.find { it.stdin == stdin }?.expectedStdout ?: "OK"
            RawExecutionResult(
                status = ExecutionStatus.ACCEPTED,
                stdout = expected,
                executionTime = "12ms",
                memory = "4.2MB"
            )
        } else {
            RawExecutionResult(
                status = ExecutionStatus.WRONG_ANSWER,
                stdout = "Incorrect output for input: $stdin",
                executionTime = "10ms",
                memory = "4.0MB"
            )
        }
    }

    private fun hasMeaningfulChanges(code: String, template: String): Boolean {
        val cleanedCode = code.replace(template, "").trim()
        return cleanedCode.isNotEmpty() && cleanedCode.length > 5
    }
}
