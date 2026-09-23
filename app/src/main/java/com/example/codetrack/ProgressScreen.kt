package com.example.codetrack

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun ProgressScreen(
    onBack: () -> Unit
) {

    // Current streak
    val currentStreak = 5

    // Weekly overview
    val weeklyCompleted = 62
    val weeklyTarget = 90

    // Weekly activity
    val weeklyActivity = listOf(4, 6, 3, 7, 5, 8, 2)

    // Daily progress
    val dsaCompleted = 2
    val dsaTarget = 5

    val aptitudeCompleted = 5
    val aptitudeTarget = 10

    val interviewCompleted = 1
    val interviewTarget = 3

    val weeklyProgress =
        weeklyCompleted.toFloat() / weeklyTarget.toFloat()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ------------------------------------------------
        // HEADER
        // ------------------------------------------------

        item {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = onBack
                ) {

                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Text(
                    text = "Progress",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }


        // ------------------------------------------------
        // CURRENT STREAK
        // ------------------------------------------------

        item {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "Current Streak",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "$currentStreak days",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }


        // ------------------------------------------------
        // WEEKLY OVERVIEW
        // ------------------------------------------------

        item {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "Weekly Overview",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text = "$weeklyCompleted / $weeklyTarget completed",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    LinearProgressIndicator(
                        progress = {
                            weeklyProgress.coerceIn(0f, 1f)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "${(weeklyProgress * 100).roundToInt()}% completed",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }


        // ------------------------------------------------
        // WEEKLY ACTIVITY
        // ------------------------------------------------

        item {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "Weekly Activity",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    WeeklyActivity(
                        activity = weeklyActivity
                    )
                }
            }
        }


        // ------------------------------------------------
        // DAILY PROGRESS TITLE
        // ------------------------------------------------

        item {

            Text(
                text = "Daily Progress",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }


        // ------------------------------------------------
        // DSA
        // ------------------------------------------------

        item {

            DailyProgressCard(
                title = "DSA",
                completed = dsaCompleted,
                target = dsaTarget
            )
        }


        // ------------------------------------------------
        // APTITUDE
        // ------------------------------------------------

        item {

            DailyProgressCard(
                title = "Aptitude",
                completed = aptitudeCompleted,
                target = aptitudeTarget
            )
        }


        // ------------------------------------------------
        // INTERVIEW
        // ------------------------------------------------

        item {

            DailyProgressCard(
                title = "Interview",
                completed = interviewCompleted,
                target = interviewTarget
            )
        }


        item {

            Spacer(
                modifier = Modifier.height(20.dp)
            )
        }
    }
}


// ========================================================
// DAILY PROGRESS CARD
// ========================================================

@Composable
private fun DailyProgressCard(
    title: String,
    completed: Int,
    target: Int
) {

    val progress =
        if (target > 0) {
            completed.toFloat() / target.toFloat()
        } else {
            0f
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "$completed / $target",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            LinearProgressIndicator(
                progress = {
                    progress.coerceIn(0f, 1f)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "${(progress * 100).roundToInt()}% completed",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


// ========================================================
// WEEKLY ACTIVITY
// ========================================================

@Composable
private fun WeeklyActivity(
    activity: List<Int>
) {

    val days = listOf(
        "M",
        "T",
        "W",
        "T",
        "F",
        "S",
        "S"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        activity.take(7).forEachIndexed { index, count ->

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = days[index],
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}