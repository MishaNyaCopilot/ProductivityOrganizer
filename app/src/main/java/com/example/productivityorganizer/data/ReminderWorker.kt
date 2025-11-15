package com.example.productivityorganizer.data

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.productivityorganizer.MainActivity // Assuming MainActivity is your entry point
import com.example.productivityorganizer.R // Required for R.drawable. if you use custom icons
import com.example.productivityorganizer.utils.Constants

class ReminderWorker(
    private val appContext: Context, // Made it a property for easier access
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "ReminderWorker started for task ID: ${inputData.getString(Constants.TASK_ID_KEY)}")

        val taskId = inputData.getString(Constants.TASK_ID_KEY)
        val taskTitle = inputData.getString(Constants.TASK_TITLE_KEY)
        val taskDescription = inputData.getString(Constants.TASK_DESC_KEY)

        if (taskId.isNullOrEmpty() || taskTitle.isNullOrEmpty()) {
            Log.e(TAG, "Task ID or Title is null/empty. Cannot show notification.")
            return Result.failure()
        }

        // Create an Intent to open MainActivity
        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("task_id_from_notification", taskId) // Pass task ID
        }

        // Create PendingIntent
        val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(appContext, taskId.hashCode(), intent, pendingIntentFlag)

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(appContext, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Using a default Android icon
            // .setSmallIcon(R.drawable.ic_notification_bell) // Example if you have a custom icon
            .setContentTitle(taskTitle)
            .setContentText(taskDescription ?: "You have a task due!") // Default text if description is null
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Notification disappears when tapped

        // Display the notification
        val notificationManager = NotificationManagerCompat.from(appContext)
        val notificationId = taskId.hashCode() // Using taskId's hashcode as notification ID

        try {
            // Check for POST_NOTIFICATIONS permission before notifying (Android 13+)
            // This check is ideally done when scheduling or at app start,
            // but as a safeguard for the worker:
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                 // The permission check itself would typically be done using ContextCompat.checkSelfPermission
                 // and then requesting if not granted. Here, we assume it's granted for the worker to proceed.
                 // If not granted, notify() might fail silently or throw an error depending on OS version.
            }
            notificationManager.notify(notificationId, notificationBuilder.build())
            Log.d(TAG, "Notification displayed for task: $taskTitle (ID: $notificationId)")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: Missing POST_NOTIFICATIONS permission? ${e.message}")
            // Depending on the app's error handling strategy, could retry or log as failure.
            // For now, if it fails here, it's a failure for this work instance.
            return Result.failure()
        }


        return Result.success()
    }

    companion object {
        const val TAG = "ReminderWorker"
    }
}
