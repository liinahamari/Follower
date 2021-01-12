package com.example.follower

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(CHANNEL_ID, "GPS tracker", NotificationManager.IMPORTANCE_DEFAULT))
    }

    companion object {
        lateinit var INSTANCE: App
    }
}