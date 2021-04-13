package dev.liinahamari.follower.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.liinahamari.follower.FollowerApp
import dev.liinahamari.follower.model.PreferenceRepository
import dev.liinahamari.follower.services.AutoTrackingSchedulingService
import javax.inject.Inject

//TODO write tests
class BootReceiver : BroadcastReceiver() {
    @Inject lateinit var prefRepo: PreferenceRepository

    override fun onReceive(context: Context, intent: Intent) {
        (context.applicationContext as FollowerApp).appComponent.inject(this)

        if (prefRepo.isAutoTrackingEnabled.blockingSingle() && intent.action == Intent.ACTION_BOOT_COMPLETED) {
            context.startForegroundService(Intent(context.applicationContext, AutoTrackingSchedulingService::class.java))
        }
    }
}