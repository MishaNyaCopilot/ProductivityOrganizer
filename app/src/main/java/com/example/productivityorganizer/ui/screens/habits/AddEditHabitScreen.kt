package com.example.productivityorganizer.ui.screens.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.productivityorganizer.ui.viewmodels.HabitViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHabitScreen(
    viewModel: HabitViewModel = hiltViewModel(),
    habitId: Int?,
    onNavigateBack: () -> Unit
) {
    val isEditing = habitId != null
    val habitToEdit by viewModel.selectedHabit.collectAsState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("daily") } // "daily" or "weekly"
    // var goal by remember { mutableStateOf("") }
    var selectedDaysOfWeek by remember { mutableStateOf(setOf<Int>()) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = habitId) {
        if (isEditing && habitId != null) {
            viewModel.loadHabitDetails(habitId)
        }
    }

    LaunchedEffect(key1 = habitToEdit) {
        if (isEditing && habitToEdit != null) {
            name = habitToEdit!!.name
            description = habitToEdit!!.description ?: ""
            type = habitToEdit!!.type
            // goal = habitToEdit!!.goal?.toString() ?: ""
            selectedDaysOfWeek = habitToEdit!!.selectedDaysOfWeek ?: setOf()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Изменить Привычку" else "Новая Привычка") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { showDeleteConfirmationDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Удалить Привычку", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                // val goalInt = goal.toIntOrNull()
                if (name.isNotBlank()) {
                    if (isEditing && habitToEdit != null) {
                        viewModel.updateHabit(
                            habitToEdit!!.copy(
                                name = name,
                                description = description.ifBlank { null },
                                type = type,
                                // goal = goalInt,
                                selectedDaysOfWeek = if (type == "weekly") selectedDaysOfWeek.takeIf { it.isNotEmpty() } else null
                            )
                        )
                    } else {
                        viewModel.addHabit(
                            name = name,
                            description = description.ifBlank { null },
                            type = type,
                            // goal = goalInt,
                            selectedDaysOfWeek = if (type == "weekly") selectedDaysOfWeek.takeIf { it.isNotEmpty() } else null
                        )
                    }
                    onNavigateBack()
                }
            }) {
                Icon(Icons.Filled.Check, contentDescription = "Сохранить Привычку")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // TextField для названия привычки
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название привычки*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
            // TextField для описания
            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание (необязательно)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )

            // Выбор типа привычки (Ежедневно/Еженедельно)
            // ИСПОЛЬЗУЕМ TOGGLE BUTTONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Отступ между кнопками
            ) {
                // Label for the options
                Text(
                    text = "Тип:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                // "Ежедневно" button/chip
                FilterChip( // FilterChip для визуального стиля
                    selected = type == "daily",
                    onClick = { type = "daily" },
                    label = { Text("Ежедневно") },
                    modifier = Modifier.weight(1f), // Занимает доступное пространство
                    shape = RoundedCornerShape(8.dp), // Закругленные углы
                    colors = FilterChipDefaults.filterChipColors( // Цвета для выбранного/невыбранного состояния
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = null // Убираем стандартную границу, чтобы выглядело как в AddTaskScreen
                )

                // "Еженедельно" button/chip
                FilterChip( // FilterChip для визуального стиля
                    selected = type == "weekly",
                    onClick = { type = "weekly" },
                    label = { Text("Еженедельно") },
                    modifier = Modifier.weight(1f), // Занимает доступное пространство
                    shape = RoundedCornerShape(8.dp), // Закругленные углы
                    colors = FilterChipDefaults.filterChipColors( // Цвета для выбранного/невыбранного состояния
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = null // Убираем стандартную границу
                )
            }


            if (type == "weekly") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Дни недели:",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.Start)
                )
                // Row для выбора дней недели
                FlowRow( // FlowRow для автоматического переноса на следующую строку
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalArrangement = Arrangement.spacedBy(8.dp) // Отступ между строками чипов
                ) {
                    val days = listOf("Вс", "Пн", "Вт", "Ср", "Чт", "Пт", "Сб")
                    days.forEachIndexed { index, dayName ->
                        val dayOfWeekConstant = (index + Calendar.SUNDAY - 1 + 7) % 7 + 1
                        val isDaySelected = selectedDaysOfWeek.contains(dayOfWeekConstant)
                        FilterChip(
                            selected = isDaySelected,
                            onClick = {
                                selectedDaysOfWeek = if (isDaySelected) {
                                    selectedDaysOfWeek - dayOfWeekConstant
                                } else {
                                    selectedDaysOfWeek + dayOfWeekConstant
                                }
                            },
                            label = { Text(dayName) },
                            modifier = Modifier.padding(horizontal = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = null
                        )
                    }
                }
            }

            Text("*Обязательное поле", style = MaterialTheme.typography.bodySmall)
        }

        if (showDeleteConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmationDialog = false },
                title = { Text("Подтвердить удаление") },
                text = { Text("Вы уверены, что хотите удалить привычку \"${habitToEdit?.name}\"?") },
                confirmButton = {
                    TextButton(onClick = {
                        habitToEdit?.let { viewModel.deleteHabit(it) }
                        showDeleteConfirmationDialog = false
                        onNavigateBack()
                    }) {
                        Text("Удалить", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmationDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}