package com.example.productivityorganizer.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.productivityorganizer.data.model.habit.HabitCompletion
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCompletionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletion): Long

    @Update
    suspend fun updateCompletion(completion: HabitCompletion)

    @Delete
    suspend fun deleteCompletion(completion: HabitCompletion)

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date = :date")
    fun getCompletion(habitId: Int, date: Long): Flow<HabitCompletion?>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getCompletionsForHabitBetweenDates(habitId: Int, startDate: Long, endDate: Long): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions WHERE date = :date")
    fun getAllCompletionsByDate(date: Long): Flow<List<HabitCompletion>>

    @Query("DELETE FROM habit_completions") // Добавлено для очистки
    suspend fun deleteAllCompletions()

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY date DESC")
    suspend fun getCompletionsForHabitSorted(habitId: Int): List<HabitCompletion>
}