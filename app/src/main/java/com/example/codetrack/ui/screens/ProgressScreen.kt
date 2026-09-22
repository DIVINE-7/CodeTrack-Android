package com.example.codetrack.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.codetrack.ui.components.SectionHeader
import com.example.codetrack.ui.theme.CodeTrackTheme
import com.example.codetrack.ui.theme.Success
import com.example.codetrack.ui.theme.Warning

data class TopicProgress(
    val name: String,
    val percentage: Int
)

data class DayActivity(
    val day: String,
    val count: Int, // e.g. number of problems solved
    val isToday: Boolean = false
)

@Composable
fun ProgressScreen() {
    val topicProgressList = listOf(
        TopicProgress("Arrays", 82),
        TopicProgress("Strings", 65),
        TopicProgress("Linked Lists", 50),
        TopicProgress("Trees", 45),
        TopicProgress("Graphs", 30),
        TopicProgress("Dynamic Programming", 20)
    )

    val weeklyActivity = listOf(
        DayActivity("Mon", 3),
        DayActivity("Tue", 5),
        DayActivity("Wed", 2),
        DayActivity("Thu", 6),
        DayActivity("Fri", 4, isToday = true),
        DayActivity("Sat", 0),
        DayActivity("Sun", 0)
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
            SummaryCardsSection()
        }

        item {
            WeeklyActivitySection(weeklyActivity)
        }

        item {
            TopicProgressSection(topicProgressList)
        }

        item {
            DifficultyBreakdownSection()
        }

        item {
            RecentAchievementSection()
        }

        item {
            Spacer(modifier = Modifier.height(80.dp)) // Space for bottom nav
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
            text = "Your Progress",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Track your interview preparation journey.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SummaryCardsSection() {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            value = "7",
            label = "Day Streak",
            accentColor = Warning,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            value = "42",
            label = "Problems Solved",
            accentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            value = "68%",
            label = "Overall Progress",
            accentColor = Success,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryCard(
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun WeeklyActivitySection(weeklyActivity: List<DayActivity>) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        SectionHeader(title = "Weekly Activity")
        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val maxCount = 6
                weeklyActivity.forEach { activity ->
                    val barHeight = ((activity.count.toFloat() / maxCount) * 60).dp
                    val normalizedHeight = barHeight.coerceAtLeast(8.dp)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        if (activity.count > 0) {
                            Text(
                                text = activity.count.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(normalizedHeight)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    if (activity.isToday) MaterialTheme.colorScheme.primary
                                    else if (activity.count > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = activity.day,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (activity.isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (activity.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopicProgressSection(topics: List<TopicProgress>) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        SectionHeader(title = "Topic Progress")
        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                topics.forEach { topic ->
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = topic.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${topic.percentage}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { topic.percentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DifficultyBreakdownSection() {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        SectionHeader(title = "Difficulty Breakdown")
        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DifficultyItem(label = "Easy", count = 24, color = Success, modifier = Modifier.weight(1f))
                DifficultyItem(label = "Medium", count = 14, color = Warning, modifier = Modifier.weight(1f))
                DifficultyItem(label = "Hard", count = 4, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DifficultyItem(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = color.copy(alpha = 0.1f),
            shape = CircleShape,
            modifier = Modifier.size(12.dp)
        ) {}
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun RecentAchievementSection() {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        SectionHeader(title = "Recent Achievement")
        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = Warning.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Warning.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = "Achievement Icon",
                            tint = Warning,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "🏆 7 Day Streak",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Warning
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Keep your consistency going!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Warning.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressScreenPreview() {
    CodeTrackTheme {
        ProgressScreen()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ProgressScreenDarkPreview() {
    CodeTrackTheme {
        ProgressScreen()
    }
}
