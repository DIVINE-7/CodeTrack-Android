package com.example.codetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codetrack.ui.theme.CodeTrackTheme

@Composable
fun SettingsScreen(
    darkTheme: Boolean = false,
    onDarkThemeChange: (Boolean) -> Unit = {},
    onLogout: (() -> Unit)? = null
) {
    val purple = Color(0xFF5146E5)
    val background = MaterialTheme.colorScheme.background

    var dailyGoal by remember { mutableFloatStateOf(5f) }
    var notifications by remember { mutableStateOf(true) }
    var revisionReminder by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            "Settings",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            "Customize your CodeTrack experience.",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp, bottom = 22.dp)
        )

        // Profile Section
        SettingsSection("Profile")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = purple.copy(alpha = 0.12f),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = purple,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 14.dp)
                ) {
                    Text("CodeTrack User", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Your coding journey", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }

        Spacer(Modifier.height(22.dp))

        // Daily Goal
        SettingsSection("Daily Goal")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Problems per day", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        "${dailyGoal.toInt()} Problems",
                        color = purple,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = dailyGoal,
                    onValueChange = { dailyGoal = it },
                    valueRange = 1f..15f,
                    steps = 13,
                    colors = SliderDefaults.colors(
                        thumbColor = purple,
                        activeTrackColor = purple
                    )
                )

                Text(
                    "Stay consistent and achieve your goals!",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        // Notifications
        SettingsSection("Notifications")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                SettingsSwitch(
                    icon = Icons.Default.Notifications,
                    title = "Push Notifications",
                    subtitle = "Receive app notifications",
                    checked = notifications,
                    onCheckedChange = { notifications = it }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                SettingsSwitch(
                    icon = Icons.Default.Alarm,
                    title = "Revision Reminder",
                    subtitle = "Get reminded to revise problems",
                    checked = revisionReminder,
                    onCheckedChange = { revisionReminder = it }
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        // Appearance
        SettingsSection("Appearance")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            SettingsSwitch(
                icon = Icons.Default.DarkMode,
                title = "Dark Mode",
                subtitle = "Change app appearance preference",
                checked = darkTheme,
                onCheckedChange = { onDarkThemeChange(it) }
            )
        }

        Spacer(Modifier.height(22.dp))

        // Account / Session (Logout)
        if (onLogout != null) {
            SettingsSection("Account")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLogout() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Log Out",
                        tint = Color(0xFFEF4444)
                    )

                    Text(
                        "Log Out",
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                    )
                }
            }

            Spacer(Modifier.height(22.dp))
        }

        // About
        SettingsSection("About")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                SettingsRow(
                    Icons.Default.Info,
                    "About CodeTrack",
                    "Your DSA preparation companion"
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                SettingsRow(
                    Icons.Default.Code,
                    "App Version",
                    "Version 1.0.0"
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                SettingsRow(
                    Icons.Default.Star,
                    "Rate CodeTrack",
                    "Support our app"
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        Text(
            "Made with ❤️ for developers",
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = Color.Gray,
            fontSize = 12.sp
        )

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsSection(title: String) {
    Text(
        title,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

@Composable
private fun SettingsSwitch(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF5146E5))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(title, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 11.sp, color = Color.Gray)
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF5146E5)
            )
        )
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF5146E5))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(title, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, color = Color.Gray, fontSize = 11.sp)
        }

        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    CodeTrackTheme {
        SettingsScreen(
            darkTheme = false,
            onDarkThemeChange = {},
            onLogout = {}
        )
    }
}
