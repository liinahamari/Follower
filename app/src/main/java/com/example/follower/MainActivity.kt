package com.example.follower

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_main.*

private const val GEO_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
private const val GEO_PERMISSION_REQUEST_CODE = 12

class MainActivity : AppCompatActivity() {
    private var gpsService: BackgroundTracker? = null
    private var isTracking = false

    override fun onCreateOptionsMenu(menu: Menu?): Boolean = true.also { menuInflater.inflate(R.menu.menu, menu) }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.showTrace -> startActivity(Intent(this, ShowTraceActivity::class.java))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        with(Intent(application, BackgroundTracker::class.java)) {
            application.startService(this)
            //        this.getApplication().startForegroundService(this);
            application.bindService(this, serviceConnection, BIND_AUTO_CREATE)
        }

        btn_start_tracking.setOnClickListener {
            if (hasAllPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))) {
                gpsService!!.startTracking()
                isTracking = true
                toggleButtons()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(GEO_PERMISSION),
                    GEO_PERMISSION_REQUEST_CODE
                )
            }
        }

        btn_stop_tracking.setOnClickListener {
            isTracking = false
            gpsService?.stopTracking()
            toggleButtons()
        }
    }

    private fun toggleButtons() {
        btn_start_tracking.isEnabled = isTracking.not()
        btn_stop_tracking.isEnabled = isTracking
        txt_status.text = getString(if (isTracking) R.string.title_tracking else R.string.title_gps_ready)
    }

    private fun openSettings() {
        startActivity(Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
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