package com.example.follower

import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.multidex.MultiDexApplication
import com.example.follower.di.AppComponent
import com.example.follower.di.DaggerAppComponent

class FollowerApp: MultiDexApplication() {
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        setupDagger()
        super.onCreate()
        INSTANCE = this
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(NotificationChannel(CHANNEL_ID, "GPS tracker", NotificationManager.IMPORTANCE_DEFAULT))
    }

    private fun setupDagger() {
        appComponent = DaggerAppComponent.builder()
            .application(this)
            .build()
    }

    companion object {
        lateinit var INSTANCE: FollowerApp
    }
}