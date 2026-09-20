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

@Composable
fun ProgressScreen(
    onBack: () -> Unit
) {
    val repository = ProgressRepository.getInstance()

    val dailyProgress by repository.dailyProgress.collectAsState()

    val totalCompleted =
        dailyProgress.dsaCompleted +
                dailyProgress.aptitudeCompleted +
                dailyProgress.interviewCompleted

    val totalTarget =
        dailyProgress.dsaTarget +
                dailyProgress.aptitudeTarget +
                dailyProgress.interviewTarget

    val overallProgress =
        if (totalTarget > 0) {
            totalCompleted.toFloat() / totalTarget
        } else {
            0f
        }

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
            text = "Track your daily learning progress",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Overall Progress",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "$totalCompleted / $totalTarget completed"
                )

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { overallProgress },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${(overallProgress * 100).toInt()}% completed"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ProgressCard(
            title = "DSA",
            completed = dailyProgress.dsaCompleted,
            target = dailyProgress.dsaTarget
        )

        Spacer(modifier = Modifier.height(12.dp))

        ProgressCard(
            title = "Aptitude",
            completed = dailyProgress.aptitudeCompleted,
            target = dailyProgress.aptitudeTarget
        )

        Spacer(modifier = Modifier.height(12.dp))

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