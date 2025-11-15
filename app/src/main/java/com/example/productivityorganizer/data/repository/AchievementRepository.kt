package com.example.productivityorganizer.data.repository

import com.example.productivityorganizer.data.local.AchievementDao
import com.example.productivityorganizer.data.local.UnlockedAchievementDao
import com.example.productivityorganizer.data.model.achievement.Achievement
import com.example.productivityorganizer.data.model.achievement.DisplayAchievement
import com.example.productivityorganizer.data.model.achievement.UnlockedAchievement
import com.example.productivityorganizer.domain.manager.AchievementManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepository @Inject constructor(
    private val achievementDao: AchievementDao,
    private val unlockedAchievementDao: UnlockedAchievementDao,
    private val achievementManager: AchievementManager // To ensure it's initialized for population
) {

    fun getAllAchievementsWithStatus(): Flow<List<DisplayAchievement>> {
        // Ensure AchievementManager is initialized (Hilt does this for Singletons)
        // The manager populates initial achievements in its init block.

        val allAchievementsFlow: Flow<List<Achievement>> = achievementDao.getAllAchievements()
        val unlockedAchievementsFlow: Flow<List<UnlockedAchievement>> = unlockedAchievementDao.getUnlockedAchievements()

        return allAchievementsFlow.combine(unlockedAchievementsFlow) { achievements, unlocked ->
            val unlockedMap = unlocked.associateBy { it.achievementId }
            achievements.map { achievement ->
                val unlockedEntry = unlockedMap[achievement.id]
                DisplayAchievement(
                    achievement = achievement,
                    isUnlocked = unlockedEntry != null,
                    unlockedDate = unlockedEntry?.unlockedDate
                )
            }
        }
    }

    // Expose the manager's check function if needed by ViewModel directly, or handle calls from other ViewModels
    suspend fun checkAndUnlockAchievements(userId: String = "") { // Default empty userId for local ops
        achievementManager.checkAndUnlockAchievements(userId)
    }
}
