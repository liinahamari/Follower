package dev.liinahamari.follower.di.modules

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dev.liinahamari.follower.FollowerApp
import dev.liinahamari.follower.helper.FlightRecorder
import dev.liinahamari.follower.helper.rx.BaseComposers
import dev.liinahamari.follower.helper.rx.BaseSchedulerProvider
import dev.liinahamari.follower.helper.rx.SchedulersProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dev.liinahamari.follower.R
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
    fun bindComposers(schedulers: SchedulersProvider, logger: FlightRecorder): BaseComposers = BaseComposers(schedulers, logger)

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
