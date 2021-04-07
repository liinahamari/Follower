package dev.liinahamari.follower.base

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.annotation.CallSuper
import dev.liinahamari.follower.FollowerApp
import dev.liinahamari.follower.helper.FlightRecorder
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

const val FOREGROUND_ID_AUTO_TRACKING_SCHEDULING = 111
const val FOREGROUND_ID_LOCATION_TRACKING = 112

abstract class BaseService: Service() {
    @Inject lateinit var logger: FlightRecorder
    protected val subscriptions = CompositeDisposable()

    @CallSuper
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        logger.lifecycle { "${this.javaClass.simpleName} onStartCommand()" }
        return super.onStartCommand(intent, flags, startId)
    }

    @CallSuper
    override fun onCreate() {
        (applicationContext as FollowerApp).appComponent.inject(this)
        logger.lifecycle { "${this.javaClass.simpleName} creating..." }
    }

    @CallSuper
    override fun onDestroy() {
        logger.lifecycle { "${this.javaClass.simpleName} destroyed" }
        subscriptions.clear()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
