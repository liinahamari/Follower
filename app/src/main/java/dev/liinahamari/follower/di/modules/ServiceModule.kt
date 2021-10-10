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

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Context.*
import android.hardware.SensorManager
import android.location.LocationManager
import android.os.BatteryManager
import android.os.PowerManager
import android.view.WindowManager
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.Reusable
import javax.inject.Named
import javax.inject.Singleton

@Module
open class ServiceModule {
    @SuppressLint("InlinedApi")
    @Provides
    @Singleton
    fun provideBatteryManager(@Named(APP_CONTEXT) context: Context): BatteryManager? = context.getSystemService(BATTERY_SERVICE) as BatteryManager?

    @Provides
    @Singleton
    fun provideWakeLock(@Named(APP_CONTEXT) context: Context): PowerManager.WakeLock = (context.getSystemService(POWER_SERVICE) as PowerManager)
        .newWakeLock(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, javaClass.simpleName)

    @Provides
    @Singleton
    fun provideKeyguardManager(@Named(APP_CONTEXT) context: Context): KeyguardManager = context.getSystemService(KEYGUARD_SERVICE) as KeyguardManager

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