@file:Suppress("StaticFieldLeak" /*cause app context*/)

package com.example.follower.screens.tracking_control

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.example.follower.BuildConfig
import com.example.follower.R
import com.example.follower.base.BaseViewModel
import com.example.follower.di.modules.APP_CONTEXT
import com.example.follower.ext.getBooleanOf
import com.example.follower.helper.SingleLiveEvent
import com.example.follower.helper.rx.BaseComposers
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
