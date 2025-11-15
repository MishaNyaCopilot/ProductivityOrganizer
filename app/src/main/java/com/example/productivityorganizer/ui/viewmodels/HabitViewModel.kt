package com.example.productivityorganizer.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.productivityorganizer.data.model.habit.Habit
import com.example.productivityorganizer.data.repository.HabitRepository
import com.example.productivityorganizer.domain.manager.AchievementManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val achievementManager: AchievementManager
) : ViewModel() {

    private val _allHabits = MutableStateFlow<List<Habit>>(emptyList())
    val allHabits: StateFlow<List<Habit>> = _allHabits.asStateFlow()

    private val _selectedHabit = MutableStateFlow<Habit?>(null)
    val selectedHabit: StateFlow<Habit?> = _selectedHabit.asStateFlow()

    private val _monthlyCompletions = MutableStateFlow<Map<Int, Map<Int, Boolean>>>(emptyMap())
    val monthlyCompletions: StateFlow<Map<Int, Map<Int, Boolean>>> = _monthlyCompletions.asStateFlow()

    data class HabitWithCompletion(val habit: Habit, val isCompleted: Boolean)
    private val _habitsForDay = MutableStateFlow<List<HabitWithCompletion>>(emptyList())
    val habitsForDay: StateFlow<List<HabitWithCompletion>> = _habitsForDay.asStateFlow()


    init {
        loadAllHabits()
        // Временно для отладки: раскомментируйте, если нужно очистить все завершения при старте ViewModel
        // viewModelScope.launch { habitRepository.clearAllHabitCompletions() }
    }

    fun loadAllHabits() {
        viewModelScope.launch {
            habitRepository.getAllHabits().collect { habits ->
                Log.d("HabitViewModel", "All habits loaded: ${habits.size}")
                _allHabits.value = habits
            }
        }
    }

    fun loadHabitDetails(id: Int) {
        viewModelScope.launch {
            habitRepository.getHabitById(id).collect { habit ->
                Log.d("HabitViewModel", "Habit details loaded for ID $id: ${habit?.name}")
                _selectedHabit.value = habit
            }
        }
    }

    // Updated addHabit to include selectedDaysOfWeek
    fun addHabit(name: String, description: String?, type: String, /* goal: Int?, */ selectedDaysOfWeek: Set<Int>?) {
        viewModelScope.launch {
            Log.d("HabitViewModel", "Adding habit: $name, Type: $type, Days: $selectedDaysOfWeek")
            val newHabit = Habit(
                name = name,
                description = description,
                type = type,
                // goal = goal, // УДАЛЯЕМ ЭТУ СТРОКУ
                creationDate = System.currentTimeMillis(),
                selectedDaysOfWeek = selectedDaysOfWeek
            )
            val insertedId = habitRepository.addHabit(newHabit)
            Log.d("HabitViewModel", "Habit added: $name (ID: $insertedId)")
            val today = Calendar.getInstance()
            loadHabitsForDay(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
        }
    }

    // Updated updateHabit: удаляем goal
    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            Log.d("HabitViewModel", "Updating habit: ${habit.name}, Type: ${habit.type}, Days: ${habit.selectedDaysOfWeek}")
            habitRepository.updateHabit(
                habit.copy(
                    // goal = habit.goal, // Эту строку либо удалить, либо убедиться, что goal больше не является частью Habit.
                    // Так как мы удалили поле из модели, habit.goal уже не существует.
                    // Просто убедитесь, что goal не передается в copy.
                )
            )
            Log.d("HabitViewModel", "Habit updated: ${habit.name}")
            // Если вы используете copy для Habit, просто убедитесь, что goal отсутствует в Habit
            // и тогда нет необходимости указывать его здесь.
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            Log.d("HabitViewModel", "Deleting habit: ${habit.name}")
            habitRepository.deleteHabit(habit)
            Log.d("HabitViewModel", "Habit deleted: ${habit.name}")
        }
    }

    fun toggleHabitCompletion(habitId: Int, date: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            Log.d("HabitViewModel", "toggleHabitCompletion called for habitId: $habitId, date: $date (normalized to ${habitRepository.getStartOfDayMillis(date)}), isCompleted: $isCompleted")
            habitRepository.markHabitCompleted(habitId, date, isCompleted)
            if (isCompleted) {
                achievementManager.checkAndUnlockAchievements("")
            }
            Log.d("HabitViewModel", "markHabitCompleted finished. Reloading habits for the day.")
            val calendar = Calendar.getInstance().apply { timeInMillis = date }
            loadHabitsForDay(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        }
    }

    fun loadHabitsForDay(year: Int, month: Int, day: Int) {
        viewModelScope.launch {
            Log.d("HabitViewModel", "Loading habits for day: $year-${month+1}-$day")
            val calendar = Calendar.getInstance().apply {
                set(year, month, day, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val dayTimestamp = calendar.timeInMillis
            // NEW: Get the day of the week for filtering
            // Calendar.DAY_OF_WEEK: Sunday is 1, Monday is 2, ..., Saturday is 7
            val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

            _allHabits.combine(habitRepository.getCompletionsForDate(dayTimestamp)) { habits, completionsMap ->
                val filteredHabits = habits.filter { habit ->
                    when (habit.type) {
                        "daily" -> true // Daily habits are always active
                        "weekly" -> habit.selectedDaysOfWeek?.contains(currentDayOfWeek) ?: false
                        else -> false
                    }
                }
                val listForState = filteredHabits.map { habit ->
                    val completion = completionsMap[habit.id]
                    val completionStatus = completion?.isCompleted ?: false
                    Log.d("HabitViewModel", "Habit '${habit.name}' (ID: ${habit.id}) for day ${currentDayOfWeek} filtered: ${filteredHabits.contains(habit)}. Loaded with status: $completionStatus")
                    HabitWithCompletion(
                        habit = habit,
                        isCompleted = completionStatus
                    )
                }.toMutableList()
                Log.d("HabitViewModel", "Combined list ready for day: ${listForState.size} habits. First habit status: ${listForState.firstOrNull()?.isCompleted}")
                listForState
            }.collect { combinedList ->
                _habitsForDay.value = combinedList
                Log.d("HabitViewModel", "StateFlow _habitsForDay updated with ${combinedList.size} habits for $year-${month+1}-$day. UI should re-render.")
            }
        }
    }

    fun loadCompletionsForMonth(year: Int, month: Int) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.set(year, month, 1, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val monthStartDate = calendar.timeInMillis
            calendar.add(Calendar.MONTH, 1)
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val monthEndDate = calendar.timeInMillis

        }
    }

    fun debugClearAllHabitCompletions() {
        viewModelScope.launch {
            habitRepository.clearAllHabitCompletions()
            val today = Calendar.getInstance()
            loadHabitsForDay(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
            Log.d("HabitViewModel", "Debug: All habit completions cleared and habits for today reloaded.")
        }
    }
}