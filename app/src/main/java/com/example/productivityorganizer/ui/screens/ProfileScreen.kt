// app/src/main/java/com/example/productivityorganizer/ui/screens/profile/ProfileScreen.kt
package com.example.productivityorganizer.ui.screens.profile

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.productivityorganizer.data.model.achievement.DisplayAchievement
import com.example.productivityorganizer.ui.navigation.AppDestinations
import com.example.productivityorganizer.ui.state.AuthenticationState
import com.example.productivityorganizer.ui.viewmodels.AchievementViewModel
import com.example.productivityorganizer.ui.viewmodels.GoogleSignInViewModel
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.text.SimpleDateFormat
import java.util.Locale

private const val TAG = "ProfileScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: GoogleSignInViewModel = hiltViewModel(),
    achievementViewModel: AchievementViewModel = hiltViewModel()
) {
    val authState by authViewModel.authenticationState.collectAsStateWithLifecycle()
    val achievementUiState by achievementViewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (val state = authState) {
            is AuthenticationState.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            is AuthenticationState.Error -> {
                item {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            is AuthenticationState.Success -> {
                val user = state.user
                val userName = user?.userMetadata?.get("full_name")?.jsonPrimitive?.contentOrNull
                    ?: user?.userMetadata?.get("name")?.jsonPrimitive?.contentOrNull
                    ?: user?.email
                    ?: "Пользователь"
                val userEmail = user?.email ?: "N/A"
                // Извлекаем URL аватарки. Google обычно использует 'picture' или 'avatar_url'.
                val userAvatarUrl = user?.userMetadata?.get("picture")?.jsonPrimitive?.contentOrNull
                    ?: user?.userMetadata?.get("avatar_url")?.jsonPrimitive?.contentOrNull

                item {
                    // User Profile Section
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (userAvatarUrl != null && userAvatarUrl.isNotBlank()) {
                            AsyncImage(
                                model = userAvatarUrl,
                                contentDescription = "User Avatar",
                                modifier = Modifier
                                    .size(96.dp) // Размер аватара
                                    .clip(CircleShape), // Делаем его круглым
                                contentScale = ContentScale.Crop // Обрезаем, чтобы заполнить круг
                            )
                        } else {
                            // Если URL аватарки нет, показываем иконку по умолчанию
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Default User Profile Icon",
                                modifier = Modifier.size(96.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "Привет, $userName!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    // Buttons for Analytics and Sign Out
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Кнопка аналитики
                        Button(
                            onClick = { navController.navigate(AppDestinations.ANALYTICS_SCREEN_ROUTE) },
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(Icons.Filled.Analytics, contentDescription = "Статистика", modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Статистика", style = MaterialTheme.typography.labelLarge)
                        }

                        // Кнопка выхода из аккаунта
                        Button(
                            onClick = {
                                Log.d(TAG, "Sign Out button clicked.")
                                authViewModel.signOut()
                            },
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Выйти", modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Выйти", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))

                    // Achievements Section Header
                    Text(
                        text = "Мои Достижения",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Achievements Grid
                if (achievementUiState.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (achievementUiState.error != null) {
                    item {
                        Text(
                            text = "Ошибка загрузки достижений: ${achievementUiState.error}",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else if (achievementUiState.achievements.isEmpty()) {
                    item {
                        Text(
                            text = "Нет доступных достижений. Продолжайте использовать приложение, чтобы разблокировать их!",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                        )
                    }
                } else {
                    item {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(4.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(calculateGridHeight(achievementUiState.achievements.size))
                        ) {
                            items(achievementUiState.achievements) { achievement ->
                                AchievementItem(displayAchievement = achievement)
                            }
                        }
                    }
                }
            }
            is AuthenticationState.Idle -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Загрузка профиля...",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

// Helper function to calculate height for LazyVerticalGrid inside LazyColumn
@Composable
private fun calculateGridHeight(itemCount: Int): Dp {
    val itemsPerRow = 2
    val rowCount = (itemCount + itemsPerRow - 1) / itemsPerRow
    val itemHeight = 160.dp
    val verticalArrangement = 12.dp
    return (rowCount * itemHeight) + ((rowCount - 1) * verticalArrangement) + 24.dp
}

@Composable
fun AchievementItem(displayAchievement: DisplayAchievement) {
    val achievement = displayAchievement.achievement
    val isUnlocked = displayAchievement.isUnlocked
    val unlockedDate = displayAchievement.unlockedDate

    val cardColors = if (isUnlocked) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    } else {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    }

    val iconToShow = if (isUnlocked) Icons.Filled.EmojiEvents else Icons.Filled.Lock
    val iconColor = if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    val textColor = if (isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val descriptionColor = if (isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    val dateColor = if (isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = cardColors,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Icon(
                imageVector = iconToShow,
                contentDescription = if (isUnlocked) "Разблокировано" else "Заблокировано",
                modifier = Modifier
                    .size(40.dp)
                    .padding(top = 4.dp),
                tint = iconColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = achievement.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = textColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = achievement.description,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = descriptionColor
            )
            if (isUnlocked && unlockedDate != null) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Разблокировано: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(java.util.Date(unlockedDate))}",
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                    color = dateColor
                )
            }
        }
    }
}