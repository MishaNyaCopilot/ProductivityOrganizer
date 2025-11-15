package com.example.productivityorganizer.ui.screens.habits

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.productivityorganizer.ui.viewmodels.HabitViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCalendarScreen(
    viewModel: HabitViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val habitsForSelectedDay by viewModel.habitsForDay.collectAsState()
    var currentCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }

    LaunchedEffect(selectedDate) {
        Log.d("HabitCalendarScreen", "Selected date changed: ${SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(selectedDate.time)}. Loading habits.")
        viewModel.loadHabitsForDay(
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )
    }

    LaunchedEffect(currentCalendar) {
        // If currentCalendar month/year changes, re-select the day within that month to trigger reload for new month data
        // This ensures the selected date is always visually within the displayed month
        val currentMonthDay = selectedDate.get(Calendar.DAY_OF_MONTH)
        val maxDayInNewMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val newDay = if (currentMonthDay > maxDayInNewMonth) maxDayInNewMonth else currentMonthDay

        selectedDate = Calendar.getInstance().apply {
            set(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), newDay)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        Log.d("HabitCalendarScreen", "Current calendar month/year changed to ${SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentCalendar.time)}. Selected day adjusted to $newDay. Reloading habits for selected day.")
        // No explicit call to loadHabitsForDay here, as it's already triggered by the selectedDate LaunchedEffect
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Календарь Привычек") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Month navigation header
            MonthNavigationHeader(
                calendar = currentCalendar,
                onPreviousMonth = {
                    currentCalendar = Calendar.getInstance().apply {
                        time = currentCalendar.time
                        add(Calendar.MONTH, -1)
                    }
                },
                onNextMonth = {
                    currentCalendar = Calendar.getInstance().apply {
                        time = currentCalendar.time
                        add(Calendar.MONTH, 1)
                    }
                }
            )

            // Calendar grid
            CalendarGrid(
                calendar = currentCalendar,
                selectedDate = selectedDate,
                onDateSelected = { day ->
                    selectedDate = Calendar.getInstance().apply {
                        set(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), day)
                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Habits for selected day
            Text(
                text = "Привычки на ${SimpleDateFormat("d MMMM, yyyy", Locale.getDefault()).format(selectedDate.time)}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            if (habitsForSelectedDay.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Для этого дня привычки не запланированы или не определены.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(habitsForSelectedDay) { habitWithCompletion ->
                        HabitCompletionItem(
                            habitName = habitWithCompletion.habit.name,
                            isCompleted = habitWithCompletion.isCompleted,
                            onToggle = {
                                viewModel.toggleHabitCompletion(
                                    habitId = habitWithCompletion.habit.id,
                                    date = selectedDate.timeInMillis,
                                    isCompleted = !habitWithCompletion.isCompleted
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthNavigationHeader(
    calendar: Calendar,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Filled.ArrowLeft, "Предыдущий Месяц")
        }
        Text(
            text = monthFormat.format(calendar.time),
            style = MaterialTheme.typography.headlineSmall
        )
        IconButton(onClick = onNextMonth) {
            Icon(Icons.Filled.ArrowRight, "Следующий Месяц")
        }
    }
}

@Composable
fun CalendarGrid(
    calendar: Calendar,
    selectedDate: Calendar,
    onDateSelected: (Int) -> Unit // Day of month
) {
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfMonth = Calendar.getInstance().apply {
        time = calendar.time
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val dayOfWeekOfFirstDay = firstDayOfMonth.get(Calendar.DAY_OF_WEEK)
    val emptyCellsBefore = (dayOfWeekOfFirstDay - Calendar.SUNDAY + 7) % 7

    val dayCells = mutableListOf<String>()
    for (i in 0 until emptyCellsBefore) {
        dayCells.add("")
    }
    for (i in 1..daysInMonth) {
        dayCells.add(i.toString())
    }

    Column {
        // Days of the week header
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
            val daysOfWeek = listOf("Вс", "Пн", "Вт", "Ср", "Чт", "Пт", "Сб")
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Greyed out for header
                )
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(dayCells) { day ->
                if (day.isNotEmpty()) {
                    val dayInt = day.toInt()
                    val dateForCell = Calendar.getInstance().apply {
                        set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), dayInt)
                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    }

                    val isSelected = dayInt == selectedDate.get(Calendar.DAY_OF_MONTH) &&
                            calendar.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) &&
                            calendar.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)

                    val isToday = remember {
                        val today = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                        }
                        dateForCell.timeInMillis == today.timeInMillis
                    }

                    Surface( // Используем Surface вместо Box для Material Design стиля
                        modifier = Modifier
                            .aspectRatio(1f) // Make cells square
                            .clickable { onDateSelected(dayInt) },
                        shape = MaterialTheme.shapes.small, // Используем скругленные углы из темы
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.primary // Выбранный день
                            isToday -> MaterialTheme.colorScheme.tertiaryContainer // Сегодняшний день
                            else -> MaterialTheme.colorScheme.surfaceVariant // Обычный день
                        },
                        shadowElevation = if (isSelected || isToday) 4.dp else 0.dp // Небольшая тень для выделения
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day,
                                style = MaterialTheme.typography.bodyLarge, // Увеличим размер текста дня
                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary // Текст на выбранном дне
                                    isToday -> MaterialTheme.colorScheme.onTertiaryContainer // Текст на сегодняшнем дне
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant // Текст на обычном дне
                                }
                            )
                        }
                    }
                } else {
                    Spacer(Modifier.aspectRatio(1f)) // Empty cell
                }
            }
        }
    }
}

@Composable
fun HabitCompletionItem(
    habitName: String,
    isCompleted: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        // Изменяем здесь
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.primaryContainer // Более заметный цвет для выполненных
            else MaterialTheme.colorScheme.surface // Стандартный фон для невыполненных
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = habitName,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                // Изменяем цвет текста в зависимости от состояния
                color = if (isCompleted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Checkbox(
                checked = isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}