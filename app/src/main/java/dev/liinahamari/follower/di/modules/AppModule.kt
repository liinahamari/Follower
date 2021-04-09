package dev.liinahamari.follower.di.modules

import android.app.AlarmManager
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.work.WorkManager
import dev.liinahamari.follower.FollowerApp
import dev.liinahamari.follower.R
import dev.liinahamari.follower.helper.rx.BaseComposers
import dev.liinahamari.follower.helper.rx.BaseSchedulerProvider
import dev.liinahamari.follower.helper.rx.SchedulersProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dev.liinahamari.follower.helper.FlightRecorder
import dev.liinahamari.follower.screens.settings.AutoTrackingSchedulingUseCase
import dev.liinahamari.follower.screens.settings.SettingsScope
import javax.inject.Named
import javax.inject.Singleton

const val APP_CONTEXT = "application_context"
const val UID = "userID"

@Module
class AppModule {
    @Provides
    @Singleton
    @Named(APP_CONTEXT)
    fun bindContext(app: FollowerApp): Context = app.applicationContext

    @Provides
    @Reusable
    fun provideAutoTrackingSchedulingUseCase(prefs: SharedPreferences, @Named(APP_CONTEXT) ctx: Context, composers: BaseComposers, logger: FlightRecorder, alarmManager: AlarmManager): AutoTrackingSchedulingUseCase =
        AutoTrackingSchedulingUseCase(prefs, ctx, composers, alarmManager, logger)

    @Provides
    @Singleton
    fun bindSharedPrefs(@Named(APP_CONTEXT) context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    @Singleton
    fun bindComposers(schedulers: SchedulersProvider, logger: FlightRecorder): BaseComposers = BaseComposers(schedulers, logger)

    @Provides
    @Singleton
    fun bindSchedulers(): SchedulersProvider = BaseSchedulerProvider()

    @Provides
    @Singleton
    @Named(UID) /* https://developer.android.com/training/articles/user-data-ids.html */
    fun provideUID(sharedPreferences: SharedPreferences, @Named(APP_CONTEXT) context: Context): String = sharedPreferences.getString(context.getString(R.string.pref_uid), null)!!

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
}
