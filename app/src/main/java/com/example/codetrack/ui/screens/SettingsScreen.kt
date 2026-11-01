package com.example.codetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun SettingsScreen(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit
) {

    val purple = Color(0xFF5146E5)
    val background = Color(0xFFF7F9FC)

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
            color = Color(0xFF111827)
        )

        Text(
            "Customize your CodeTrack experience.",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp, bottom = 22.dp)
        )

        // Profile
        SettingsSection("Profile")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
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
                    Text("CodeTrack User", fontWeight = FontWeight.Bold)
                    Text("Your coding journey", color = Color.Gray, fontSize = 12.sp)
                }

                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
        }

        Spacer(Modifier.height(22.dp))

        // Daily Goal
        SettingsSection("Daily Goal")

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Problems per day", modifier = Modifier.weight(1f))
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
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                SettingsSwitch(
                    icon = Icons.Default.Notifications,
                    title = "Push Notifications",
                    subtitle = "Receive app notifications",
                    checked = notifications,
                    onCheckedChange = { notifications = it }
                )

                HorizontalDivider(color = background)

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
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
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

        // About
        SettingsSection("About")

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                SettingsRow(
                    Icons.Default.Info,
                    "About CodeTrack",
                    "Your DSA preparation companion"
                )

                HorizontalDivider(color = background)

                SettingsRow(
                    Icons.Default.Code,
                    "App Version",
                    "Version 1.0.0"
                )

                HorizontalDivider(color = background)

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
            Text(title, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 11.sp, color = Color.Gray)
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF5146E5))
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
            Text(title, fontWeight = FontWeight.Medium)
            Text(subtitle, color = Color.Gray, fontSize = 11.sp)
        }

        Icon(Icons.Default.ChevronRight, contentDescription = null)
    }
}
