package com.example.codetrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import navigation.AppNavigation
import com.example.codetrack.ui.theme.CodeTrackTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            CodeTrackTheme {

                val navController = rememberNavController()

                AppNavigation(
                    navController = navController
                )
            }
        }
    }
}