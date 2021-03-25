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
import javax.inject.Named
import javax.inject.Singleton

@Module
open class ServiceModule {
    @Provides
    @Singleton
    open fun provideLocationManager(@Named(APP_CONTEXT) context: Context): LocationManager = context.getSystemService(Service.LOCATION_SERVICE) as LocationManager

    @Provides
    @Singleton
    open fun provideNotificationManager(@Named(APP_CONTEXT) context: Context): NotificationManager = context.getSystemService(NotificationManager::class.java) as NotificationManager

    @Provides
    @Singleton
    fun provideSensorManager(@Named(APP_CONTEXT) context: Context): SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    @Provides
    @Singleton
    fun provideAlarmManager(@Named(APP_CONTEXT) context: Context): AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @Provides
    @Reusable
    fun provideWorkManager(@Named(APP_CONTEXT) context: Context): WorkManager = WorkManager.getInstance(context)
}