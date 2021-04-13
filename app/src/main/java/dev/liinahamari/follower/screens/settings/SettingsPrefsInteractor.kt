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
class SettingsPrefsInteractor constructor(
    private val baseComposers: BaseComposers,
    private val sharedPreferences: SharedPreferences,
    @Named(APP_CONTEXT) private val context: Context
) {
    fun resetOptionsToDefaults(): Observable<ResetToDefaultsState> = Observable.fromCallable {
        with(sharedPreferences) {
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