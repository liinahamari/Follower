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

package dev.liinahamari.follower.base

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.annotation.CallSuper
import dev.liinahamari.follower.ext.appComponent
import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import io.reactivex.rxjava3.disposables.CompositeDisposable

const val FOREGROUND_ID_AUTO_TRACKING_SCHEDULING = 111
const val FOREGROUND_ID_LOCATION_TRACKING = 112

abstract class BaseService: Service() {
    protected val subscriptions = CompositeDisposable()

    @CallSuper
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        FlightRecorder.lifecycle { "${this.javaClass.simpleName} onStartCommand()" }
        return super.onStartCommand(intent, flags, startId)
    }

    @CallSuper
    override fun onCreate() {
        appComponent.inject(this)
        FlightRecorder.lifecycle { "${this.javaClass.simpleName} creating..." }
    }

    @CallSuper
    override fun onDestroy() {
        FlightRecorder.lifecycle { "${this.javaClass.simpleName} destroyed" }
        subscriptions.clear()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
