package ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen(
    onDsaClick: () -> Unit,
    onProductivityClick: () -> Unit,
    onProgressClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "CodeTrack",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your Coding Companion",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        DashboardCard(
            title = "DSA Practice",
            description = "Practice coding problems and improve your skills.",
            onClick = onDsaClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        DashboardCard(
            title = "Productivity",
            description = "Manage your coding tasks and activities.",
            onClick = onProductivityClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        DashboardCard(
            title = "Progress",
            description = "Track your learning and coding progress.",
            onClick = onProgressClick
        )
    }
}

@Composable
private fun DashboardCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClick
            ) {
                Text("Open")
            }
        }
    }
}