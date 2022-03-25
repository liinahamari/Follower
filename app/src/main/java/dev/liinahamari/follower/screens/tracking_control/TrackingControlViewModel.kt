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
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.plusAssign
import javax.inject.Inject
import javax.inject.Named

private const val LAUNCH_COUNTER_THRESHOLD = 5
private const val DEBUG_LAUNCH_COUNTER_THRESHOLD = 2

class TrackingControlViewModel @Inject constructor(private val baseComposers: BaseComposers, private val sharedPreferences: SharedPreferences, @Named(APP_CONTEXT) private val context: Context) : BaseViewModel() {
    private val _showRateMyAppEvent = SingleLiveEvent<Any>()
    val showRateMyAppEvent: LiveData<Any> get() = _showRateMyAppEvent

    private val launchCounter = if (BuildConfig.DEBUG) DEBUG_LAUNCH_COUNTER_THRESHOLD else LAUNCH_COUNTER_THRESHOLD

    fun checkShowRateMyApp() {
        disposable += Single.fromCallable { sharedPreferences.getBooleanOf(context.getString(R.string.pref_never_show_rate_app)).not() && sharedPreferences.getInt(context.getString(R.string.pref_is_app_first_launched), 0) % launchCounter == 0 }
            .compose(baseComposers.applySingleSchedulers())
            .subscribe({
                if (it) {
                    _showRateMyAppEvent.call()
                }
            }, { it.printStackTrace() })
    }
}
