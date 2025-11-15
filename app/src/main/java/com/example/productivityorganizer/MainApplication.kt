package com.example.productivityorganizer

import android.app.Application
import com.example.productivityorganizer.utils.createNotificationChannel
import android.util.Log
import dagger.hilt.android.HiltAndroidApp // Импортируйте HiltAndroidApp

@HiltAndroidApp // Добавьте эту аннотацию
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("MainApplication", "Application onCreate called.")
        createNotificationChannel(this)
    }
}