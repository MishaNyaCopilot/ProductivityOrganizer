package com.example.productivityorganizer.domain.manager

import com.example.productivityorganizer.data.local.AchievementDao
import com.example.productivityorganizer.data.local.HabitCompletionDao
import com.example.productivityorganizer.data.local.HabitDao
import com.example.productivityorganizer.data.local.PomodoroSessionDao
import com.example.productivityorganizer.data.local.TaskDao
import com.example.productivityorganizer.data.local.UnlockedAchievementDao
import com.example.productivityorganizer.data.model.achievement.Achievement
import com.example.productivityorganizer.data.model.achievement.UnlockedAchievement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementManager @Inject constructor(
    private val taskDao: TaskDao,
    private val pomodoroSessionDao: PomodoroSessionDao,
    private val habitDao: HabitDao,
    private val habitCompletionDao: HabitCompletionDao,
    private val achievementDao: AchievementDao,
    private val unlockedAchievementDao: UnlockedAchievementDao
) {

    // Using GlobalScope for now for simplicity in initialize.
    // In a real app, this should be tied to Application scope or a specific CoroutineScope.
    init {
        GlobalScope.launch(Dispatchers.IO) {
            populateInitialAchievements()
        }
    }

    private suspend fun populateInitialAchievements() {
        if (achievementDao.getAchievementsCount() == 0) {
            val initialAchievements = listOf(
                Achievement(name = "Первая задача!", description = "Выполните свою самую первую задачу.", iconResId = "ic_achievement_task_1", criteriaType = "tasks_completed", criteriaValue = 1),
                Achievement(name = "Новичок в задачах", description = "Выполните 5 задач.", iconResId = "ic_achievement_task_5", criteriaType = "tasks_completed", criteriaValue = 5),
                Achievement(name = "Ученик по задачам", description = "Выполните 10 задач.", iconResId = "ic_achievement_task_10", criteriaType = "tasks_completed", criteriaValue = 10),
                Achievement(name = "Подмастерье по задачам", description = "Выполните 25 задач.", iconResId = "ic_achievement_task_25", criteriaType = "tasks_completed", criteriaValue = 25),
                Achievement(name = "Эксперт по задачам", description = "Выполните 50 задач.", iconResId = "ic_achievement_task_50", criteriaType = "tasks_completed", criteriaValue = 50),

                Achievement(name = "Начинающий Помодоро", description = "Завершите свою первую сессию Помодоро.", iconResId = "ic_achievement_pomodoro_1", criteriaType = "pomodoro_sessions", criteriaValue = 1),
                Achievement(name = "Мастер Тайм-боксинга", description = "Завершите 5 сессий Помодоро.", iconResId = "ic_achievement_pomodoro_5", criteriaType = "pomodoro_sessions", criteriaValue = 5),
                Achievement(name = "Фанатик Фокуса", description = "Завершите 10 сессий Помодоро.", iconResId = "ic_achievement_pomodoro_10", criteriaType = "pomodoro_sessions", criteriaValue = 10),
                Achievement(name = "Мастер Сессий", description = "Завершите 25 сессий Помодоро.", iconResId = "ic_achievement_pomodoro_25", criteriaType = "pomodoro_sessions", criteriaValue = 25),
                Achievement(name = "Король Помодоро", description = "Завершите 50 сессий Помодоро.", iconResId = "ic_achievement_pomodoro_50", criteriaType = "pomodoro_sessions", criteriaValue = 50),

                Achievement(name = "Начало Привычки", description = "Поддерживайте любую привычку в течение 3 дней подряд.", iconResId = "ic_achievement_habit_3", criteriaType = "habit_streak_generic", criteriaValue = 3),
                Achievement(name = "Постоянные Привычки", description = "Поддерживайте любую привычку в течение 7 дней подряд.", iconResId = "ic_achievement_habit_7", criteriaType = "habit_streak_generic", criteriaValue = 7),
                Achievement(name = "Герой Привычек", description = "Поддерживайте любую привычку в течение 21 дня подряд.", iconResId = "ic_achievement_habit_21", criteriaType = "habit_streak_generic", criteriaValue = 21),
                Achievement(name = "Чемпион Серий", description = "Поддерживайте любую привычку в течение 30 дней подряд.", iconResId = "ic_achievement_habit_30", criteriaType = "habit_streak_generic", criteriaValue = 30)
            )
            achievementDao.insertAll(initialAchievements)
        }
    }

    suspend fun checkAndUnlockAchievements(userId: String) { // Assuming userId might be needed later, not used in current local logic
        val allAchievements = achievementDao.getAllAchievements().firstOrNull() ?: return

        for (achievement in allAchievements) {
            if (unlockedAchievementDao.getUnlockedCount(achievement.id) > 0) {
                continue // Already unlocked
            }

            val criteriaMet = when (achievement.criteriaType) {
                "tasks_completed" -> checkTasksCompletedCriteria(achievement.criteriaValue, userId)
                "pomodoro_sessions" -> checkPomodoroSessionsCriteria(achievement.criteriaValue, userId)
                "habit_streak_generic" -> checkGenericHabitStreakCriteria(achievement.criteriaValue, userId)
                else -> false
            }

            if (criteriaMet) {
                unlockedAchievementDao.unlockAchievement(
                    UnlockedAchievement(achievementId = achievement.id, unlockedDate = System.currentTimeMillis())
                )
            }
        }
    }

    private suspend fun checkTasksCompletedCriteria(requiredCount: Int, userId: String): Boolean {
        // Assuming TaskDao has a method to count completed tasks for a user.
        // For local DB, userId might not be directly used in TaskDao queries if it's single-user.
        // Modifying this to use a more generic count if no user specific query exists.
        val completedTasks = taskDao.getCompletedTasksCount() // Example method, adjust if DAO differs
        return completedTasks >= requiredCount
    }

    private suspend fun checkPomodoroSessionsCriteria(requiredCount: Int, userId: String): Boolean {
        // Assuming PomodoroSessionDao has a method to count all sessions.
        val completedSessions = pomodoroSessionDao.getAllSessionsCount() // Example method
        return completedSessions >= requiredCount
    }

    private suspend fun checkGenericHabitStreakCriteria(requiredStreak: Int, userId: String): Boolean {
        val habits = habitDao.getAllHabitsList() // Non-flow version for one-off check
        if (habits.isEmpty()) return false

        for (habit in habits) {
            val completions = habitCompletionDao.getCompletionsForHabitSorted(habit.id)
            if (completions.size < requiredStreak) continue

            var currentStreak = 0
            var expectedDate = Calendar.getInstance() // Start from today for checking backwards

            // Normalize expectedDate to the start of the day
            expectedDate.set(Calendar.HOUR_OF_DAY, 0)
            expectedDate.set(Calendar.MINUTE, 0)
            expectedDate.set(Calendar.SECOND, 0)
            expectedDate.set(Calendar.MILLISECOND, 0)

            // Iterate completions backwards to find a streak ending "today" or "yesterday" effectively
            // This is a simplified check. A more robust one would consider the last completion date.
            // For this implementation, we check if a streak of 'requiredStreak' days exists.

            val completionDates = completions.filter { it.isCompleted }.map {
                val cal = Calendar.getInstance()
                cal.timeInMillis = it.date
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }.distinct().sortedDescending() // Ensure dates are unique and sorted

            if (completionDates.size < requiredStreak) continue

            // Check for N consecutive days from the list of completionDates
            var consecutiveDays = 0
            for (i in 0 until completionDates.size) {
                if (i == 0) {
                    consecutiveDays = 1
                } else {
                    val diff = completionDates[i-1] - completionDates[i] // Difference in millis
                    if (diff == TimeUnit.DAYS.toMillis(1)) {
                        consecutiveDays++
                    } else if (diff > TimeUnit.DAYS.toMillis(1)) {
                        // Streak broken, reset if we are looking for any N-day streak
                        // If we need the streak to be *current*, logic needs more specific date anchoring.
                        // For "any N-day streak ever", this reset is fine.
                        consecutiveDays = 1
                    } // If diff is 0 (same day), it's handled by distinct()
                }
                if (consecutiveDays >= requiredStreak) {
                    return true
                }
            }
        }
        return false
    }
}
