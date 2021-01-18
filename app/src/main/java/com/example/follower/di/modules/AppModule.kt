package com.example.follower.di.modules

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.example.follower.FollowerApp
import com.example.follower.helper.FlightRecorder
import com.example.follower.helper.rx.BaseComposers
import com.example.follower.helper.rx.BaseSchedulerProvider
import com.example.follower.helper.rx.SchedulersProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {
    @Provides
    @Singleton
    fun bindContext(app: FollowerApp): Context = app.applicationContext

    @Provides
    @Singleton
    fun bindSharedPrefs(context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    @Singleton
    fun bindComposers(schedulers: SchedulersProvider, logger: FlightRecorder): BaseComposers = BaseComposers(schedulers, logger)

    @Provides
    @Singleton
    fun bindSchedulers(): SchedulersProvider = BaseSchedulerProvider()
}
