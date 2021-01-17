package com.example.follower.di

import android.content.Context
import android.hardware.SensorManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ServiceModule {
    @Provides
    @Singleton
    fun provideSensorManager(context: Context): SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
}