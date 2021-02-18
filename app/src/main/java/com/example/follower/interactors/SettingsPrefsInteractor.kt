package com.example.follower.interactors

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.example.follower.R
import com.example.follower.ext.writeStringOf
import com.example.follower.helper.rx.BaseComposers
import io.reactivex.Observable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SettingsPrefsInteractor @Inject constructor(private val baseComposers: BaseComposers, private val sharedPreferences: SharedPreferences, private val context: Context) {
    fun resetOptionsToDefaults(): Observable<ResetToDefaultsState> = Single.fromCallable {
        with(sharedPreferences) {
            writeStringOf(context.getString(R.string.pref_lang), context.resources.getStringArray(R.array.supported_locales).first())
            writeStringOf(context.getString(R.string.pref_min_distance), DEFAULT_LOCATION_UPDATE_INTERVAL.toString())
            writeStringOf(context.getString(R.string.pref_min_location_update_interval), DEFAULT_TIME_UPDATE_INTERVAL.toString())
            writeStringOf(context.getString(R.string.pref_theme), AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString())
            writeStringOf(context.getString(R.string.pref_track_representing), context.resources.getStringArray(R.array.track_representing_values).first())
            writeStringOf(context.getString(R.string.pref_track_display_mode), context.resources.getStringArray(R.array.track_display_mode_values).first())
        }
    }.toObservable()
        .delaySubscription(750, TimeUnit.MILLISECONDS)
        .map<ResetToDefaultsState> { ResetToDefaultsState.Success }
        .onErrorReturn { ResetToDefaultsState.Failure }
        .compose(baseComposers.applyObservableSchedulers())
        .startWith(ResetToDefaultsState.Loading)
}

sealed class ResetToDefaultsState {
    object Success : ResetToDefaultsState()
    object Loading : ResetToDefaultsState()
    object Failure : ResetToDefaultsState()
}