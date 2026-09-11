package com.example.codetrack

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Goal(val id: Int, val title: String, val isChecked: Boolean = false)

@Composable
fun ProductivityScreen() {
    val context = LocalContext.current
    
    // State for daily goals
    var goalsState by remember {
        mutableStateOf(
            listOf(
                Goal(1, "Solve 5 DSA problems"),
                Goal(2, "Practice 10 aptitude questions"),
                Goal(3, "Revise 3 interview questions"),
                Goal(4, "Watch 1 recommended learning video"),
                Goal(5, "Participate in a coding contest")
            )
        )
    }

    // Derived states
    val completedTasks = goalsState.count { it.isChecked }
    val totalTasks = goalsState.size
    val progressPercent = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
    
    val animatedProgress by animateFloatAsState(
        targetValue = progressPercent,
        label = "progressAnimation"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8F9FA) // Light background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. TOP HEADER
            item {
                ProductivityHeader()
            }
            
            // 2. TODAY'S PROGRESS CARD
            item {
                TodayProgressCard(
                    completedCount = completedTasks,
                    progress = animatedProgress,
                    dsaProgress = "2 / 5",
                    aptitudeProgress = "5 / 10",
                    interviewProgress = "1 / 3"
                )
            }
            
            // 3. CURRENT STREAK CARD
            item {
                StreakCard(
                    streak = 7,
                    problemsSolved = 45,
                    daysActive = 5
                )
            }
            
            // 4. DAILY GOALS SECTION
            item {
                DailyGoalsSection(
                    goals = goalsState,
                    onGoalToggle = { goalId ->
                        goalsState = goalsState.map {
                            if (it.id == goalId) it.copy(isChecked = !it.isChecked) else it
                        }
                    }
                )
            }
            
            // 5. QUICK PRACTICE SECTION
            item {
                QuickPracticeSection { practiceType ->
                    Toast.makeText(context, "Starting $practiceType Practice...", Toast.LENGTH_SHORT).show()
                }
            }
            
            // 6. PREPARATION OVERVIEW SECTION
            item {
                PreparationOverview()
            }
            
            // 7. MOTIVATIONAL SECTION
            item {
                MotivationCard()
            }
        }
    }
}

@Composable
fun ProductivityHeader() {
    Column {
        Text(
            text = "Productivity",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        Text(
            text = "Build consistency. Prepare smarter. Get placement ready.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            color = Color(0xFFE8F0FE),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "✨ Your daily progress matters.",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF1967D2),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TodayProgressCard(
    completedCount: Int,
    progress: Float,
    dsaProgress: String,
    aptitudeProgress: String,
    interviewProgress: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Progress",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$completedCount tasks completed today",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF4F46E5)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            ProgressItemRow("DSA Problems", dsaProgress, Icons.Default.Code)
            ProgressItemRow("Aptitude Questions", aptitudeProgress, Icons.Default.Timer)
            ProgressItemRow("Interview Questions", interviewProgress, Icons.Default.QuestionAnswer)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape),
                color = Color(0xFF4F46E5),
                trackColor = Color(0xFFEEF2FF)
            )
        }
    }
}

@Composable
fun ProgressItemRow(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFFF3F4F6), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color(0xFF4B5563)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label, 
            style = MaterialTheme.typography.bodyMedium, 
            modifier = Modifier.weight(1f),
            color = Color(0xFF374151)
        )
        Text(
            text = value, 
            style = MaterialTheme.typography.bodyMedium, 
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )
    }
}

@Composable
fun StreakCard(streak: Int, problemsSolved: Int, daysActive: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)), // Light orange
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFEDD5))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🔥", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Current Streak",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9A3412)
                    )
                }
                Text(
                    text = "$streak Days",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFC2410C)
                )
                Text(
                    text = "Keep going! Don't break your streak.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFEA580C)
                )
            }
            
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StreakStat("Problems this week", problemsSolved.toString())
                StreakStat("Days active", "$daysActive/7")
            }
        }
    }
}

@Composable
fun StreakStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.End) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF9A3412))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color(0xFFC2410C).copy(alpha = 0.7f))
    }
}

@Composable
fun DailyGoalsSection(goals: List<Goal>, onGoalToggle: (Int) -> Unit) {
    Column {
        Text(
            text = "Today's Goals",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                goals.forEach { goal ->
                    GoalItem(goal, onGoalToggle)
                }
            }
        }
    }
}

@Composable
fun GoalItem(goal: Goal, onGoalToggle: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = goal.isChecked,
            onCheckedChange = { onGoalToggle(goal.id) },
            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4F46E5))
        )
        Text(
            text = goal.title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp),
            color = if (goal.isChecked) Color.Gray else Color(0xFF1F2937),
            textDecoration = if (goal.isChecked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
        )
    }
}

@Composable
fun QuickPracticeSection(onStart: (String) -> Unit) {
    Column {
        Text(
            text = "Quick Practice",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PracticeCard("DSA", "Practice coding", Icons.Default.Code, Modifier.weight(1f), onStart)
            PracticeCard("Aptitude", "Improve quant", Icons.Default.Timer, Modifier.weight(1f), onStart)
            PracticeCard("Interview", "Tech prep", Icons.Default.QuestionAnswer, Modifier.weight(1f), onStart)
        }
    }
}

@Composable
fun PracticeCard(title: String, desc: String, icon: ImageVector, modifier: Modifier, onStart: (String) -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFEEF2FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF4F46E5))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = desc, 
                style = MaterialTheme.typography.labelSmall, 
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.height(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { onStart(title) },
                modifier = Modifier.fillMaxWidth().height(36.dp),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
            ) {
                Text("Start", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun PreparationOverview() {
    Column {
        Text(
            text = "Preparation Overview",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("DSA", "45 solved", Icons.Default.Code, Modifier.weight(1f))
                StatCard("Aptitude", "32 solved", Icons.Default.Timer, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Interview", "18 completed", Icons.Default.QuestionAnswer, Modifier.weight(1f))
                StatCard("Contests", "6 participated", Icons.Default.EmojiEvents, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MotivationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Today's Motivation",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "\"Small progress every day leads to big results.\"",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}
