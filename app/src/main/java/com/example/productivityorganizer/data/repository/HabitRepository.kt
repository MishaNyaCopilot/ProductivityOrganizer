package com.example.productivityorganizer.data.repository

import android.util.Log // Import Log for debugging
import com.example.productivityorganizer.data.local.HabitDao
import com.example.productivityorganizer.data.local.HabitCompletionDao
import com.example.productivityorganizer.data.model.habit.Habit
import com.example.productivityorganizer.data.model.habit.HabitCompletion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.Calendar // Import Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor(
    private val habitDao: HabitDao,
    private val habitCompletionDao: HabitCompletionDao
) {

    // Helper function to get start of day timestamp - Made it internal
    internal fun getStartOfDayMillis(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    fun getAllHabits(): Flow<List<Habit>> = habitDao.getAllHabits()

    fun getHabitById(id: Int): Flow<Habit?> = habitDao.getHabitById(id)

    suspend fun addHabit(habit: Habit) {
        habitDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit)
    }

    suspend fun markHabitCompleted(habitId: Int, date: Long, isCompleted: Boolean) {
        val normalizedDate = getStartOfDayMillis(date) // Normalize the date to start of day

        Log.d("HabitRepository", "markHabitCompleted: habitId=$habitId, date=$date (normalized to $normalizedDate), isCompleted=$isCompleted")
        println("HabitRepository: markHabitCompleted: habitId=$habitId, date=$date (normalized to $normalizedDate), isCompleted=$isCompleted")


        // Use firstOrNull() to get the current value from the Flow
        val existingCompletion = habitCompletionDao.getCompletion(habitId, normalizedDate).firstOrNull()
        Log.d("HabitRepository", "Existing completion for habitId=$habitId, normalizedDate=$normalizedDate: $existingCompletion")
        println("HabitRepository: Existing completion for habitId=$habitId, normalizedDate=$normalizedDate: $existingCompletion")


        if (existingCompletion != null) {
            if (existingCompletion.isCompleted != isCompleted) {
                val updatedCompletion = existingCompletion.copy(
                    isCompleted = isCompleted,
                    completionTime = if (isCompleted) System.currentTimeMillis() else null
                )
                habitCompletionDao.updateCompletion(updatedCompletion)
                Log.d("HabitRepository", "Updated existing completion: $updatedCompletion")
                println("HabitRepository: Updated existing completion: $updatedCompletion")
            } else {
                Log.d("HabitRepository", "Status already matches desired state ($isCompleted). No update needed.")
                println("HabitRepository: Status already matches desired state ($isCompleted). No update needed.")
            }
        } else {
            if (isCompleted) {
                val newCompletion = HabitCompletion(
                    habitId = habitId,
                    date = normalizedDate, // Use normalized date for insertion
                    isCompleted = true,
                    completionTime = System.currentTimeMillis()
                )
                habitCompletionDao.insertCompletion(newCompletion)
                Log.d("HabitRepository", "Inserted new completion: $newCompletion")
                println("HabitRepository: Inserted new completion: $newCompletion")
            } else {
                Log.d("HabitRepository", "No existing completion and desired state is false. No action needed.")
                println("HabitRepository: No existing completion and desired state is false. No action needed.")
            }
        }
    }

    fun getHabitCompletionsForDateRange(habitId: Int, startDate: Long, endDate: Long): Flow<List<HabitCompletion>> {
        // You might need to normalize startDate and endDate here too if they are not already start/end of day
        return habitCompletionDao.getCompletionsForHabitBetweenDates(habitId, startDate, endDate)
    }

    fun getCompletionsForDate(date: Long): Flow<Map<Int, HabitCompletion>> {
        val normalizedDate = getStartOfDayMillis(date) // Normalize date for query
        return habitCompletionDao.getAllCompletionsByDate(normalizedDate).map { completions ->
            completions.associateBy { it.habitId }
        }
    }

    fun getCompletionForHabitOnDate(habitId: Int, date: Long): Flow<HabitCompletion?> {
        val normalizedDate = getStartOfDayMillis(date) // Normalize date for query
        return habitCompletionDao.getCompletion(habitId, normalizedDate)
    }

    suspend fun clearAllHabitCompletions() {
        habitCompletionDao.deleteAllCompletions()
        Log.d("HabitRepository", "All habit completions cleared.")
    }
}