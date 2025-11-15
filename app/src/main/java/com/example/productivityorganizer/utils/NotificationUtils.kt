package com.example.productivityorganizer.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.productivityorganizer.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.example.productivityorganizer.utils.Constants.NOTIFICATION_CHANNEL_NAME


fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = NOTIFICATION_CHANNEL_NAME
        val descriptionText = "Channel for task reminder notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        Log.d("NotificationUtils", "Notification channel '${NOTIFICATION_CHANNEL_ID}' created.")
    } else {
        Log.d("NotificationUtils", "Notification channel creation not required for API < 26.")
    }
}

// Added Log import helper
private class Log {
    companion object {
        fun d(tag: String, message: String) {
            println("D/$tag: $message")
        }
    }
}
