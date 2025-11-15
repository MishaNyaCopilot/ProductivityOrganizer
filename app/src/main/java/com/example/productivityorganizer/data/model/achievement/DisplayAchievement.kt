package com.example.productivityorganizer.data.model.achievement

data class DisplayAchievement(
    val achievement: Achievement,
    val isUnlocked: Boolean,
    val unlockedDate: Long?
)
