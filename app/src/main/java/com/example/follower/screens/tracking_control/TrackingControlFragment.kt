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
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.follower.*
import com.example.follower.base.BaseFragment
import com.example.follower.services.LocationTrackingService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_tracking_control.*

private const val GEO_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
private const val GEO_PERMISSION_REQUEST_CODE = 12

class TrackingControlFragment : BaseFragment(R.layout.fragment_tracking_control) {
    private val explanationDialog by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.app_name))
            .setMessage(R.string.location_permission_dialog_explanation)
            .setPositiveButton(getString(android.R.string.ok), null)
            .create()
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            if (className.className.endsWith(LocationTrackingService::class.java.simpleName)) {
                Log.d("a", "zzz connected")

                subscriptions += (service as LocationTrackingService.LocationServiceBinder)
                    .service
                    .isTracking
                    .subscribe {
                        toggleButtons(it)
                    }
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            if (name.className.endsWith(LocationTrackingService::class.java.simpleName)) {
                Log.d("a", "zzz disconnected")
                toggleButtons(false)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        requireActivity().bindService(Intent(requireActivity(), LocationTrackingService::class.java), serviceConnection, AppCompatActivity.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        /*TODO `bound` flag*/
        requireActivity().unbindService(serviceConnection)
    }

    private fun startTracking() {
        with(requireActivity()) {
            val intent = Intent(this, LocationTrackingService::class.java)
            startService(intent)
            bindService(intent, serviceConnection, AppCompatActivity.BIND_AUTO_CREATE)
        }
    }

    override fun onAttach(context: Context) {
        (context.applicationContext as FollowerApp).appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        subscriptions += btn_start_tracking.clicks()
            .throttleFirst(1000L)
            .subscribe {
                if (requireContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
                    startTracking()
                } else {
                    requestPermissions(arrayOf(GEO_PERMISSION), GEO_PERMISSION_REQUEST_CODE)
                }
            }

        subscriptions += btn_stop_tracking.clicks()
            .throttleFirst(1000L)
            .subscribe {
                requireActivity().unbindService(serviceConnection)
                requireActivity().stopService(Intent(requireActivity(), LocationTrackingService::class.java))
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
        doIfDenied = { explanationDialog.show() },
        doIfNeverAskAgain = { openSettings() }
    )
}