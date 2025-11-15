package com.example.productivityorganizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.productivityorganizer.ui.navigation.MainAppNavigation
import com.example.productivityorganizer.ui.theme.ProductivityOrganizerTheme
import androidx.navigation.compose.rememberNavController // Keep rememberNavController here
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProductivityOrganizerTheme {
                val navController = rememberNavController() // Main NavController for the app
                MainAppNavigation(navController = navController) // Pass the main navController
            }
        }
    }
}