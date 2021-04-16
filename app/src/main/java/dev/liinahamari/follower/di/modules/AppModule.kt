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
import com.scottyab.rootbeer.RootBeer
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
    fun provideRootBeer(@Named(APP_CONTEXT) context: Context): RootBeer = RootBeer(context)

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
