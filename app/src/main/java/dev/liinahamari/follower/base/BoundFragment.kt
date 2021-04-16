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
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import dev.liinahamari.follower.services.location_tracking.LocationTrackingService

abstract class BoundFragment(@LayoutRes layoutRes: Int): BaseFragment(layoutRes) {
    protected var isServiceBound = false

    abstract fun getBindingTarget(): Class<out Service>
    abstract fun onServiceConnected(binder: IBinder)
    abstract fun onServiceDisconnected()

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            if (className.className.endsWith(getBindingTarget().simpleName)) {
                logger.lifecycle { "ServiceConnection (${this::class.java.simpleName}): connected" }
                isServiceBound = true
                onServiceConnected(service)
            }
        }

        /*calling if Service have been crashed or killed in order to free resources*/
        override fun onServiceDisconnected(name: ComponentName) {
            if (name.className.endsWith(LocationTrackingService::class.java.simpleName)) {
                logger.lifecycle { "ServiceConnection: disconnected" }
                isServiceBound = false
                onServiceDisconnected()
            }
        }
    }

    @CallSuper
    override fun onStart() {
        super.onStart()
        requireActivity().bindService(Intent(requireActivity(), getBindingTarget()), serviceConnection, AppCompatActivity.BIND_AUTO_CREATE)
            .also { logger.lifecycle { "(${this::class.java.simpleName}) Service bound ($it) from onStart()" } }
    }

    @CallSuper
    override fun onStop() {
        super.onStop()
        try {
            requireActivity().unbindService(serviceConnection)
            isServiceBound = false
        } catch (e: Throwable) {
            logger.e(label = "(${this::class.java.simpleName}) Unbinding unsuccessful...", error = e)
        }
    }
}