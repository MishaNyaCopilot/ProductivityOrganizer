package com.example.productivityorganizer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.productivityorganizer.data.model.Task
import com.example.productivityorganizer.data.model.achievement.Achievement
import com.example.productivityorganizer.data.model.achievement.UnlockedAchievement
import com.example.productivityorganizer.data.model.habit.Converters
import com.example.productivityorganizer.data.model.habit.Habit
import com.example.productivityorganizer.data.model.habit.HabitCompletion

@Database(entities = [PomodoroSession::class,
    Task::class, Habit::class, HabitCompletion::class, Achievement::class,
    UnlockedAchievement::class], version = 12, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pomodoroSessionDao(): PomodoroSessionDao
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun habitCompletionDao(): HabitCompletionDao
    abstract fun achievementDao(): AchievementDao
    abstract fun unlockedAchievementDao(): UnlockedAchievementDao
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE tasks_new (" +
                "id TEXT NOT NULL PRIMARY KEY, " +
                "userId TEXT NOT NULL, " +
                "title TEXT NOT NULL, " +
                "description TEXT, " +
                "dueDate TEXT, " +
                "priorityLevel TEXT NOT NULL DEFAULT 'Medium', " +
                "isCompleted INTEGER NOT NULL DEFAULT 0, " +
                "createdAt TEXT NOT NULL, " +
                "completedAt TEXT, " +
                "categoryId TEXT, " +
                "reminder_option TEXT)")

        db.execSQL("INSERT INTO tasks_new (id, userId, title, description, dueDate, isCompleted, createdAt, completedAt, categoryId, reminder_option) " +
                "SELECT id, userId, title, description, dueDate, isCompleted, createdAt, completedAt, categoryId, reminder_option FROM tasks")

        db.execSQL("DROP TABLE tasks")
        db.execSQL("ALTER TABLE tasks_new RENAME TO tasks")
    }
}

// Эта миграция уже была выполнена и добавила startTime И endTime.
// Она не изменится, но ее версия уже была 10.
val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN startTime INTEGER")
        db.execSQL("ALTER TABLE tasks ADD COLUMN endTime INTEGER")
    }
}

// <--- НОВАЯ МИГРАЦИЯ 10 -> 11 ДЛЯ УДАЛЕНИЯ startTime
val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Создать новую временную таблицу БЕЗ startTime
        // (убедитесь, что порядок столбцов и типы данных совпадают с вашими текущими потребностями)
        db.execSQL("CREATE TABLE tasks_new (" +
                "id TEXT NOT NULL PRIMARY KEY, " +
                "userId TEXT NOT NULL, " +
                "title TEXT NOT NULL, " +
                "description TEXT, " +
                "dueDate TEXT, " +
                "priorityLevel TEXT NOT NULL DEFAULT 'Medium', " +
                "isCompleted INTEGER NOT NULL DEFAULT 0, " +
                "createdAt TEXT NOT NULL, " +
                "completedAt TEXT, " +
                "endTime INTEGER, " + // <-- endTime остается
                "categoryId TEXT, " +
                "reminder_option TEXT)")

        // 2. Скопировать данные из старой таблицы в новую, пропуская startTime
        // ВНИМАНИЕ: Если у вас есть другие столбцы, убедитесь, что они перечислены здесь,
        // и порядок столбцов в SELECT соответствует порядку в CREATE TABLE tasks_new.
        // Я использую тот же список, что и в CREATE TABLE tasks_new, чтобы быть уверенным.
        db.execSQL("INSERT INTO tasks_new (id, userId, title, description, dueDate, priorityLevel, isCompleted, createdAt, completedAt, endTime, categoryId, reminder_option) " +
                "SELECT id, userId, title, description, dueDate, priorityLevel, isCompleted, createdAt, completedAt, endTime, categoryId, reminder_option FROM tasks")

        // 3. Удалить старую таблицу
        db.execSQL("DROP TABLE tasks")

        // 4. Переименовать новую таблицу в оригинальное имя
        db.execSQL("ALTER TABLE tasks_new RENAME TO tasks")
    }
}

// <--- НОВАЯ МИГРАЦИЯ 11 -> 12 ДЛЯ УДАЛЕНИЯ goal из habits
val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Создать новую временную таблицу `habits_new` без столбца `goal`.
        // Убедитесь, что список столбцов и их порядок соответствуют вашей актуальной модели Habit.kt
        db.execSQL("CREATE TABLE habits_new (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "name TEXT NOT NULL, " +
                "description TEXT, " +
                "type TEXT NOT NULL, " +
                "creationDate INTEGER NOT NULL DEFAULT 0, " + // Помните, что `System.currentTimeMillis()` это Long, в SQLite INTEGER
                "selectedDaysOfWeek TEXT)") // selectedDaysOfWeek как TEXT (для Gson Converters)

        // 2. Скопировать данные из старой таблицы `habits` в `habits_new`, пропуская столбец `goal`.
        db.execSQL("INSERT INTO habits_new (id, name, description, type, creationDate, selectedDaysOfWeek) " +
                "SELECT id, name, description, type, creationDate, selectedDaysOfWeek FROM habits")

        // 3. Удалить старую таблицу `habits`.
        db.execSQL("DROP TABLE habits")

        // 4. Переименовать новую таблицу `habits_new` в `habits`.
        db.execSQL("ALTER TABLE habits_new RENAME TO habits")
    }
}