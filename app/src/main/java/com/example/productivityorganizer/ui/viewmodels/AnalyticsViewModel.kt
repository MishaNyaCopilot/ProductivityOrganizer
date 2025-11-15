package com.example.productivityorganizer.ui.viewmodels

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.example.productivityorganizer.data.local.TaskDao
import com.example.productivityorganizer.data.local.PomodoroSessionDao
import com.example.productivityorganizer.data.model.Task
import com.example.productivityorganizer.data.local.PomodoroSession
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneOffset
import java.time.DayOfWeek

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val pomodoroSessionDao: PomodoroSessionDao
) : ViewModel() {

    // --- StateFlows for UI ---

    // Bar chart data: List of pairs (DayOfWeek Name, Count)
    private val _tasksCompletedDaily = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val tasksCompletedDaily: StateFlow<List<Pair<String, Int>>> = _tasksCompletedDaily.asStateFlow()

    // Pie chart data: List of pairs (Session Type, Total Duration in minutes)
    private val _pomodoroSessionDistribution = MutableStateFlow<List<Pair<String, Float>>>(emptyList())
    val pomodoroSessionDistribution: StateFlow<List<Pair<String, Float>>> = _pomodoroSessionDistribution.asStateFlow()

    // Overall progress
    private val _totalCompletedTasks = MutableStateFlow(0)
    val totalCompletedTasks: StateFlow<Int> = _totalCompletedTasks.asStateFlow()

    private val _totalPomodoroFocusMinutes = MutableStateFlow(0L) // Minutes
    val totalPomodoroFocusMinutes: StateFlow<Long> = _totalPomodoroFocusMinutes.asStateFlow()

    init {
        loadTasksCompletedDaily()
        loadPomodoroSessionDistribution()
    }

    private fun loadTasksCompletedDaily() {
        viewModelScope.launch {
            taskDao.getCompletedTasks().collect { tasks ->
                _totalCompletedTasks.value = tasks.size // Обновляем общее количество

                val tasksByDay = tasks
                    .filter { it.completedAt != null } // Убедимся, что completedAt не null
                    .groupBy { task ->
                        try {
                            // task.completedAt это String?, содержащий ISO timestamp.
                            // Используем DateTimeFormatter.ISO_OFFSET_DATE_TIME, так как Instant.now().toString() дает Z (смещение)
                            java.time.LocalDateTime.parse(task.completedAt!!, java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME).dayOfWeek
                        } catch (e: Exception) {
                            System.err.println("Error parsing completedAt for task ${task.id}: ${task.completedAt} - ${e.message}")
                            null // Пропускаем задачи с невалидной датой завершения
                        }
                    }
                    .filterKeys { it != null } // Удаляем null ключи (задачи с ошибками парсинга)
                    .mapValues { entry -> entry.value.size } // Считаем количество задач для каждого DayOfWeek

                val dailyCounts = java.time.DayOfWeek.values().map { dayOfWeek ->
                    Pair(dayOfWeek.name.take(3).uppercase(), tasksByDay[dayOfWeek] ?: 0)
                }
                _tasksCompletedDaily.value = dailyCounts
            }
        }
    }

    private fun loadPomodoroSessionDistribution() {
        viewModelScope.launch {
            pomodoroSessionDao.getAllSessions().collect { sessions ->
                var totalFocusMs = 0L
                var totalBreakMs = 0L
                sessions.forEach { session ->
                    if (session.type.equals("focus", ignoreCase = true)) {
                        totalFocusMs += session.duration
                    } else if (session.type.equals("break", ignoreCase = true)) {
                        totalBreakMs += session.duration
                    }
                }
                val totalFocusMinutes = totalFocusMs / 60000L
                val totalBreakMinutes = totalBreakMs / 60000L

                _totalPomodoroFocusMinutes.value = totalFocusMinutes

                val distribution = mutableListOf<Pair<String, Float>>()
                if (totalFocusMinutes > 0) distribution.add(Pair("Focus", totalFocusMinutes.toFloat()))
                if (totalBreakMinutes > 0) distribution.add(Pair("Break", totalBreakMinutes.toFloat()))

                _pomodoroSessionDistribution.value = distribution
            }
        }
    }
}
