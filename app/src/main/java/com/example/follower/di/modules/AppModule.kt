package com.example.follower.di.modules

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.example.follower.FollowerApp
import com.example.follower.R
import com.example.follower.helper.FlightRecorder
import com.example.follower.helper.rx.BaseComposers
import com.example.follower.helper.rx.BaseSchedulerProvider
import com.example.follower.helper.rx.SchedulersProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

const val APP_CONTEXT = "application_context"
const val UID = "userID"

@Module
class AppModule {
    @Provides
    @Singleton
//    @Named(APP_CONTEXT) TODO
    fun bindContext(app: FollowerApp): Context = app.applicationContext

    @Provides
    @Singleton
    fun bindSharedPrefs(context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    @Singleton
    fun bindComposers(schedulers: SchedulersProvider): BaseComposers = BaseComposers(schedulers)

    @Provides
    @Singleton
    fun bindSchedulers(): SchedulersProvider = BaseSchedulerProvider()

    @Provides
    @Singleton
    @Named(UID) /* https://developer.android.com/training/articles/user-data-ids.html */
    fun provideUID(sharedPreferences: SharedPreferences, context: Context): String = sharedPreferences.getString(context.getString(R.string.pref_uid), null)!!

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
}
