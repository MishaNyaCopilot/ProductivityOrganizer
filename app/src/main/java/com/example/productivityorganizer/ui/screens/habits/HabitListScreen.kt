package com.example.productivityorganizer.ui.screens.habits

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.productivityorganizer.data.model.habit.Habit
import com.example.productivityorganizer.ui.viewmodels.HabitViewModel

@Composable
fun HabitListScreen(
    viewModel: HabitViewModel = hiltViewModel(),
    onNavigateToAddHabit: () -> Unit,
    onNavigateToEditHabit: (Int) -> Unit,
    onNavigateToCalendar: () -> Unit
) {
    val habits by viewModel.allHabits.collectAsState()

    // УДАЛЕНЫ Scaffold и TopAppBar. Компонент теперь является частью NavHost в HomeScreen.
    Column(modifier = Modifier.fillMaxSize()) { // paddingValues теперь автоматически применяются к NavHost в HomeScreen
        // Ранее удаленная кнопка "Календарь" также остается удаленной отсюда.

        if (habits.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Привычек пока нет. Нажмите '+' чтобы добавить!")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(habits) { habit ->
                    HabitItem(habit = habit, onClick = { onNavigateToEditHabit(habit.id) })
                }
            }
        }
    }
}

@Composable
fun HabitItem(habit: Habit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick), // Card itself is clickable
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        // <--- ИЗМЕНЕНИЕ ЦВЕТА ФОНА КАРТОЧКИ ---
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)) // <--- ИСПОЛЬЗУЕМ surfaceColorAtElevation
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp) // Inner padding
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = habit.name, style = MaterialTheme.typography.titleMedium)
                habit.description?.let {
                    if (it.isNotBlank()) {
                        Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(text = "Тип: ${if (habit.type == "daily") "Ежедневно" else "Еженедельно"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}