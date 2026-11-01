package com.example.codetrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.codetrack.ui.MainScreen
import com.example.codetrack.ui.theme.CodeTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var darkTheme by rememberSaveable {
                mutableStateOf(false)
            }

            CodeTrackTheme(darkTheme = darkTheme) {
                MainScreen(
                    darkTheme = darkTheme,
                    onDarkThemeChange = { darkTheme = it }
                )
            }
        }
    }
}
