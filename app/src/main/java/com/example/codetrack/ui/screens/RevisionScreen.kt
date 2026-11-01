package com.example.codetrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class RevisionTask(
    val id: Int,
    val title: String,
    val topic: String,
    val difficulty: String,
    val due: String,
    val completed: Boolean = false
)

@Composable
fun RevisionScreen() {

    val purple = Color(0xFF5146E5)
    val cyan = Color(0xFF08B5CE)
    val background = Color(0xFFF7F9FC)

    var selectedFilter by remember { mutableStateOf("All") }

    val revisions = remember {
        mutableStateListOf(
            RevisionTask(1, "Two Sum", "Arrays", "Easy", "Today"),
            RevisionTask(2, "Binary Search", "Arrays", "Easy", "Today"),
            RevisionTask(3, "Valid Parentheses", "Strings", "Easy", "Today"),
            RevisionTask(4, "Merge Intervals", "Arrays", "Medium", "Tomorrow"),
            RevisionTask(5, "Best Time to Buy and Sell Stock", "Arrays", "Easy", "Tomorrow")
        )
    }

    val completedCount = revisions.count { it.completed }
    val pendingCount = revisions.size - completedCount

    val filteredRevisions = revisions.filter {
        when (selectedFilter) {
            "Pending" -> !it.completed
            "Completed" -> it.completed
            else -> true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Revision",
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF101828)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Keep revising and strengthen your DSA concepts.",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(22.dp))

        // Revision summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF0EEFF)
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {

                Text(
                    text = "Today's Revision",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = purple
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RevisionStat("Total", revisions.size.toString(), purple)
                    RevisionStat("Completed", completedCount.toString(), Color(0xFF00B783))
                    RevisionStat("Pending", pendingCount.toString(), cyan)
                }

                Spacer(modifier = Modifier.height(16.dp))

                LinearProgressIndicator(
                    progress = {
                        completedCount.toFloat() / revisions.size.coerceAtLeast(1)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = purple,
                    trackColor = Color(0xFFDCD9F7)
                )

                Spacer(modifier = Modifier.height(7.dp))

                Text(
                    text = "$completedCount of ${revisions.size} revisions completed",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Your Revision Tasks",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF101828)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filters
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Pending", "Completed").forEach { filter ->

                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE8E5FF),
                        selectedLabelColor = purple
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {

            if (filteredRevisions.isEmpty()) {
                item {
                    Text(
                        text = "No revision tasks here.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(30.dp),
                        color = Color.Gray
                    )
                }
            }

            items(filteredRevisions, key = { it.id }) { task ->

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(modifier = Modifier.weight(1f)) {

                            Text(
                                text = task.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF101828)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "${task.topic}  •  ${task.difficulty}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(5.dp))

                            Text(
                                text = if (task.completed) "Completed"
                                else "Due: ${task.due}",
                                fontSize = 12.sp,
                                color = if (task.completed)
                                    Color(0xFF00A878) else purple
                            )
                        }

                        Button(
                            onClick = {
                                val index = revisions.indexOfFirst {
                                    it.id == task.id
                                }
                                if (index != -1) {
                                    revisions[index] = revisions[index].copy(
                                        completed = !revisions[index].completed
                                    )
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(
                                horizontal = 12.dp,
                                vertical = 8.dp
                            ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (task.completed)
                                    Color(0xFFE8F7F1) else purple,
                                contentColor = if (task.completed)
                                    Color(0xFF008F68) else Color.White
                            )
                        ) {
                            Text(
                                text = if (task.completed) "Undo" else "Revise",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RevisionStat(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.DarkGray
        )
    }
}
