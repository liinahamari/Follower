package com.example.follower.screens.tracking_control

import android.Manifest
import android.app.Service
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.example.follower.FollowerApp
import com.example.follower.R
import com.example.follower.base.BoundFragment
import com.example.follower.di.modules.DIALOG_EMPTY_WAYPOINTS
import com.example.follower.di.modules.DIALOG_PERMISSION_EXPLANATION
import com.example.follower.di.modules.DIALOG_RATE_MY_APP
import com.example.follower.di.modules.TrackingControlModule
import com.example.follower.ext.*
import com.example.follower.services.location_tracking.*
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_tracking_control.*
import javax.inject.Inject
import javax.inject.Named

const val PERMISSION_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
@RequiresApi(Build.VERSION_CODES.Q) const val PERMISSION_BACKGROUND_LOCATION = Manifest.permission.ACCESS_BACKGROUND_LOCATION
const val CODE_PERMISSION_LOCATION = 101

/*todo, add distance, points*/

@TrackingControlScope
class TrackingControlFragment : BoundFragment(R.layout.fragment_tracking_control) {
    private val viewModel by viewModels<TrackingControlViewModel> { viewModelFactory }
    private val boundServiceDisposables = CompositeDisposable()

    @Inject
    @Named(DIALOG_PERMISSION_EXPLANATION)
    lateinit var locationPermissionExplanationDialog: AlertDialog

    @Inject
    @Named(DIALOG_EMPTY_WAYPOINTS)
    lateinit var emptyWayPointsDialog: AlertDialog

    @Inject
    @Named(DIALOG_RATE_MY_APP)
    lateinit var rateMyAppDialog: DialogFragment

    private var gpsService: LocationTrackingService? = null

    override fun getBindingTarget(): Class<out Service> = LocationTrackingService::class.java

    override fun onServiceDisconnected() {
        gpsService = null
        toggleButtons(false)
    }

    override fun onDetach() {
        super.onDetach()
        gpsService = null
        boundServiceDisposables.clear()
    }

    override fun onServiceConnected(binder: IBinder) {
        boundServiceDisposables.clear()
        gpsService = (binder as LocationTrackingService.LocationServiceBinder).getService()

        boundServiceDisposables += gpsService!!
            .isTracking
            .subscribe { toggleButtons(it) }

        boundServiceDisposables += gpsService!!
            .wayPointsCounter
            .subscribe { way_points_counter.text = String.format(getString(R.string.title_way_points_collected), it) }
    }

    override fun onAttach(context: Context) {
        (context.applicationContext as FollowerApp)
            .appComponent
            .trackingControlComponent(TrackingControlModule(activity = requireActivity()))
            .inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.checkShowRateMyApp()
    }

    override fun setupViewModelSubscriptions() {
        viewModel.showRateMyAppEvent.observe(this, { rateMyAppDialog.show(childFragmentManager, RateMyAppDialog::class.java.simpleName) })
    }

        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        @Suppress("DEPRECATION") /* new API with registerForActivityResult(ActivityResultContract, ActivityResultCallback)} instead doesn't work! :( */
        /*Maybe someday... https://developer.android.com/training/permissions/requesting*/
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (isDetached.not() && requestCode == CODE_PERMISSION_LOCATION) {
            val permissionsToHandle = mutableListOf(PERMISSION_LOCATION)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                /* https://stackoverflow.com/questions/58816066/android-10-q-access-background-location-permission */
                permissionsToHandle.add(PERMISSION_BACKGROUND_LOCATION)
            }

            handleUsersReactionToPermissions(
                permissionsToHandle = permissionsToHandle,
                allPermissions = permissions.toList(),
                doIfAllowed = { startService(LocationTrackingService::class.java, action = ACTION_START_TRACKING) },
                doIfDenied = { locationPermissionExplanationDialog.show() },
                doIfNeverAskAgain = { locationPermissionExplanationDialog.show() }
            )
        }
    }

    override fun setupClicks() {
        subscriptions += btn_start_tracking.clicks()
            .throttleFirst(750L)
            .subscribe {
                val permissions = mutableListOf(PERMISSION_LOCATION)
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    permissions.add(PERMISSION_BACKGROUND_LOCATION)
                }
                if (hasAllPermissions(permissions)) {
                    startService(LocationTrackingService::class.java, action = ACTION_START_TRACKING)
                } else {
                    @Suppress("DEPRECATION")
                    // new API with registerForActivityResult(ActivityResultContract, ActivityResultCallback)} instead doesn't work! :(
                    // Maybe someday... https://developer.android.com/training/permissions/requesting
                    requestPermissions(permissions.toTypedArray(), CODE_PERMISSION_LOCATION)
                }
            }

        subscriptions += btn_stop_tracking.clicks()
            .throttleFirst(750L)
            .subscribe {
                if (isServiceBound && gpsService != null) {
                    if (gpsService!!.isTrackEmpty) {
                        emptyWayPointsDialog.show()
                    } else {
                        MaterialDialog(requireContext()).show {
                            cancelable(false)
                            negativeButton(res = R.string.discard) {
                                startService(LocationTrackingService::class.java, action = ACTION_DISCARD_TRACK)
                            }

                            input(prefill = gpsService!!.traceBeginningTime!!.toReadableDate(), hintRes = R.string.hint_name_your_trace) { _, text ->
                                startService(LocationTrackingService::class.java,
                                    action = ACTION_RENAME_TRACK_AND_STOP_TRACKING,
                                    bundle = Bundle().apply {
                                        putCharSequence(ARG_AUTO_SAVE, text)
                                    })
                            }
                        }
                    }
                } else {
                    logger.wtf { "problem with service binding... gpsService == null (${gpsService == null})" }
                    throw RuntimeException()
                }
            }
    }

    /*todo investigate why NPE happens here and why lifecycle fires twice*/
    private fun toggleButtons(isTracking: Boolean) {
        btn_start_tracking?.isEnabled = isTracking.not()
        btn_stop_tracking?.isEnabled = isTracking
        txt_status?.text = getString(if (isTracking) R.string.title_tracking else R.string.title_gps_ready)
        way_points_counter.isVisible = isTracking
    }
}