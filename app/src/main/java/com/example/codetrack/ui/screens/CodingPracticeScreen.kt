package com.example.codetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codetrack.data.model.*
import com.example.codetrack.data.repository.CodeExecutionService
import com.example.codetrack.data.repository.CodingProblemRepository
import com.example.codetrack.data.repository.MockCodeExecutionService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodingPracticeScreen(
    problem: DsaProblem,
    onBack: () -> Unit,
    onSubmitSuccess: (Int) -> Unit
) {
    val config = remember(problem.id) { CodingProblemRepository.getConfig(problem.id) }
    val service: CodeExecutionService = remember(config) { MockCodeExecutionService(config) }

    var selectedLanguage by remember { mutableStateOf(CodingLanguage.PYTHON) }
    var codeText by remember(selectedLanguage, config) { 
        mutableStateOf(config?.starterCode?.get(selectedLanguage) ?: "// Starter code not available") 
    }

    var executionResult by remember { mutableStateOf<CodeExecutionResult?>(null) }
    var isRunning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Code Editor", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F9FA))
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Problem Overview
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = problem.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = problem.difficulty, color = Color(0xFF4F46E5), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text(text = "•", color = Color.Gray, fontSize = 12.sp)
                        Text(text = problem.topic, color = Color.Gray, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = problem.description, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF374151))
                }
            }

            // Language Selector
            Column {
                Text("Select Language:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CodingLanguage.entries.forEach { lang ->
                        val isSelected = selectedLanguage == lang
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF4F46E5) else Color(0xFFE5E7EB))
                                .clickable { 
                                    selectedLanguage = lang
                                    executionResult = null
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = lang.displayName, color = if (isSelected) Color.White else Color(0xFF1F2937), fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Editor
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Source Code Editor:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        "Demo: MOCK_COMPILE_ERROR, MOCK_RUNTIME_ERROR",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color(0xFF1F2937), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF374151), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    BasicTextField(
                        value = codeText,
                        onValueChange = { codeText = it },
                        modifier = Modifier.fillMaxSize(),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            color = Color(0xFFF9FAFB),
                            lineHeight = 20.sp
                        )
                    )
                }
            }

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { 
                        codeText = config?.starterCode?.get(selectedLanguage) ?: "// N/A"
                        executionResult = null
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset")
                }

                Button(
                    onClick = {
                        isRunning = true
                        executionResult = null
                        scope.launch {
                            val result = runAllTests(service, config, selectedLanguage, codeText)
                            executionResult = result
                            isRunning = false
                        }
                    },
                    enabled = !isRunning,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B7280))
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Run")
                }

                Button(
                    onClick = {
                        isRunning = true
                        executionResult = null
                        scope.launch {
                            val result = runAllTests(service, config, selectedLanguage, codeText)
                            executionResult = result
                            isRunning = false
                            if (result.status == ExecutionStatus.ACCEPTED) {
                                onSubmitSuccess(problem.id)
                            }
                        }
                    },
                    enabled = !isRunning,
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Submit")
                }
            }

            if (isRunning) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color(0xFF4F46E5))
            }

            // Verdict and Test Results
            executionResult?.let { res ->
                val bannerColor = when (res.status) {
                    ExecutionStatus.ACCEPTED -> Color(0xFF10B981)
                    ExecutionStatus.WRONG_ANSWER -> Color(0xFFF59E0B)
                    ExecutionStatus.NOT_CONFIGURED -> Color(0xFF6B7280)
                    ExecutionStatus.INCOMPLETE -> Color(0xFF3B82F6)
                    else -> Color(0xFFEF4444)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = bannerColor.copy(alpha = 0.1f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, bannerColor)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (res.status) {
                                ExecutionStatus.ACCEPTED -> Icons.Default.CheckCircle
                                ExecutionStatus.NOT_CONFIGURED -> Icons.Default.Info
                                ExecutionStatus.INCOMPLETE -> Icons.Default.Warning
                                else -> Icons.Default.Error
                            },
                            contentDescription = null,
                            tint = bannerColor
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "Verdict: ${res.status.label}", fontWeight = FontWeight.Bold, color = bannerColor)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = res.message, style = MaterialTheme.typography.bodySmall, color = Color(0xFF374151))
                            if (res.compileOutput != null) {
                                Text(text = res.compileOutput, color = Color.Red, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                if (res.testResults.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Test Case Results:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1F2937))
                        res.testResults.forEachIndexed { index, tr ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Test Case #${index + 1}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        val (statusText, statusColor) = when(tr.passed) {
                                            true -> "PASSED" to Color(0xFF10B981)
                                            false -> "FAILED" to Color(0xFFEF4444)
                                            else -> "NOT RUN" to Color.Gray
                                        }
                                        Surface(
                                            color = statusColor.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = statusText, 
                                                color = statusColor, 
                                                fontWeight = FontWeight.ExtraBold, 
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    TestParamRow("Display Input", tr.testCase.displayInput ?: tr.testCase.stdin)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TestParamRow("Expected Output", tr.testCase.expectedStdout)
                                    
                                    if (tr.actualStdout != null || tr.stderr != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        TestParamRow(
                                            label = if (tr.stderr != null) "Runtime Error (stderr)" else "Actual Output (stdout)", 
                                            value = tr.stderr ?: tr.actualStdout ?: "", 
                                            isError = tr.passed == false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Orchestrates running multiple test cases using the execution service.
 * In a real implementation, this might be a single batch call to the backend.
 */
private suspend fun runAllTests(
    service: CodeExecutionService,
    config: CodingProblemConfig?,
    language: CodingLanguage,
    sourceCode: String
): CodeExecutionResult {
    if (config == null) {
        return CodeExecutionResult(ExecutionStatus.NOT_CONFIGURED, "Execution configuration not available.", emptyList())
    }

    val results = mutableListOf<TestCaseResult>()
    var overallStatus = ExecutionStatus.ACCEPTED
    var overallMessage = "All test cases passed."

    for (testCase in config.testCases) {
        val raw = service.execute(language, sourceCode, testCase.stdin)
        
        // If any test is INCOMPLETE/ERROR, abort and return immediately
        if (raw.status == ExecutionStatus.INCOMPLETE) {
            return CodeExecutionResult(ExecutionStatus.INCOMPLETE, "No solution submitted yet.", emptyList())
        }
        if (raw.status == ExecutionStatus.COMPILATION_ERROR) {
            return CodeExecutionResult(ExecutionStatus.COMPILATION_ERROR, "Compilation failed.", emptyList(), raw.compileOutput)
        }
        if (raw.status == ExecutionStatus.RUNTIME_ERROR) {
            overallStatus = ExecutionStatus.RUNTIME_ERROR
            overallMessage = "Runtime error occurred during execution."
        }

        val passed = raw.status == ExecutionStatus.ACCEPTED && raw.stdout?.trim() == testCase.expectedStdout.trim()
        
        if (!passed && overallStatus == ExecutionStatus.ACCEPTED) {
            overallStatus = ExecutionStatus.WRONG_ANSWER
            overallMessage = "One or more test cases failed."
        }

        results.add(TestCaseResult(
            testCase = testCase,
            actualStdout = raw.stdout,
            stderr = raw.stderr,
            passed = passed,
            status = raw.status,
            executionTime = raw.executionTime,
            memory = raw.memory
        ))
    }

    return CodeExecutionResult(overallStatus, overallMessage, results)
}

@Composable
fun TestParamRow(label: String, value: String, isError: Boolean = false) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = if (isError) Color(0xFFFEF2F2) else Color(0xFFF3F4F6),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = value,
                modifier = Modifier.padding(10.dp),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = if (isError) Color(0xFFB91C1C) else Color(0xFF111827)
            )
        }
    }
}
