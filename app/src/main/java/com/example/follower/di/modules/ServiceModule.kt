package com.example.follower.di.modules

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.hardware.SensorManager
import android.location.LocationManager
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.Reusable
import javax.inject.Singleton

@Module
open class ServiceModule {
    @Provides
    @Singleton
    open fun provideLocationManager(context: Context): LocationManager = context.getSystemService(Service.LOCATION_SERVICE) as LocationManager

    @Provides
    @Singleton
    open fun provideNotificationManager(context: Context): NotificationManager = context.getSystemService(NotificationManager::class.java) as NotificationManager

    @Provides
    @Singleton
    fun provideSensorManager(context: Context): SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    @Provides
    @Singleton
    fun provideAlarmManager(context: Context): AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @Provides
    @Reusable
    fun provideWorkManager(context: Context): WorkManager = WorkManager.getInstance(context)
}