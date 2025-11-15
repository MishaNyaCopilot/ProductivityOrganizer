package com.example.productivityorganizer.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.productivityorganizer.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTask(task: Task)

    //@Insert(onConflict = OnConflictStrategy.REREPLACE) // Если Room 2.4.0+
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<Task>) // НОВЫЙ МЕТОД: вставляет/обновляет список задач

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY createdAt DESC")
    fun getCompletedTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): Task?

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("UPDATE tasks SET priorityLevel = :priorityLevel WHERE id = :taskId")
    suspend fun updateTaskPriorityLevel(taskId: String, priorityLevel: String)

    @Query("SELECT * FROM tasks WHERE priorityLevel = 'Highest' AND isCompleted = 0 ORDER BY createdAt DESC")
    fun getHighestPriorityTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE priorityLevel = 'High' AND isCompleted = 0 ORDER BY createdAt DESC")
    fun getHighPriorityTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE priorityLevel = 'Medium' AND isCompleted = 0 ORDER BY createdAt DESC")
    fun getMediumPriorityTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE priorityLevel = 'Low' AND isCompleted = 0 ORDER BY createdAt DESC")
    fun getLowPriorityTasks(): Flow<List<Task>>

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1")
    suspend fun getCompletedTasksCount(): Int
}