package dev.liinahamari.follower.di.modules

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import dev.liinahamari.follower.ext.openAppSettings
import dev.liinahamari.follower.helper.FlightRecorder
import dev.liinahamari.follower.helper.rx.BaseComposers
import dev.liinahamari.follower.model.WayPointDao
import dev.liinahamari.follower.screens.tracking_control.ClearWayPointsInteractor
import dev.liinahamari.follower.screens.tracking_control.TrackingControlScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.Module
import dagger.Provides
import dev.liinahamari.follower.R
import javax.inject.Named

const val DIALOG_EMPTY_WAYPOINTS = "tracking_controls_empty_waypoints"
const val DIALOG_PERMISSION_EXPLANATION = "tracking_controls_permission_explanation"

@Module
class TrackingControlModule(private val activity: FragmentActivity, private val onStopTrackingClick: () -> Unit) {
    @TrackingControlScope
    @Named(DIALOG_PERMISSION_EXPLANATION)
    @Provides
    fun providePermissionExplanationDialog(context: Context): AlertDialog = MaterialAlertDialogBuilder(activity)
        .setTitle(context.getString(R.string.app_name))
        .setMessage(R.string.location_permission_dialog_explanation)
        .setPositiveButton(context.getString(android.R.string.ok), null)
        .setNegativeButton(context.getString(R.string.title_settings)) { dialog, _ ->
            dialog.dismiss()
            activity.openAppSettings()
        }
        .create()

    @Provides
    @TrackingControlScope
    fun provideClearWayPointsInteractor(wayPointDao: WayPointDao, logger: FlightRecorder, baseComposers: BaseComposers) = ClearWayPointsInteractor(wayPointDao, logger, baseComposers)

    @TrackingControlScope
    @Named(DIALOG_EMPTY_WAYPOINTS)
    @Provides
    fun provideEmptyWayPointsDialog(context: Context): AlertDialog = MaterialAlertDialogBuilder(activity)
        .setTitle(context.getString(R.string.app_name))
        .setMessage(R.string.message_you_have_no_waypoints)
        .setPositiveButton(context.getString(R.string.title_stop_tracking)) { _, _ -> onStopTrackingClick.invoke() }
        .setNegativeButton(context.getString(R.string.title_continue), null)
        .create()
}