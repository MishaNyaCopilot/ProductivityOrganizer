package com.example.productivityorganizer.data.model.achievement

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String,
    val iconResId: String, // Store name of the drawable resource
    val criteriaType: String, // e.g., "tasks_completed", "habit_streak", "pomodoro_sessions"
    val criteriaValue: Int
)
