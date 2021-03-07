package com.example.follower.screens.tracking_control

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onCancel
import com.afollestad.materialdialogs.input.input
import com.example.follower.FollowerApp
import com.example.follower.R
import com.example.follower.base.BaseFragment
import com.example.follower.ext.*
import com.example.follower.helper.CustomToast.errorToast
import com.example.follower.helper.CustomToast.infoToast
import com.example.follower.helper.FlightRecorder
import com.example.follower.services.ACTION_START_TRACKING
import com.example.follower.services.ACTION_STOP_TRACKING
import com.example.follower.services.LocationTrackingService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_tracking_control.*
import javax.inject.Inject

private const val PERMISSION_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
private const val CODE_PERMISSION_LOCATION = 101

class TrackingControlFragment : BaseFragment(R.layout.fragment_tracking_control) {
    @Inject lateinit var logger: FlightRecorder
    private val viewModel by viewModels<TrackingControlViewModel> { viewModelFactory }
    private var isServiceBound = false
    private var gpsService: LocationTrackingService? = null

    private val locationPermissionExplanationDialog by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.app_name))
            .setMessage(R.string.location_permission_dialog_explanation)
            .setPositiveButton(getString(android.R.string.ok), null)
            .setNegativeButton(getString(R.string.title_settings)) { dialog, _ ->
                dialog.dismiss()
                requireActivity().openAppSettings()
            }
            .create()
    }

    private val emptyWayPointsDialog by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.app_name))
            .setMessage(R.string.message_you_have_no_waypoints)
            .setPositiveButton(getString(R.string.title_stop_tracking)) { _, _ -> viewModel.clearWaypoints(gpsService!!.traceBeginningTime!!) }
            .setNegativeButton(getString(R.string.title_continue), null)
            .create()
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            if (className.className.endsWith(LocationTrackingService::class.java.simpleName)) {
                logger.i { "ServiceConnection: connected" }
                isServiceBound = true

                gpsService = (service as LocationTrackingService.LocationServiceBinder).getService()
//                subscriptions.clear() /*FIXME WTF?!!!!?!??!???!!?*/
                subscriptions += gpsService!!
                    .isTracking
                    .subscribe { toggleButtons(it) }
            }
        }

        /*calling if Service have been crashed or killed*/
        override fun onServiceDisconnected(name: ComponentName) {
            if (name.className.endsWith(LocationTrackingService::class.java.simpleName)) {
                logger.i { "ServiceConnection: disconnected" }
                isServiceBound = false
                toggleButtons(false)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        requireActivity().bindService(Intent(requireActivity(), LocationTrackingService::class.java), serviceConnection, AppCompatActivity.BIND_AUTO_CREATE)
            .also { logger.i { "service bound ($it) from onStart()" } }
    }

    override fun onStop() {
        super.onStop()
        try {
            requireActivity().unbindService(serviceConnection)
            isServiceBound = false
            gpsService = null
        } catch (e: Throwable) {
            logger.e(label = "Unbinding unsuccessful...", stackTrace = e.stackTrace)
        }
    }

    private fun startTracking() {
        with(requireActivity()) {
            startService(Intent(this, LocationTrackingService::class.java)
                .apply { action = ACTION_START_TRACKING })
        }
    }

    override fun onAttach(context: Context) {
        (context.applicationContext as FollowerApp).appComponent.inject(this)
        super.onAttach(context)
    }

    override fun setupViewModelSubscriptions() {
        viewModel.errorEvent.observe(viewLifecycleOwner, { errorToast(getString(it)) })
        viewModel.saveTrackEvent.observe(viewLifecycleOwner, { infoToast(getString(it)) })

        viewModel.unbindServiceEvent.observe(viewLifecycleOwner, {
            requireActivity().startService(Intent(requireActivity(), LocationTrackingService::class.java).apply { action = ACTION_STOP_TRACKING })
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        @Suppress("DEPRECATION") /* new API with registerForActivityResult(ActivityResultContract, ActivityResultCallback)} instead doesn't work! :( */
        /*Maybe someday... https://developer.android.com/training/permissions/requesting*/
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (isDetached.not() && requestCode == CODE_PERMISSION_LOCATION) {
            handleUsersReactionToPermission(
                permissionToHandle = PERMISSION_LOCATION,
                allPermissions = permissions,
                doIfAllowed = { startTracking() },
                doIfDenied = { locationPermissionExplanationDialog.show() },
                doIfNeverAskAgain = { locationPermissionExplanationDialog.show() }
            )
        }
    }

    override fun setupClicks() {
        subscriptions += btn_start_tracking.clicks()
            .throttleFirst(750L)
            .subscribe {
                if (hasPermission(PERMISSION_LOCATION)) {
                    startTracking()
                } else {
                    @Suppress("DEPRECATION")
                    // new API with registerForActivityResult(ActivityResultContract, ActivityResultCallback)} instead doesn't work! :(
                    // Maybe someday... https://developer.android.com/training/permissions/requesting
                    requestPermissions(arrayOf(PERMISSION_LOCATION), CODE_PERMISSION_LOCATION)
                }
            }

        subscriptions += btn_stop_tracking.clicks()
            .throttleFirst(750L)
            .subscribe {
                if (isServiceBound && gpsService != null) {
                    if (gpsService!!.wayPoints.isEmpty()) {
                        emptyWayPointsDialog.show()
                    } else {
                        MaterialDialog(requireContext()).show {
                            val nonSavedTrackId = gpsService!!.traceBeginningTime!!
                            onCancel {
                                viewModel.clearWaypoints(nonSavedTrackId)
                            }
                            input(prefill = nonSavedTrackId.toReadableDate(), hintRes = R.string.hint_name_your_trace) { _, text ->
                                viewModel.saveTrack(nonSavedTrackId, text.toString(), gpsService!!.wayPoints)
                            }
                        }
                    }
                } else {
                    logger.wtf { "problem with service binding... ${gpsService == null}" }
                    throw RuntimeException()
                }
            }
    }

    /*todo investigate why NPE happens here and why lifecycle fires twice*/
    private fun toggleButtons(isTracking: Boolean) {
        btn_start_tracking?.isEnabled = isTracking.not()
        btn_stop_tracking?.isEnabled = isTracking
        txt_status?.text = getString(if (isTracking) R.string.title_tracking else R.string.title_gps_ready)
    }
}