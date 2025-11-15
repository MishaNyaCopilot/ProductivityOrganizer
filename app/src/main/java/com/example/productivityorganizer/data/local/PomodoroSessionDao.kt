package com.example.productivityorganizer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroSessionDao {
    @Insert
    suspend fun insert(session: PomodoroSession): Long

    @Query("SELECT * FROM pomodoro_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<PomodoroSession>>

    @Query("SELECT * FROM pomodoro_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): PomodoroSession?

    @Query("DELETE FROM pomodoro_sessions")
    suspend fun clearAllSessions() // Added for testing or reset purposes

    @Query("SELECT COUNT(*) FROM pomodoro_sessions")
    suspend fun getAllSessionsCount(): Int
}
