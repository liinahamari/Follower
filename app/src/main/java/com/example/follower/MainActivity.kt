package com.example.follower

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import com.example.follower.base.BaseActivity
import com.example.follower.screens.show_trace.ShowTraceActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

private const val GEO_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
private const val GEO_PERMISSION_REQUEST_CODE = 12

class MainActivity : BaseActivity(R.layout.activity_main) {
    private var gpsService: LocationTrackingService? = null

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            if (className.className.endsWith(LocationTrackingService::class.java.simpleName)) {
                Log.d("a", "zzz connected")
                gpsService = (service as LocationTrackingService.LocationServiceBinder).service
                subscriptions += gpsService!!.isTracking./*todo debounce?*/subscribe {
                    toggleButtons(it)
                }
            }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            if (className.className == LocationTrackingService::class.java.simpleName) {
                Log.d("a", "zzz disconnected")
                gpsService = null
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean = true.also { menuInflater.inflate(R.menu.menu, menu) }
    override fun onStart() = super.onStart().also { bindService(Intent(this, LocationTrackingService::class.java), serviceConnection, BIND_AUTO_CREATE) }

    override fun onStop() = super.onStop().also {
        gpsService?.let {
            unbindService(serviceConnection)
            gpsService = null
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.showTrace -> startActivity(Intent(this, ShowTraceActivity::class.java))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupClicks()
    }

    private fun setupClicks() {
        subscriptions += btn_start_tracking.clicks()
            .throttleFirst()
            .subscribe {
                if (hasAllPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))) {
                    with(Intent(this, LocationTrackingService::class.java)) {
                        startService(this)
                        Thread.sleep(1000)
                        bindService(this, serviceConnection, BIND_AUTO_CREATE)
                    }
                    gpsService?.startTracking()
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(GEO_PERMISSION), GEO_PERMISSION_REQUEST_CODE)
                }
            }

        subscriptions += btn_stop_tracking.clicks()
            .throttleFirst()
            .subscribe { startService(Intent(this, LocationTrackingService::class.java)
                .apply { action?.let { action = ACTION_TERMINATE } })
            }
    }

    private fun toggleButtons(isTracking: Boolean) {
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
            doIfAllowed = { gpsService?.startTracking() },
            doIfDenied = { explanationDialog.show() },
            doIfNeverAskAgain = { openSettings() }
        )
    }
}