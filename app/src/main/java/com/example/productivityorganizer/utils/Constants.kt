package com.example.productivityorganizer.utils

object Constants {
    // For ReminderWorker Data
    const val TASK_ID_KEY = "task_id"
    const val TASK_TITLE_KEY = "task_title"
    const val TASK_DESC_KEY = "task_description"

    // For Notification Channel
    const val NOTIFICATION_CHANNEL_ID = "task_reminders"
    const val NOTIFICATION_CHANNEL_NAME = "Task Reminders"
    const val NOTIFICATION_ID_PREFIX = "task_reminder_" // Prefix for notification ID

    // For WorkManager Unique Work
    const val REMINDER_WORK_TAG_PREFIX = "reminder_"
}
