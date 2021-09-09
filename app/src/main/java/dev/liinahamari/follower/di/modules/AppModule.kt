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
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.scottyab.rootbeer.RootBeer
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dev.liinahamari.follower.R
import dev.liinahamari.follower.helper.rx.BaseComposers
import dev.liinahamari.follower.helper.rx.BaseSchedulerProvider
import dev.liinahamari.follower.helper.rx.SchedulersProvider
import dev.liinahamari.follower.screens.settings.AutoTrackingSchedulingUseCase
import javax.inject.Named
import javax.inject.Singleton

const val UID = "userID"

@Module
class AppModule {
    @Provides
    @Singleton
    fun provideRootBeer(app: Application): RootBeer = RootBeer(app)

    @Provides
    @Reusable
    fun provideAutoTrackingSchedulingUseCase(prefs: SharedPreferences, app: Application, composers: BaseComposers, alarmManager: AlarmManager): AutoTrackingSchedulingUseCase =
        AutoTrackingSchedulingUseCase(prefs, app, composers, alarmManager)

    @Provides
    @Singleton
    fun bindSharedPrefs(app: Application): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(app)

    @Provides
    @Singleton
    fun bindComposers(schedulers: SchedulersProvider): BaseComposers = BaseComposers(schedulers)

    @Provides
    @Singleton
    fun bindSchedulers(): SchedulersProvider = BaseSchedulerProvider()

    @Provides
    @Singleton
    @Named(UID) /* https://developer.android.com/training/articles/user-data-ids.html */
    fun provideUID(sharedPreferences: SharedPreferences, app: Application): String = sharedPreferences.getString(app.getString(R.string.pref_uid), null)!!

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
}
