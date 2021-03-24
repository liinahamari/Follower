package dev.liinahamari.follower.screens.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import dev.liinahamari.follower.R
import dev.liinahamari.follower.ext.writeStringOf
import dev.liinahamari.follower.helper.rx.BaseComposers
import dev.liinahamari.follower.services.DEFAULT_LOCATION_UPDATE_INTERVAL
import dev.liinahamari.follower.services.DEFAULT_TIME_UPDATE_INTERVAL
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

@SettingsScope
class SettingsPrefsInteractor constructor(private val baseComposers: BaseComposers, private val sharedPreferences: SharedPreferences, private val context: Context) {
    fun resetOptionsToDefaults(): Observable<ResetToDefaultsState> = Observable.fromCallable {
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
        .startWith(ResetToDefaultsState.Loading)
}

sealed class ResetToDefaultsState {
    object Success : ResetToDefaultsState()
    object Loading : ResetToDefaultsState()
    object Failure : ResetToDefaultsState()
}