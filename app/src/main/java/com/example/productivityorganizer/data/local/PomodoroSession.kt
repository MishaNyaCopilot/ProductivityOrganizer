package com.example.productivityorganizer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,
    val duration: Long, // Duration in milliseconds
    val type: String // "focus" or "break"
)
