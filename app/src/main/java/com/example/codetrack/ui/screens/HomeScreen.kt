package com.example.codetrack.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.codetrack.ui.components.*
import com.example.codetrack.ui.theme.CodeTrackTheme
import com.example.codetrack.ui.theme.Success

data class Problem(
    val title: String,
    val difficulty: String,
    val platform: String
)

data class RecentActivity(
    val title: String,
    val timeAgo: String
)

@Composable
fun HomeScreen() {
    val todayProblems = listOf(
        Problem("Two Sum", "Easy", "LeetCode"),
        Problem("Binary Search", "Easy", "LeetCode"),
        Problem("Valid Parentheses", "Easy", "LeetCode"),
        Problem("Merge Intervals", "Medium", "LeetCode")
    )

    val recentActivities = listOf(
        RecentActivity("Solved Two Sum", "15 mins ago"),
        RecentActivity("Revised Binary Search", "1 hour ago"),
        RecentActivity("Completed today's goal", "2 hours ago")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        item {
            HeaderSection()
        }

        item {
            StreakSection()
        }

        item {
            ProgressSection()
        }

        item {
            QuickActionsSection()
        }

        item {
            SectionHeader(
                title = "Today's Problems",
                actionText = "View All",
                onActionClick = {}
            )
        }

        items(todayProblems) { problem ->
            QuestionCard(
                title = problem.title,
                difficulty = problem.difficulty,
                platform = problem.platform,
                onClick = {},
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            SectionHeader(
                title = "Recent Activity"
            )
        }

        items(recentActivities) { activity ->
            RecentActivityCard(
                activity = activity,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
        
        item {
            Spacer(Modifier.height(80.dp)) // Space for bottom nav
        }
    }
}

@Composable
private fun HeaderSection() {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .padding(top = 8.dp)
    ) {
        Text(
            text = "Good morning, User 👋",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Ready to make progress today?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StreakSection() {
    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        StreakCard(streakCount = 7)
    }
}

@Composable
private fun ProgressSection() {
    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        ProgressCard(current = 4, total = 5)
    }
}

@Composable
private fun QuickActionsSection() {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionCard(
            title = "Add Problem",
            icon = Icons.Filled.Add,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            onClick = {}
        )
        QuickActionCard(
            title = "Revision",
            icon = Icons.Filled.History,
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = Color.White,
            onClick = {}
        )
    }
}

@Composable
private fun RecentActivityCard(
    activity: RecentActivity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Success,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = activity.timeAgo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    CodeTrackTheme {
        HomeScreen()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenDarkPreview() {
    CodeTrackTheme {
        HomeScreen()
    }
}
