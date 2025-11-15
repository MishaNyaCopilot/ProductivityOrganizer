// app/src/main/java/com/example/productivityorganizer/ui/screens/SplashScreen.kt
package com.example.productivityorganizer.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.productivityorganizer.ui.navigation.AppDestinations
import com.example.productivityorganizer.ui.state.AuthenticationState
import com.example.productivityorganizer.ui.viewmodels.GoogleSignInViewModel

private const val TAG = "SplashScreen"

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: GoogleSignInViewModel = viewModel()
) {
    val authState by authViewModel.authenticationState.collectAsStateWithLifecycle()

    LaunchedEffect(authState) {
        Log.d(TAG, "Auth state changed: $authState")
        when (authState) {
            is AuthenticationState.Success -> {
                Log.d(TAG, "Navigating to HomeScreen")
                navController.navigate(AppDestinations.HOME_SCREEN_ROUTE) {
                    popUpTo(AppDestinations.SPLASH_SCREEN_ROUTE) { inclusive = true }
                }
            }
            is AuthenticationState.Error, AuthenticationState.Idle -> {
                // Navigate only if the state is definitively not loading and not success
                Log.d(TAG, "Navigating to AuthScreen from state: $authState")
                navController.navigate(AppDestinations.AUTH_SCREEN_ROUTE) {
                    popUpTo(AppDestinations.SPLASH_SCREEN_ROUTE) { inclusive = true }
                }
            }
            AuthenticationState.Loading -> {
                Log.d(TAG, "Auth state is Loading. Waiting...")
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "ProdOrganizer",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            CircularProgressIndicator()
        }
    }
}