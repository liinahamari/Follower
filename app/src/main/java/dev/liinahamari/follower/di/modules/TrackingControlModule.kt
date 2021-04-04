package dev.liinahamari.follower.di.modules

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import dev.liinahamari.follower.R
import dev.liinahamari.follower.ext.openAppSettings
import dev.liinahamari.follower.screens.tracking_control.RateMyAppDialog
import dev.liinahamari.follower.screens.tracking_control.TrackingControlScope
import dev.liinahamari.follower.services.location_tracking.ACTION_DISCARD_TRACK
import dev.liinahamari.follower.services.location_tracking.LocationTrackingService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.Module
import dagger.Provides
import javax.inject.Named

const val DIALOG_RATE_MY_APP = "tracking_controls_rate_my_app"
const val DIALOG_EMPTY_WAYPOINTS = "tracking_controls_empty_waypoints"
const val DIALOG_PERMISSION_EXPLANATION = "tracking_controls_permission_explanation"

@Module
class TrackingControlModule(private val activity: FragmentActivity) {
    @TrackingControlScope
    @Named(DIALOG_PERMISSION_EXPLANATION)
    @Provides
    fun providePermissionExplanationDialog(@Named(APP_CONTEXT) context: Context): AlertDialog = MaterialAlertDialogBuilder(activity)
        .setTitle(context.getString(R.string.app_name))
        .setMessage(R.string.location_permission_dialog_explanation)
        .setPositiveButton(context.getString(android.R.string.ok), null)
        .setNegativeButton(context.getString(R.string.title_settings)) { dialog, _ ->
            dialog.dismiss()
            activity.openAppSettings()
        }
        .create()

    @TrackingControlScope
    @Named(DIALOG_EMPTY_WAYPOINTS)
    @Provides
    fun provideEmptyWayPointsDialog(@Named(APP_CONTEXT) context: Context): AlertDialog = MaterialAlertDialogBuilder(activity)
        .setTitle(context.getString(R.string.app_name))
        .setMessage(R.string.message_you_have_no_waypoints)
        .setPositiveButton(context.getString(R.string.title_stop_tracking)) { _, _ ->
            context.startService(Intent(context, LocationTrackingService::class.java)
                .apply { action = ACTION_DISCARD_TRACK })
        }
        .setNegativeButton(context.getString(R.string.title_continue), null)
        .create()

    @TrackingControlScope
    @Named(DIALOG_RATE_MY_APP)
    @Provides
    fun provideRateMyAppDialog(sharedPreferences: SharedPreferences): DialogFragment = RateMyAppDialog(sharedPreferences)
}