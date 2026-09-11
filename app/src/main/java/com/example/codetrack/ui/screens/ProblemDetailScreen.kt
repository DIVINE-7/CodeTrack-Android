package com.example.codetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codetrack.data.model.DsaProblem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemDetailScreen(
    problem: DsaProblem,
    onBack: () -> Unit,
    onToggleSolved: (Int) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onNavigateToCodingPractice: (DsaProblem) -> Unit = {}
) {
    val difficultyColor = when (problem.difficulty) {
        "Easy" -> Color(0xFF10B981)
        "Medium" -> Color(0xFFF59E0B)
        "Hard" -> Color(0xFFEF4444)
        else -> Color.Gray
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Problem Details", fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to list",
                            tint = Color(0xFF1A1A1A)
                        )
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
            // Title Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = problem.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = difficultyColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = problem.difficulty,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = difficultyColor,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            color = Color(0xFFF3F4F6),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = problem.topic,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF4B5563),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        if (problem.isSolved) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Solved",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Solved", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Description Card
            DetailSectionCard(title = "Problem Description", content = problem.description)

            // Example Input / Output
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Examples", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("Example Input:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)).padding(8.dp)) {
                        Text(problem.exampleInput, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 13.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Example Output:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)).padding(8.dp)) {
                        Text(problem.exampleOutput, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 13.sp)
                    }

                    if (problem.explanation.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Explanation:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(problem.explanation, fontSize = 14.sp, color = Color(0xFF374151))
                    }
                }
            }

            // Constraints Card
            if (problem.constraints.isNotEmpty()) {
                DetailSectionCard(title = "Constraints", content = problem.constraints)
            }

            // Hint Card
            if (problem.hint.isNotEmpty()) {
                DetailSectionCard(title = "Hint / Strategy", content = problem.hint)
            }

            // Primary Actions: Practice Code Workspace
            Button(
                onClick = { onToggleSolved(problem.id) }, // Keeps backward compatibility for quick status toggling if needed or we change label
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (problem.isSolved) Color(0xFF10B981) else Color(0xFF4F46E5)
                )
            ) {
                Text(
                    text = if (problem.isSolved) "✓ Solved (Tap to Undo)" else "Mark as Solved",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // Coding Sandbox CTA Button
            Button(
                onClick = { onNavigateToCodingPractice(problem) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827))
            ) {
                Text("Open Code Practice Workspace 💻", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            // Navigation: Previous / Next
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onPrevious,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4F46E5)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4F46E5))
                ) {
                    Text("Previous Problem", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onNext,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4F46E5)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4F46E5))
                ) {
                    Text("Next Problem", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun DetailSectionCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = content, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF374151), lineHeight = 20.sp)
        }
    }
}
