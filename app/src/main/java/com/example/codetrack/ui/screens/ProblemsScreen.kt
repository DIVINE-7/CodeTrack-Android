package com.example.codetrack.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.codetrack.ui.components.*
import com.example.codetrack.ui.models.UiProblem
import com.example.codetrack.ui.models.sampleProblems
import com.example.codetrack.ui.theme.CodeTrackTheme
import com.example.codetrack.ui.theme.Success
import com.example.codetrack.ui.theme.Warning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemsScreen(
    onProblemClick: (UiProblem) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf("All") }
    var selectedTopic by remember { mutableStateOf("All Topics") }
    var selectedPlatform by remember { mutableStateOf("All Platforms") }
    var selectedStatus by remember { mutableStateOf("All Statuses") }

    var topicDropdownExpanded by remember { mutableStateOf(false) }
    var platformDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    val topics = listOf("All Topics", "Arrays", "Strings", "Linked Lists", "Trees", "Graphs", "Dynamic Programming")
    val platforms = listOf("All Platforms", "LeetCode", "GeeksforGeeks", "CodeChef", "HackerRank")
    val statuses = listOf("All Statuses", "Not Started", "In Progress", "Solved")
    val difficulties = listOf("All", "Easy", "Medium", "Hard")

    // Filter problems
    val filteredProblems = sampleProblems.filter { problem ->
        val matchesSearch = searchQuery.isEmpty() ||
                problem.title.contains(searchQuery, ignoreCase = true) ||
                problem.topic.contains(searchQuery, ignoreCase = true) ||
                problem.platform.contains(searchQuery, ignoreCase = true)

        val matchesDifficulty = selectedDifficulty == "All" || problem.difficulty.equals(selectedDifficulty, ignoreCase = true)
        val matchesTopic = selectedTopic == "All Topics" || problem.topic.equals(selectedTopic, ignoreCase = true)
        val matchesPlatform = selectedPlatform == "All Platforms" || problem.platform.equals(selectedPlatform, ignoreCase = true)
        val matchesStatus = selectedStatus == "All Statuses" || problem.status.equals(selectedStatus, ignoreCase = true)

        matchesSearch && matchesDifficulty && matchesTopic && matchesPlatform && matchesStatus
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        // Header
        item {
            HeaderSection()
        }

        // Search Bar
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search problems...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search"
                        )
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = CircleShape,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }

        // Difficulty Chips Row
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(difficulties) { difficulty ->
                    val isSelected = selectedDifficulty == difficulty
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDifficulty = difficulty },
                        label = {
                            Text(
                                text = difficulty,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }

        // Additional Dropdown Filters (Topic, Platform, Status)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Topic Dropdown
                Box {
                    FilterChip(
                        selected = selectedTopic != "All Topics",
                        onClick = { topicDropdownExpanded = true },
                        label = { Text(if (selectedTopic == "All Topics") "Topic" else selectedTopic) },
                        trailingIcon = { Icon(Icons.Filled.FilterList, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    DropdownMenu(
                        expanded = topicDropdownExpanded,
                        onDismissRequest = { topicDropdownExpanded = false }
                    ) {
                        topics.forEach { topic ->
                            DropdownMenuItem(
                                text = { Text(topic) },
                                onClick = {
                                    selectedTopic = topic
                                    topicDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Platform Dropdown
                Box {
                    FilterChip(
                        selected = selectedPlatform != "All Platforms",
                        onClick = { platformDropdownExpanded = true },
                        label = { Text(if (selectedPlatform == "All Platforms") "Platform" else selectedPlatform) },
                        trailingIcon = { Icon(Icons.Filled.FilterList, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    DropdownMenu(
                        expanded = platformDropdownExpanded,
                        onDismissRequest = { platformDropdownExpanded = false }
                    ) {
                        platforms.forEach { platform ->
                            DropdownMenuItem(
                                text = { Text(platform) },
                                onClick = {
                                    selectedPlatform = platform
                                    platformDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Status Dropdown
                Box {
                    FilterChip(
                        selected = selectedStatus != "All Statuses",
                        onClick = { statusDropdownExpanded = true },
                        label = { Text(if (selectedStatus == "All Statuses") "Status" else selectedStatus) },
                        trailingIcon = { Icon(Icons.Filled.FilterList, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    DropdownMenu(
                        expanded = statusDropdownExpanded,
                        onDismissRequest = { statusDropdownExpanded = false }
                    ) {
                        statuses.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = {
                                    selectedStatus = status
                                    statusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Problem Count Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredProblems.size} Problems",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (selectedDifficulty != "All" || selectedTopic != "All Topics" ||
                    selectedPlatform != "All Platforms" || selectedStatus != "All Statuses" || searchQuery.isNotEmpty()) {
                    TextButton(onClick = {
                        searchQuery = ""
                        selectedDifficulty = "All"
                        selectedTopic = "All Topics"
                        selectedPlatform = "All Platforms"
                        selectedStatus = "All Statuses"
                    }) {
                        Text("Reset Filters", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // Problem Cards or Empty State
        if (filteredProblems.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp)
                ) {
                    EmptyState(
                        title = "No problems found",
                        description = "Try changing your search or filters.",
                        icon = Icons.Filled.SearchOff
                    )
                }
            }
        } else {
            items(filteredProblems, key = { it.id }) { problem ->
                ProblemItemCard(
                    problem = problem,
                    onClick = { onProblemClick(problem) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
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
            text = "Problems",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Track your DSA practice and stay consistent.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProblemItemCard(
    problem: UiProblem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusBadge(status = problem.status)
                    Text(
                        text = problem.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DifficultyChip(difficulty = problem.difficulty)
                    PlatformChip(platform = problem.platform)
                    Text(
                        text = "•  ${problem.topic}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (icon, color) = when (status) {
        "Solved" -> Icons.Filled.CheckCircle to Success
        "In Progress" -> Icons.Filled.HourglassTop to Warning
        else -> Icons.Filled.RadioButtonUnchecked to MaterialTheme.colorScheme.outline
    }

    Icon(
        imageVector = icon,
        contentDescription = status,
        tint = color,
        modifier = Modifier.size(18.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun ProblemsScreenPreview() {
    CodeTrackTheme {
        ProblemsScreen(onProblemClick = {})
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ProblemsScreenDarkPreview() {
    CodeTrackTheme {
        ProblemsScreen(onProblemClick = {})
    }
}
