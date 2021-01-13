package com.example.follower

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import com.example.follower.base.BaseActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_main.*

private const val GEO_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
private const val GEO_PERMISSION_REQUEST_CODE = 12

class MainActivity : BaseActivity() {
    /*    private val serviceConnection: ServiceConnection = object : ServiceConnection {
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
    */

    private val sharedPrefListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
        if (key == TRACKING_ID) {
            toggleButtons(sharedPrefs.getBoolean(TRACKING_ID, false))
        }
    }

    override fun onDestroy() = super.onDestroy().also { PreferenceManager.getDefaultSharedPreferences(applicationContext).unregisterOnSharedPreferenceChangeListener(sharedPrefListener) }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean = true.also { menuInflater.inflate(R.menu.menu, menu) }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.showTrace -> startActivity(Intent(this, ShowTraceActivity::class.java))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toggleButtons(PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean(TRACKING_ID, false))
        PreferenceManager.getDefaultSharedPreferences(applicationContext).registerOnSharedPreferenceChangeListener(sharedPrefListener)

        btn_start_tracking.setOnClickListener {
            if (hasAllPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))) {
                startTrackingService()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(GEO_PERMISSION), GEO_PERMISSION_REQUEST_CODE)
            }
        }

        btn_stop_tracking.setOnClickListener { startTrackingService(ACTION_TERMINATE) }
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

    private fun startTrackingService(action: String? = null) = startService(Intent(this, BackgroundTracker::class.java)
        .apply { action?.let { this.action = action } })

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
            doIfAllowed = { startTrackingService() },
            doIfDenied = { explanationDialog.show() },
            doIfNeverAskAgain = { openSettings() }
        )
    }
}