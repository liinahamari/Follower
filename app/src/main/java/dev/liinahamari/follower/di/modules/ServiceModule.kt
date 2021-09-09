/*
Copyright 2020-2021 liinahamari

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package dev.liinahamari.follower.di.modules

import android.app.AlarmManager
import android.app.Application
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
    open fun provideLocationManager(app: Application): LocationManager = app.applicationContext.getSystemService(Service.LOCATION_SERVICE) as LocationManager

    @Provides
    @Singleton
    open fun provideNotificationManager(app: Application): NotificationManager = app.applicationContext.getSystemService(NotificationManager::class.java) as NotificationManager

    @Provides
    @Singleton
    fun provideSensorManager(app: Application): SensorManager = app.applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    @Provides
    @Singleton
    fun provideAlarmManager(app: Application): AlarmManager = app.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @Provides
    @Reusable
    fun provideWorkManager(app: Application): WorkManager = WorkManager.getInstance(app.applicationContext)
}