package com.example.follower.screens.tracking_control

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import com.example.follower.*
import com.example.follower.base.BaseFragment
import com.example.follower.services.ACTION_TERMINATE
import com.example.follower.services.LocationTrackingService
import com.example.follower.services.TRACKING_ID
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_tracking_control.*
import javax.inject.Inject

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

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    private val sharedPrefListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
        if (key == TRACKING_ID) {
            toggleButtons(sharedPrefs.getBoolean(TRACKING_ID, false))
        }
    }

    override fun onAttach(context: Context) {
        (context.applicationContext as FollowerApp).appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onDestroy() = super.onDestroy().also { sharedPrefs.unregisterOnSharedPreferenceChangeListener(sharedPrefListener) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toggleButtons(sharedPrefs.getBoolean(TRACKING_ID, false))
        sharedPrefs.registerOnSharedPreferenceChangeListener(sharedPrefListener)

        subscriptions += btn_start_tracking.clicks()
            .throttleFirst()
            .subscribe {
                if (requireContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
                    startTrackingService()
                } else {
                    requestPermissions(arrayOf(GEO_PERMISSION), GEO_PERMISSION_REQUEST_CODE)
                }
            }

        subscriptions += btn_stop_tracking.clicks()
            .throttleFirst()
            .subscribe { startTrackingService(ACTION_TERMINATE) }

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

    private fun startTrackingService(action: String? = null) = requireActivity().startService(Intent(requireActivity(), LocationTrackingService::class.java)
        .apply { action?.let { this.action = action } })

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) = handleUsersReactionToPermission(
        permissionToHandle = Manifest.permission.ACCESS_FINE_LOCATION,
        allPermissions = permissions,
        doIfAllowed = { startTrackingService() },
        doIfDenied = { explanationDialog.show() },
        doIfNeverAskAgain = { openSettings() }
    )
}