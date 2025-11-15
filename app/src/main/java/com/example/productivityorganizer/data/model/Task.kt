package com.example.productivityorganizer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity(tableName = "tasks")
@Serializable
data class Task(
    @PrimaryKey
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String? = null,
    @SerialName("due_date") val dueDate: String? = null, // Дата дедлайна (день)
    @SerialName("priority_level") val priorityLevel: String = "Medium",
    @SerialName("is_completed") val isCompleted: Boolean = false,
    @SerialName("created_at") val createdAt: String,
    @SerialName("completed_at") val completedAt: String? = null,
    // @SerialName("start_time") val startTime: Long? = null, // <--- ЭТУ СТРОКУ УДАЛИТЬ!
    @SerialName("end_time") val endTime: Long? = null,   // <--- ЭТУ СТРОКУ ОСТАВИТЬ!
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("reminder_option") val reminder_option: String? = null
)