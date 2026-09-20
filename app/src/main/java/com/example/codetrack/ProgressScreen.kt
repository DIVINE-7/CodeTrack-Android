package com.example.codetrack

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.codetrack.data.repository.ProgressRepository
import com.example.codetrack.data.repository.StatisticsRepository
import com.example.codetrack.data.repository.StreakRepository
import java.time.LocalDate

@Composable
fun ProgressScreen() {
    val progressRepository = ProgressRepository.getInstance()
    val statisticsRepository = StatisticsRepository.getInstance()
    val streakRepository = StreakRepository.getInstance()

    val dailyProgress by progressRepository.dailyProgress.collectAsState()
    val weeklyStats by statisticsRepository.weeklyStats.collectAsState()
    val activityRecords by streakRepository.activityRecords.collectAsState()

    val currentStreak = calculateStreak(activityRecords)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = "Progress",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Track your coding and placement preparation",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Current streak
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Current Streak",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$currentStreak days",
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = "Keep practicing every day!"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weekly overview
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Weekly Overview",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${weeklyStats.totalCompleted} / ${weeklyStats.totalTarget} completed"
                )

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { weeklyStats.completionPercentage },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${(weeklyStats.completionPercentage * 100).toInt()}% completed"
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Active days: ${weeklyStats.activeDays}"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weekly activity
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Weekly Activity",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val days = listOf(
                        "M", "T", "W", "T", "F", "S", "S"
                    )

                    days.forEachIndexed { index, day ->
                        Column(
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                        ) {
                            Text(day)

                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            Text(
                                text = weeklyStats.weeklyActivity[index].toString()
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category progress
        ProgressCard(
            title = "DSA",
            completed = dailyProgress.dsaCompleted,
            target = dailyProgress.dsaTarget
        )

        Spacer(modifier = Modifier.height(10.dp))

        ProgressCard(
            title = "Aptitude",
            completed = dailyProgress.aptitudeCompleted,
            target = dailyProgress.aptitudeTarget
        )

        Spacer(modifier = Modifier.height(10.dp))

        ProgressCard(
            title = "Interview",
            completed = dailyProgress.interviewCompleted,
            target = dailyProgress.interviewTarget
        )
    }
}

@Composable
private fun ProgressCard(
    title: String,
    completed: Int,
    target: Int
) {
    val progress =
        if (target > 0) {
            completed.toFloat() / target
        } else {
            0f
        }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "$completed / $target"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun calculateStreak(
    records: List<com.example.codetrack.data.model.ActivityRecord>
): Int {
    if (records.isEmpty()) return 0

    val activeDates = records
        .filter { it.problemsSolved > 0 }
        .map { it.date }
        .toSet()

    var streak = 0
    var date = LocalDate.now()

    while (activeDates.contains(date)) {
        streak++
        date = date.minusDays(1)
    }

    return streak
}