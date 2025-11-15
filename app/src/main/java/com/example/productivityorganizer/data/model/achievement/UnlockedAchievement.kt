package com.example.productivityorganizer.data.model.achievement

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "unlocked_achievements",
    foreignKeys = [
        ForeignKey(
            entity = Achievement::class,
            parentColumns = ["id"],
            childColumns = ["achievementId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UnlockedAchievement(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val achievementId: Int,
    val unlockedDate: Long // Timestamp
)
