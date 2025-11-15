package com.example.productivityorganizer.data.model.habit

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String? = null,
    val type: String, // "daily" or "weekly"
    val creationDate: Long = System.currentTimeMillis(),
    // val goal: Int? = null, // УДАЛЯЕМ ЭТУ СТРОКУ
    val selectedDaysOfWeek: Set<Int>? = null // NEW: 1=Sunday, 2=Monday, ..., 7=Saturday
)

class Converters {
    @TypeConverter
    fun fromSetInt(set: Set<Int>?): String {
        return Gson().toJson(set)
    }

    @TypeConverter
    fun toSetInt(json: String?): Set<Int>? {
        if (json == null) {
            return null
        }
        val type = object : TypeToken<Set<Int>>() {}.type
        return Gson().fromJson(json, type)
    }
}