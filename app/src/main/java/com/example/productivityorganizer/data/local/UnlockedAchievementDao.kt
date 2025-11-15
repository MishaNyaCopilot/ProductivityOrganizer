package com.example.productivityorganizer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.productivityorganizer.data.model.achievement.UnlockedAchievement
import kotlinx.coroutines.flow.Flow

@Dao
interface UnlockedAchievementDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE) // Ignore if already unlocked
    suspend fun unlockAchievement(unlockedAchievement: UnlockedAchievement)

    @Query("SELECT * FROM unlocked_achievements")
    fun getUnlockedAchievements(): Flow<List<UnlockedAchievement>>

    @Query("SELECT * FROM unlocked_achievements WHERE achievementId = :achievementId")
    fun isAchievementUnlocked(achievementId: Int): Flow<UnlockedAchievement?>

    @Query("SELECT COUNT(*) FROM unlocked_achievements WHERE achievementId = :achievementId")
    suspend fun getUnlockedCount(achievementId: Int): Int
}
