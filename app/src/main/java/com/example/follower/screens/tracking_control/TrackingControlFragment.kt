package com.example.follower.screens.tracking_control

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.example.follower.*
import com.example.follower.base.BaseFragment
import com.example.follower.ext.*
import com.example.follower.helper.FlightRecorder
import com.example.follower.services.LocationTrackingService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_tracking_control.*
import javax.inject.Inject

private const val GEO_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
private const val GEO_PERMISSION_REQUEST_CODE = 12

class TrackingControlFragment : BaseFragment(R.layout.fragment_tracking_control) {
    @Inject lateinit var logger: FlightRecorder
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<TrackingControlViewModel> { viewModelFactory }
    private var isServiceBound = false
    private lateinit var gpsService: LocationTrackingService

    private val permissionExplanationDialog by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.app_name))
            .setMessage(R.string.location_permission_dialog_explanation)
            .setPositiveButton(getString(android.R.string.ok), null)
            .create()
    }

    private val emptyWayPointsDialog by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.app_name))
            .setMessage(R.string.message_you_have_no_waypoints)
            .setPositiveButton(getString(R.string.title_stop_tracking)) { _, _ ->
                if (isServiceBound) {
                    requireActivity().unbindService(serviceConnection)
                    isServiceBound = false
                }
                requireActivity().stopService(Intent(requireActivity(), LocationTrackingService::class.java))
            }
            .setNegativeButton(getString(R.string.title_continue), null)
            .create()
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            if (className.className.endsWith(LocationTrackingService::class.java.simpleName)) {
                logger.i { "ServiceConnection: connected" }
                isServiceBound = true

                gpsService = (service as LocationTrackingService.LocationServiceBinder).service
//                subscriptions.clear() /*TODO WTF?*/
                subscriptions += gpsService
                    .isTracking
                    .subscribe {
                        toggleButtons(it)
                    }
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
        } catch (e: Throwable) {
            logger.wtf { "Unbinding unsuccessful!" }
            logger.e(stackTrace = e.stackTrace)
        }
    }

    private fun startTracking() {
        with(requireActivity()) {
            val intent = Intent(this, LocationTrackingService::class.java)
            startService(intent)
            bindService(intent, serviceConnection, AppCompatActivity.BIND_AUTO_CREATE).also { logger.i { "service bound ($it) from startTracking()" } }
        }
    }

    override fun onAttach(context: Context) {
        (context.applicationContext as FollowerApp).appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupClicks()
        setupViewModelSubscriptions()
    }

    private fun setupViewModelSubscriptions() {
        viewModel.errorEvent.observe(viewLifecycleOwner, { errorToast(it) })
        viewModel.saveTrackEvent.observe(viewLifecycleOwner, { toast(it) })
    }

    private fun setupClicks() {
        subscriptions += btn_start_tracking.clicks()
            .throttleFirst(750L)
            .subscribe {
                if (requireContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
                    startTracking()
                } else {
                    requestPermissions(arrayOf(GEO_PERMISSION), GEO_PERMISSION_REQUEST_CODE)
                }
            }

        subscriptions += btn_stop_tracking.clicks()
            .throttleFirst(750L)
            .subscribe {
                if (isServiceBound) {
                    if (gpsService.wayPoints.isEmpty()) {
                        emptyWayPointsDialog.show()
                    } else {
                        MaterialDialog(requireContext()).show {
                            input(prefill = gpsService.traceBeginningTime!!.toReadableDate(), hintRes = R.string.hint_name_your_trace) { _, text ->
                                viewModel.saveTrack(gpsService.traceBeginningTime!!, text.toString(), gpsService.wayPoints)
                            }
                        }
                        requireActivity().unbindService(serviceConnection)
                        isServiceBound = false

                        requireActivity().stopService(Intent(requireActivity(), LocationTrackingService::class.java))
                    }
                } else {
                    logger.wtf { "problem with service binding..." }
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

    private fun openSettings() {
        startActivity(Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) = handleUsersReactionToPermission(
        permissionToHandle = Manifest.permission.ACCESS_FINE_LOCATION,
        allPermissions = permissions,
        doIfAllowed = { startTracking() },
        doIfDenied = { permissionExplanationDialog.show() },
        doIfNeverAskAgain = { openSettings() }
    )
}