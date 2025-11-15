package com.example.productivityorganizer.di

import android.content.Context
import androidx.room.Room
import com.example.productivityorganizer.data.local.AchievementDao
import com.example.productivityorganizer.data.local.AppDatabase
import com.example.productivityorganizer.data.local.HabitCompletionDao
import com.example.productivityorganizer.data.local.HabitDao
import com.example.productivityorganizer.data.local.MIGRATION_10_11
import com.example.productivityorganizer.data.local.MIGRATION_11_12
import com.example.productivityorganizer.data.local.MIGRATION_8_9
import com.example.productivityorganizer.data.local.MIGRATION_9_10
import com.example.productivityorganizer.data.local.PomodoroSessionDao
import com.example.productivityorganizer.data.local.TaskDao
import com.example.productivityorganizer.data.local.UnlockedAchievementDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "productivity_organizer_database"
        ).addMigrations(MIGRATION_8_9)
            .addMigrations(MIGRATION_9_10)
            .addMigrations(MIGRATION_10_11)
            .addMigrations(MIGRATION_11_12) // <--- ДОБАВИТЬ НОВУЮ МИГРАЦИЮ
            .build()
    }

    @Singleton
    @Provides
    fun providePomodoroSessionDao(database: AppDatabase): PomodoroSessionDao {
        return database.pomodoroSessionDao()
    }

    @Provides
    @Singleton // Added Singleton annotation for consistency
    fun provideTaskDao(appDatabase: AppDatabase): TaskDao {
        return appDatabase.taskDao()
    }

    @Provides
    @Singleton // Added Singleton annotation
    fun provideHabitDao(appDatabase: AppDatabase): HabitDao { // Added provider
        return appDatabase.habitDao()
    }

    @Provides
    @Singleton // Added Singleton annotation
    fun provideHabitCompletionDao(appDatabase: AppDatabase): HabitCompletionDao { // Added provider
        return appDatabase.habitCompletionDao()
    }

    @Provides
    @Singleton
    fun provideAchievementDao(appDatabase: AppDatabase): AchievementDao { // Added provider
        return appDatabase.achievementDao()
    }

    @Provides
    @Singleton
    fun provideUnlockedAchievementDao(appDatabase: AppDatabase): UnlockedAchievementDao { // Added provider
        return appDatabase.unlockedAchievementDao()
    }
}