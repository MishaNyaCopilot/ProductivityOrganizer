package com.example.productivityorganizer.data.model.habit

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_completions",
    foreignKeys = [ForeignKey(
        entity = Habit::class,
        parentColumns = ["id"],
        childColumns = ["habitId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["habitId"]), Index(value = ["date"])]
)
data class HabitCompletion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int,
    val date: Long, // Timestamp for the specific day of completion
    var isCompleted: Boolean = false, // Changed to var for mutability and fixed typo
    val completionTime: Long? = null // Timestamp of when the habit was marked complete
)