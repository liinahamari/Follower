@file:Suppress("StaticFieldLeak" /*cause app context*/)

package dev.liinahamari.follower.screens.tracking_control

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import dev.liinahamari.follower.BuildConfig
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.BaseViewModel
import dev.liinahamari.follower.di.modules.APP_CONTEXT
import dev.liinahamari.follower.ext.getBooleanOf
import dev.liinahamari.follower.helper.SingleLiveEvent
import dev.liinahamari.follower.helper.rx.BaseComposers
import io.reactivex.Single
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject
import javax.inject.Named

private const val LAUNCH_COUNTER_THRESHOLD = 5
private const val DEBUG_LAUNCH_COUNTER_THRESHOLD = 2

class TrackingControlViewModel @Inject constructor(private val baseComposers: BaseComposers, private val sharedPreferences: SharedPreferences, @Named(APP_CONTEXT) private val context: Context) : BaseViewModel() {
    private val _showRateMyAppEvent = SingleLiveEvent<Any>()
    val showRateMyAppEvent: LiveData<Any> get() = _showRateMyAppEvent

    private val launchCounter = if (BuildConfig.DEBUG) DEBUG_LAUNCH_COUNTER_THRESHOLD else LAUNCH_COUNTER_THRESHOLD

    fun checkShowRateMyApp() {
        disposable += Single.fromCallable { sharedPreferences.getBooleanOf(context.getString(R.string.pref_never_show_rate_app)).not() && sharedPreferences.getInt(context.getString(R.string.pref_app_launch_counter), 0) % launchCounter == 0 }
            .compose(baseComposers.applySingleSchedulers())
            .subscribe({
                if (it) {
                    _showRateMyAppEvent.call()
                }
            }, { it.printStackTrace() })
    }
}
