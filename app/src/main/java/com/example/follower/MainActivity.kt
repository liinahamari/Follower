/*
@file:SuppressLint("SetTextI18n")

package com.example.follower

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.follower.BackgroundTracker.LocationServiceBinder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_main.*

private const val GEO_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
private const val GEO_PERMISSION_REQUEST_CODE = 12

class MainActivity : AppCompatActivity() {
    private var gpsService: BackgroundTracker? = null
    private var isTracking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        with(Intent(this.application, BackgroundTracker::class.java)) {
            application.startService(this)
            //        this.getApplication().startForegroundService(intent);
            application.bindService(this, serviceConnection, BIND_AUTO_CREATE)
        }
        btn_start_tracking.setOnClickListener {
            if (hasAllPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))) {
                gpsService!!.startTracking()
                isTracking = true
                toggleButtons()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(GEO_PERMISSION), GEO_PERMISSION_REQUEST_CODE)
            }
        }
        btn_stop_tracking.setOnClickListener {
            isTracking = false
            gpsService!!.stopTracking()
            toggleButtons()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val explanationDialog = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.app_name))
            .setMessage(R.string.location_permission_dialog_explanation)
            .setPositiveButton(getString(android.R.string.ok), null)
            .create()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            handleUsersReactionToPermission(
                permissionToHandle = Manifest.permission.ACCESS_FINE_LOCATION,
                allPermissions = permissions,
                doIfAllowed = {
                    gpsService!!.startTracking()
                    isTracking = true
                    toggleButtons()
                },
                doIfDenied = { explanationDialog.show() },
                doIfNeverAskAgain = { openSettings() }
            )
        }
    }

    private fun toggleButtons() {
        btn_start_tracking.isEnabled = isTracking.not()
        btn_stop_tracking.isEnabled = isTracking
        txt_status.text = if (isTracking) "TRACKING" else "GPS Ready"
    }

    private fun openSettings() {
        with(Intent()) {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(this)
        }
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            if (className.className.endsWith("BackgroundTracker")) {
                gpsService = (service as LocationServiceBinder).service
                btn_start_tracking.isEnabled = true
                txt_status.text = "GPS Ready"
            }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            if (className.className == "BackgroundTracker") {
                gpsService = null
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.M)
fun Activity.handleUsersReactionToPermission(
    permissionToHandle: String,
    allPermissions: Array<out String>,
    doIfDenied: () -> Unit,
    doIfAllowed: () -> Unit,
    doIfNeverAskAgain: () -> Unit
) {
    if (allPermissions.contains(permissionToHandle)) {
        if (shouldShowRequestPermissionRationale(permissionToHandle)) {
            doIfDenied()
        } else {
            if (hasPermission(permissionToHandle)) { //allowed
                doIfAllowed()
            } else { //set to never ask again
                doIfNeverAskAgain()
            }
        }
    }
}

fun Activity.hasPermission(permission: String) =
    ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

fun Context.hasAllPermissions(permissions: Array<String>): Boolean {
    return permissions.all {
        ActivityCompat.checkSelfPermission(
            this,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }
}
*/

package com.example.follower

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var gpsService: BackgroundTracker? = null
    private var tracking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        with(Intent(application, BackgroundTracker::class.java)) {
            application.startService(this)
            //        this.getApplication().startForegroundService(this);
            application.bindService(this, serviceConnection, BIND_AUTO_CREATE)
        }
        btn_start_tracking.setOnClickListener {
            Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(object : PermissionListener {

                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        gpsService?.startTracking()
                        tracking = true
                        toggleButtons()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        if (response.isPermanentlyDenied) {
                            openSettings()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: com.karumi.dexter.listener.PermissionRequest?,
                        token: PermissionToken?
                    ) {
                        token?.continuePermissionRequest()
                    }
                }).check()
        }
        btn_stop_tracking.setOnClickListener {
            tracking = false
            gpsService?.stopTracking()
            toggleButtons()
        }
    }

    private fun toggleButtons() {
        btn_start_tracking.isEnabled = tracking.not()
        btn_stop_tracking.isEnabled = tracking
        txt_status.text = getString(if (tracking) R.string.title_tracking else R.string.title_gps_ready)
    }

    private fun openSettings() {
        startActivity(Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            if (className.className.endsWith(BackgroundTracker::class.java.simpleName)) {
                gpsService = (service as BackgroundTracker.LocationServiceBinder).service
                btn_start_tracking.isEnabled = true
                txt_status.text = getString(R.string.title_gps_ready)
            }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            if (className.className == BackgroundTracker::class.java.simpleName) {
                gpsService = null
            }
        }
    }
}