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

package dev.liinahamari.follower.screens.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import dev.liinahamari.follower.R
import dev.liinahamari.follower.di.modules.APP_CONTEXT
import dev.liinahamari.follower.ext.writeStringOf
import dev.liinahamari.follower.helper.rx.BaseComposers
import dev.liinahamari.follower.services.location_tracking.DEFAULT_LOCATION_UPDATE_INTERVAL
import dev.liinahamari.follower.services.location_tracking.DEFAULT_TIME_UPDATE_INTERVAL
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Named

@SettingsScope
class ResetPrefsToDefaultsInteractor constructor(
    private val baseComposers: BaseComposers,
    private val sharedPreferences: SharedPreferences,
    @Named(APP_CONTEXT) private val context: Context
) {
    fun resetPrefsToDefaults(): Observable<ResetToDefaultsState> = Observable.fromCallable {
        with(sharedPreferences) {
            writeStringOf(context.getString(R.string.pref_lang), context.resources.getStringArray(R.array.supported_locales).first())
            writeStringOf(context.getString(R.string.pref_min_distance), DEFAULT_LOCATION_UPDATE_INTERVAL.toString())
            writeStringOf(context.getString(R.string.pref_min_location_update_interval), DEFAULT_TIME_UPDATE_INTERVAL.toString())
            writeStringOf(context.getString(R.string.pref_theme), AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString())
            writeStringOf(context.getString(R.string.pref_track_representation), context.resources.getStringArray(R.array.track_representing_values).first())
            writeStringOf(context.getString(R.string.pref_track_display_mode), context.resources.getStringArray(R.array.track_display_mode_values).first())
        }
    }
        .delaySubscription(1, TimeUnit.SECONDS)
        .compose(baseComposers.applyObservableSchedulers())
        .map<ResetToDefaultsState> { ResetToDefaultsState.Success }
        .onErrorReturn { ResetToDefaultsState.Failure }
        .startWithItem(ResetToDefaultsState.Loading)
}

sealed class ResetToDefaultsState {
    object Success : ResetToDefaultsState()
    object Loading : ResetToDefaultsState()
    object Failure : ResetToDefaultsState()
}