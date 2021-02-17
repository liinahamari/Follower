package com.example.follower.screens.tracking_control

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.IBinder
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.example.follower.FollowerApp
import com.example.follower.R
import com.example.follower.base.BaseFragment
import com.example.follower.ext.errorToast
import com.example.follower.ext.throttleFirst
import com.example.follower.ext.toReadableDate
import com.example.follower.ext.toast
import com.example.follower.helper.FlightRecorder
import com.example.follower.services.LocationTrackingService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_tracking_control.*
import javax.inject.Inject

class TrackingControlFragment : BaseFragment(R.layout.fragment_tracking_control) {
    @Inject lateinit var logger: FlightRecorder
    private val viewModel by viewModels<TrackingControlViewModel> { viewModelFactory }
    private var isServiceBound = false
    private var gpsService: LocationTrackingService? = null

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
                gpsService?.let { gpsService ->
                    subscriptions += gpsService
                        .isTracking
                        .subscribe {
                            toggleButtons(it)
                        }
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

    override fun onDetach() {
        super.onDetach()
        gpsService = null
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
            logger.e(label = "Unbinding unsuccessful...", stackTrace = e.stackTrace)
        }
    }

    private fun startTracking() {
        with(requireActivity()) {
            val intent = Intent(this, LocationTrackingService::class.java)
            startService(intent)
            bindService(intent, serviceConnection, AppCompatActivity.BIND_AUTO_CREATE)
                .also { logger.i { "service bound ($it) from startTracking()" } }
        }
    }

    override fun onAttach(context: Context) {
        (context.applicationContext as FollowerApp).appComponent.inject(this)
        super.onAttach(context)
    }

    override fun setupViewModelSubscriptions() {
        viewModel.errorEvent.observe(viewLifecycleOwner, { errorToast(it) })
        viewModel.saveTrackEvent.observe(viewLifecycleOwner, { toast(it) })
    }

    override fun setupClicks() {
        subscriptions += btn_start_tracking.clicks()
            .throttleFirst(750L)
            .subscribe {
                if (requireContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
                    startTracking()
                } else {
                    registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                        if (it) {
                            startTracking()
                        }
                    }.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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
                            input(prefill = gpsService!!.traceBeginningTime!!.toReadableDate(), hintRes = R.string.hint_name_your_trace) { _, text ->
                                viewModel.saveTrack(gpsService!!.traceBeginningTime!!, text.toString(), gpsService!!.wayPoints)
                            }
                        }
                        requireActivity().unbindService(serviceConnection)
                        isServiceBound = false

                        requireActivity().stopService(Intent(requireActivity(), LocationTrackingService::class.java))
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