package com.example.productivityorganizer.ui.screens.achievement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.productivityorganizer.data.model.achievement.DisplayAchievement
import com.example.productivityorganizer.ui.viewmodels.AchievementViewModel

// Removed ExperimentalMaterial3Api import if not used by other composables in this file,
// but keeping @OptIn for now if other Material3 components still require it.
// import androidx.compose.material3.ExperimentalMaterial3Api // Keep if other M3 components need it.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    viewModel: AchievementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // УДАЛЕНЫ Scaffold и TopAppBar. Компонент теперь является частью NavHost в HomeScreen.
    Box(
        modifier = Modifier
            .fillMaxSize()
            // .padding(paddingValues) // paddingValues теперь автоматически применяются к NavHost в HomeScreen
            .padding(16.dp) // Сохраняем внутренние отступы для контента
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.error != null -> {
                Text(
                    text = "Error: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            uiState.achievements.isEmpty() -> {
                Text(
                    text = "Достижений пока нет. Продолжайте использовать приложение, чтобы разблокировать их!",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                AchievementsGrid(achievements = uiState.achievements)
            }
        }
    }
}

@Composable
fun AchievementsGrid(achievements: List<DisplayAchievement>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // Display 2 achievements per row
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(achievements) { achievement ->
            AchievementItem(displayAchievement = achievement)
        }
    }
}

@Composable
fun AchievementItem(displayAchievement: DisplayAchievement) {
    val achievement = displayAchievement.achievement
    val isUnlocked = displayAchievement.isUnlocked
    val unlockedDate = displayAchievement.unlockedDate

    val cardColors = if (isUnlocked) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    } else {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    }

    val iconToShow = if (isUnlocked) Icons.Filled.EmojiEvents else Icons.Filled.Lock
    val iconColor = if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f), // Make items square-ish
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = cardColors
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // TODO: Implement dynamic icon loading based on achievement.iconResId
            Icon(
                imageVector = iconToShow,
                contentDescription = if (isUnlocked) "Разблокировано" else "Заблокировано",
                modifier = Modifier.size(48.dp),
                tint = iconColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = achievement.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = if (isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = achievement.description,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = if (isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            if (isUnlocked && unlockedDate != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Разблокировано: ${java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(unlockedDate))}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }
        }
    }
}
