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

package dev.liinahamari.follower.model

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dev.liinahamari.follower.R
import dev.liinahamari.follower.ext.*
import dev.liinahamari.follower.helper.rx.BaseComposers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.util.*
import javax.inject.Inject

private const val DEFAULT_AUTO_TRACKING_START_TIME = "09:00"
private const val DEFAULT_AUTO_TRACKING_END_TIME = "21:00"

//todo to RxSharedPreferences?
class PreferencesRepository @Inject constructor(private val sharedPreferences: SharedPreferences, private val app: Application, private val baseComposers: BaseComposers) {
    /** If App is launching first time, then set default preferences*/
    fun applyDefaultPreferences() {
        if (sharedPreferences.getBoolean(app.getString(R.string.pref_is_first_launch), false).not()) {
            sharedPreferences.writeBooleanOf(app.getString(R.string.pref_is_first_launch), true)
            sharedPreferences.writeBooleanOf(app.getString(R.string.pref_battery_optimization), app.isIgnoringBatteryOptimizations())

            sharedPreferences.writeBooleanOf(app.getString(R.string.pref_acra_disable), true)
            sharedPreferences.writeBooleanOf(app.getString(R.string.pref_root_is_ok), false)
            sharedPreferences.writeStringOf(app.getString(R.string.pref_uid), UUID.randomUUID().toString())

            PreferenceManager.setDefaultValues(app, R.xml.preferences, false)
            sharedPreferences.writeIntOf(app.getString(R.string.pref_tracking_start_time), hourlyTimeToMinutesFromMidnight(DEFAULT_AUTO_TRACKING_START_TIME))
            sharedPreferences.writeIntOf(app.getString(R.string.pref_tracking_stop_time), hourlyTimeToMinutesFromMidnight(DEFAULT_AUTO_TRACKING_END_TIME))
        }
    }

    fun getTrackDisplayMode(): Single<String> = Single.just(app.getString(R.string.pref_track_display_mode))
        .map { sharedPreferences.getStringOf(it) ?: app.getString(R.string.pref_value_track_display_mode_none) }
        .onErrorReturnItem(app.getString(R.string.pref_value_track_display_mode_none))
        .compose(baseComposers.applySingleSchedulers())

    fun saveTrackDisplayMode(mode: String): Completable = Completable.fromCallable { sharedPreferences.writeStringOf(app.getString(R.string.pref_track_display_mode), mode) }
        .compose(baseComposers.applyCompletableSchedulers())

    fun getPersistedLocale(): Single<Locale> = Single.just(Locale(sharedPreferences.getStringOf(app.getString(R.string.pref_lang)) ?: Locale.UK.language))
        .onErrorReturnItem(Locale.UK)

    fun getPersistedTrackRepresentation(): Single<PersistedTrackResult> = Single.just(app.getString(R.string.pref_track_representation))
        .map { sharedPreferences.getStringOf(it) }
        .onErrorResumeNext {
            Single.fromCallable { sharedPreferences.writeStringOf(app.getString(R.string.pref_track_representation), app.getString(R.string.pref_line)) }
                .map { sharedPreferences.getStringOf(app.getString(R.string.pref_track_representation)) }
        }
        .map<PersistedTrackResult> {
            when (it) {
                app.getString(R.string.pref_marker_set) -> PersistedTrackResult.Success(app.getString(R.string.pref_marker_set))
                app.getString(R.string.pref_line) -> PersistedTrackResult.Success(app.getString(R.string.pref_line))
                else -> PersistedTrackResult.Success(app.getString(R.string.pref_line)).also {
                    sharedPreferences.writeStringOf(app.getString(R.string.pref_track_representation), app.getString(R.string.pref_line))
                }
            }
        }
        .onErrorReturn { PersistedTrackResult.Failure }

    fun incrementAppLaunchCounter() = sharedPreferences.incrementAppLaunchCounter(app)
}

sealed class PersistedTrackResult {
    data class Success(val value: String) : PersistedTrackResult()
    object Failure : PersistedTrackResult()
}