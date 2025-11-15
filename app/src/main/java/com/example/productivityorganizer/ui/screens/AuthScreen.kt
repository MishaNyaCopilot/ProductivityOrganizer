package com.example.productivityorganizer.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.productivityorganizer.ui.state.AuthenticationState
import com.example.productivityorganizer.ui.viewmodels.GoogleSignInViewModel


@Composable
fun AuthScreen(
    viewModel: GoogleSignInViewModel = viewModel(),
    onSignInSuccess: () -> Unit
) {
    val authenticationState by viewModel.authenticationState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // <--- НОВОЕ: Получаем ActivityContext
    val context = LocalContext.current
    val activity = context as Activity
    // --->

    LaunchedEffect(authenticationState) {
        when (val state = authenticationState) {
            is AuthenticationState.Success -> {
                onSignInSuccess()
            }
            is AuthenticationState.Error -> {
                snackbarHostState.showSnackbar(message = state.message)
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Добавляем название приложения
                Text(
                    text = "ProdOrganizer", // Имя приложения из strings.xml или константы
                    style = MaterialTheme.typography.displaySmall, // Более крупный и заметный стиль
                    fontWeight = FontWeight.Bold, // Жирный шрифт
                    color = MaterialTheme.colorScheme.primary, // Используем основной цвет темы
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Ваш личный помощник по продуктивности", // Короткое описание
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 48.dp) // Больший отступ перед кнопкой
                )

                Text(
                    text = "Войдите, чтобы продолжить",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                when (authenticationState) {
                    is AuthenticationState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                    }
                    else -> { // Idle и Error состояния, где показываем кнопку
                        Button(
                            onClick = { viewModel.signInWithGoogleCredentialManager(activity) }, // <--- ИЗМЕНЕНО: Передаем activity
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp), // Консистентная высота кнопки
                            shape = RoundedCornerShape(8.dp), // Закругленные углы
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary, // Используем основной цвет
                                contentColor = MaterialTheme.colorScheme.onPrimary // Цвет текста на кнопке
                            )
                        ) {
                            Text("Войти с Google", style = MaterialTheme.typography.titleMedium) // Увеличим размер текста
                        }
                    }
                }
            }
        }
    }
}