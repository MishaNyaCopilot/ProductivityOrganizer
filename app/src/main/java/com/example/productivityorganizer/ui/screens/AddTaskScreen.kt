package com.example.productivityorganizer.ui.screens

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.DatePicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.productivityorganizer.data.model.Category
import com.example.productivityorganizer.ui.dialogs.AddCategoryDialog
import com.example.productivityorganizer.ui.navigation.AppDestinations
import com.example.productivityorganizer.ui.viewmodels.CategoryUIState
import com.example.productivityorganizer.ui.viewmodels.TaskViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Helper for Time Input Row (moved from EditTaskScreen for consistency)
@Composable
private fun TimeInputRow(
    label: String,
    selectedTime: Calendar?, // Full Calendar object, will be converted to Long for storage
    onFieldClick: () -> Unit,
    isEnabled: Boolean = true // Добавляем параметр enabled
) {
    val timeSdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(enabled = isEnabled, onClick = onFieldClick) // Использование enabled
            .background(
                color = if (isEnabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 0.dp,
                color = Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Text(
                    text = selectedTime?.let { timeSdf.format(it.time) } ?: "Выбрать время",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(
                Icons.Filled.Schedule,
                "Выбрать время",
                tint = if (isEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    navController: NavController,
    taskViewModel: TaskViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDateCalendar by remember { mutableStateOf<Calendar?>(null) } // Calendar for Date (day only)
    var endTimeCalendar by remember { mutableStateOf<Calendar?>(null) } // Calendar for Time (full datetime)

    var priorityLevel by remember { mutableStateOf("Medium") }
    val priorityLevels = listOf("Highest", "High", "Medium", "Low")
    var priorityMenuExpanded by remember { mutableStateOf(false) }

    var selectedReminder by remember { mutableStateOf("Нет") }
    val reminderOptions = listOf("None", "15 minutes before", "1 hour before", "1 day before")
    var reminderMenuExpanded by remember { mutableStateOf(false) }

    val categoryUiState by taskViewModel.categoryUIState.collectAsState()
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val isoDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // For dueDate (day only)
    val displayDateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    val showDatePicker = remember { mutableStateOf(false) }
    val showTimePicker = remember { mutableStateOf(false) } // For endTime

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    val notificationsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("AddTaskScreen", "POST_NOTIFICATIONS permission granted.")
        } else {
            Log.d("AddTaskScreen", "POST_NOTIFICATIONS permission denied. Reminders require this permission.")
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Разрешение на уведомления отклонено. Напоминания работать не будут.")
            }
        }
    }

    LaunchedEffect(Unit) {
        taskViewModel.fetchCategories()
    }

    // --- Date Picker Logic ---
    LaunchedEffect(showDatePicker.value) {
        if (showDatePicker.value) {
            val calendar = dueDateCalendar ?: Calendar.getInstance()

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                context,
                { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
                    val selectedDateOnly = Calendar.getInstance().apply {
                        set(selectedYear, selectedMonth, selectedDayOfMonth)
                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    }
                    dueDateCalendar = selectedDateOnly
                    // Если пользователь выбрал новую дату, сбрасываем время
                    endTimeCalendar = null
                    showDatePicker.value = false
                },
                year, month, dayOfMonth
            )
            datePickerDialog.setOnDismissListener {
                Log.d("AddTaskScreen", "DatePickerDialog dismissed.")
                showDatePicker.value = false
            }
            datePickerDialog.show()
        }
    }

    // --- Time Picker Logic for endTime ---
    LaunchedEffect(showTimePicker.value) {
        if (showTimePicker.value) {
            // Если дата не выбрана, но пользователь кликнул на время,
            // по умолчанию используем сегодняшнюю дату и текущее время.
            val baseCalendar = dueDateCalendar ?: Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val initialTime = endTimeCalendar ?: Calendar.getInstance() // Use existing end time or current time

            val currentHour = initialTime.get(Calendar.HOUR_OF_DAY)
            val currentMinute = initialTime.get(Calendar.MINUTE)

            TimePickerDialog(
                context,
                { _, selectedHour: Int, selectedMinute: Int ->
                    // Создаем новый Calendar на основе выбранной даты (или сегодняшней),
                    // и устанавливаем выбранное время
                    val newEndTime = baseCalendar.clone() as Calendar
                    newEndTime.set(Calendar.HOUR_OF_DAY, selectedHour)
                    newEndTime.set(Calendar.MINUTE, selectedMinute)
                    newEndTime.set(Calendar.SECOND, 0)
                    newEndTime.set(Calendar.MILLISECOND, 0)

                    endTimeCalendar = newEndTime
                    showTimePicker.value = false
                },
                currentHour,
                currentMinute,
                true // 24 hour view
            ).apply {
                setOnDismissListener { showTimePicker.value = false }
                show()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Добавить новое задание") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название*") },
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

            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание") },
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

            // --- Due Date Selection ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable { showDatePicker.value = true }
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        width = 0.dp,
                        color = Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Срок выполнения",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = dueDateCalendar?.let { displayDateFormatter.format(it.time) } ?: "Выбрать срок",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(
                        Icons.Filled.DateRange,
                        "Выбрать дату",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // --- End Time Selection ---
            TimeInputRow(
                label = "Время (необязательно)",
                selectedTime = endTimeCalendar,
                onFieldClick = { showTimePicker.value = true },
                isEnabled = dueDateCalendar != null // Только если дата выбрана
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBox(
                    expanded = priorityMenuExpanded,
                    onExpandedChange = { priorityMenuExpanded = !priorityMenuExpanded }
                ) {
                    TextField(
                        value = priorityLevel,
                        onValueChange = {},
                        label = { Text("Приоритет") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityMenuExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
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
                    ExposedDropdownMenu(
                        expanded = priorityMenuExpanded,
                        onDismissRequest = { priorityMenuExpanded = false }
                    ) {
                        priorityLevels.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    priorityLevel = selectionOption
                                    priorityMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBox(
                    expanded = reminderMenuExpanded,
                    onExpandedChange = { reminderMenuExpanded = !reminderMenuExpanded }
                ) {
                    TextField(
                        value = selectedReminder,
                        onValueChange = {},
                        label = { Text("Напоминание") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reminderMenuExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        enabled = dueDateCalendar != null || endTimeCalendar != null, // Напоминание только если есть дата/время
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
                    ExposedDropdownMenu(
                        expanded = reminderMenuExpanded,
                        onDismissRequest = { reminderMenuExpanded = false }
                    ) {
                        reminderOptions.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    if (selectionOption != "None") {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            when (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)) {
                                                PackageManager.PERMISSION_GRANTED -> {
                                                    Log.d("AddTaskScreen", "Notification permission already granted.")
                                                    selectedReminder = selectionOption
                                                    taskViewModel.updateSelectedReminderOption(selectionOption)
                                                }
                                                else -> {
                                                    notificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                }
                                            }
                                        } else {
                                            selectedReminder = selectionOption
                                            taskViewModel.updateSelectedReminderOption(selectionOption)
                                        }
                                    } else {
                                        selectedReminder = selectionOption
                                        taskViewModel.updateSelectedReminderOption(selectionOption)
                                    }
                                    reminderMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBox(
                    expanded = categoryMenuExpanded,
                    onExpandedChange = {
                        if (categoryUiState !is CategoryUIState.Loading || (categoryUiState as? CategoryUIState.Loading)?.let { selectedCategory != null } == true) {
                            categoryMenuExpanded = !categoryMenuExpanded
                        }
                    }
                ) {
                    TextField(
                        value = selectedCategory?.name ?: "Выберите категорию",
                        onValueChange = {},
                        label = { Text("Категория") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        enabled = categoryUiState !is CategoryUIState.Loading,
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
                    ExposedDropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Нет") },
                            onClick = {
                                selectedCategory = null
                                categoryMenuExpanded = false
                            }
                        )
                        when (val state = categoryUiState) {
                            is CategoryUIState.Success -> {
                                if (state.categories.isEmpty()) {
                                    DropdownMenuItem(text = { Text("Категорий пока нет. Добавьте!") }, enabled = false, onClick = {})
                                } else {
                                    state.categories.forEach { category ->
                                        DropdownMenuItem(
                                            text = { Text(category.name) },
                                            onClick = {
                                                selectedCategory = category
                                                categoryMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            is CategoryUIState.Loading -> { DropdownMenuItem(text = { Text("Загрузка категорий...") }, enabled = false, onClick = {}) }
                            is CategoryUIState.Error -> { DropdownMenuItem(text = { Text(state.message, color = MaterialTheme.colorScheme.error) }, enabled = false, onClick = {}) }
                            is CategoryUIState.Idle -> { DropdownMenuItem(text = { Text("Получение категорий...") }, enabled = false, onClick = {}) }
                        }
                    }
                }
            }

            TextButton(
                onClick = { showAddCategoryDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp)
            ) {
                Text("+ Новая категория", style = MaterialTheme.typography.labelLarge)
            }


            if (showAddCategoryDialog) {
                AddCategoryDialog(
                    taskViewModel = taskViewModel,
                    onDismissRequest = { showAddCategoryDialog = false },
                    onCategoryAdded = { showAddCategoryDialog = false }
                )
            }

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        isLoading = true

                        val formattedDueDate = dueDateCalendar?.let { isoDateFormatter.format(it.time) }
                        val endTimeMillis = endTimeCalendar?.timeInMillis // Сохраняем как Long

                        taskViewModel.addTask(
                            title = title,
                            description = description.ifEmpty { null },
                            dueDate = formattedDueDate,
                            priorityLevel = priorityLevel,
                            categoryId = selectedCategory?.id,
                            reminderOption = selectedReminder,
                            endTime = endTimeMillis      // Передаем endTime
                        )
                        navController.navigate(AppDestinations.HOME_SCREEN_ROUTE) {
                            popUpTo(AppDestinations.HOME_SCREEN_ROUTE) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Название не может быть пустым")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = title.isNotBlank() && !isLoading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Сохранить задание", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}