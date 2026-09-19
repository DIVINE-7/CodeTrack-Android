package com.example.codetrack.data.model

enum class CodingLanguage(val displayName: String) {
    C("C"),
    CPP("C++"),
    JAVA("Java"),
    PYTHON("Python")
}

enum class ExecutionStatus(val label: String) {
    IDLE("Idle"),
    INCOMPLETE("Incomplete Solution"),
    ACCEPTED("Accepted"),
    WRONG_ANSWER("Wrong Answer"),
    COMPILATION_ERROR("Compilation Error"),
    RUNTIME_ERROR("Runtime Error"),
    TIME_LIMIT_EXCEEDED("Time Limit Exceeded"),
    MEMORY_LIMIT_EXCEEDED("Memory Limit Exceeded"),
    SERVER_ERROR("Server Error"),
    NOT_CONFIGURED("Not Configured")
}

data class DsaTestCase(
    val id: Int,
    val stdin: String,
    val expectedStdout: String,
    val displayInput: String? = null,
    val isHidden: Boolean = false
)

data class TestCaseResult(
    val testCase: DsaTestCase,
    val actualStdout: String? = null,
    val stderr: String? = null,
    val passed: Boolean = false,
    val status: ExecutionStatus = ExecutionStatus.IDLE,
    val executionTime: String = "0ms",
    val memory: String = "0MB"
)

data class CodeExecutionResult(
    val status: ExecutionStatus,
    val message: String,
    val testResults: List<TestCaseResult>,
    val compileOutput: String? = null
)

data class CodingProblemConfig(
    val problemId: Int,
    val starterCode: Map<CodingLanguage, String>,
    val testCases: List<DsaTestCase>,
    // For mock evaluation of solution validity
    val mockCorrectSolutions: Map<CodingLanguage, List<String>>
)
