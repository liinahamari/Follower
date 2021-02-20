package com.example.follower.screens.settings

import android.app.AlarmManager
import android.app.Dialog
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.work.WorkManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.example.follower.FollowerApp
import com.example.follower.R
import com.example.follower.ext.*
import com.example.follower.helper.FlightRecorder
import com.example.follower.screens.tracking_control.TrackingControlViewModel
import com.example.follower.services.LocationTrackingService
import com.example.follower.workers.TAG_AUTO_START_WORKER
import com.example.follower.workers.TAG_AUTO_STOP_WORKER
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    @Inject lateinit var logger: FlightRecorder
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var alarmManager: AlarmManager
    private var gpsService: LocationTrackingService? = null
    private var isServiceBound = false

    private val trackingControlViewModel by activityViewModels<TrackingControlViewModel> { viewModelFactory }
    private val settingsViewModel by viewModels<SettingsViewModel> { viewModelFactory }

    private val loadingDialog by lazy {
        Dialog(requireContext(), R.style.DialogNoPaddingNoTitle).apply {
            setContentView(R.layout.dialog_saving)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }
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
            }
        }

        /*calling if Service have been crashed or killed*/
        override fun onServiceDisconnected(name: ComponentName) {
            if (name.className.endsWith(LocationTrackingService::class.java.simpleName)) {
                logger.i { "ServiceConnection: disconnected" }
                isServiceBound = false
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        gpsService = null
    }

    override fun onStart() {
        super.onStart()
        requireActivity().bindService(Intent(requireActivity(), LocationTrackingService::class.java), serviceConnection, AppCompatActivity.BIND_AUTO_CREATE) /*todo TO ACTIVITY?*/
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModelSubscriptions()
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        if (preference is TimePickerPreference) {
            TimePickerPreferenceDialog.newInstance(preference.key)
                .also { @Suppress("DEPRECATION") it.setTargetFragment(this, 0) }
                .show(parentFragmentManager, TimePickerPreferenceDialog::class.java.simpleName)
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    private fun setupViewModelSubscriptions() {
        settingsViewModel.loadingEvent.observe(viewLifecycleOwner, {
            when {
                loadingDialog.isShowing.not() && it -> loadingDialog.show()
                loadingDialog.isShowing && it.not() -> loadingDialog.dismiss()
            }
        })
        settingsViewModel.errorEvent.observe(viewLifecycleOwner, { errorToast(getString(it)) })
        settingsViewModel.successfulSchedulingEvent.observe(viewLifecycleOwner, { toast(getString(it)) })
        settingsViewModel.resetToDefaultsEvent.observe(viewLifecycleOwner, { requireActivity().recreate() })
    }

    override fun onAttach(context: Context) = super.onAttach(context).also { (requireContext().applicationContext as FollowerApp).appComponent.inject(this) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = super.onCreateView(inflater, container, savedInstanceState)
        .also { PreferenceManager.getDefaultSharedPreferences(requireContext()).registerOnSharedPreferenceChangeListener(this) }

    override fun onDestroyView() = super.onDestroyView().also { PreferenceManager.getDefaultSharedPreferences(requireContext()).unregisterOnSharedPreferenceChangeListener(this) }
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) = setPreferencesFromResource(R.xml.preferences, rootKey)

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        if (preference?.key == getString(R.string.pref_reset_to_default) && isDetached.not()) {
            MaterialDialog(requireActivity()).show {
                title(R.string.title_reset_to_defaults)
                negativeButton {}
                positiveButton(R.string.title_continue) { settingsViewModel.resetOptionsToDefaults() }
            }
        }
        return super.onPreferenceTreeClick(preference)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            requireContext().getString(R.string.pref_theme) -> {
                AppCompatDelegate.setDefaultNightMode(sharedPreferences.getStringOf(requireContext().getString(R.string.pref_theme))!!.toInt())
                requireActivity().recreate()
            }
            requireContext().getString(R.string.pref_lang) -> {
                with(Locale(sharedPreferences.getStringOf(key)!!)) {
                    Locale.setDefault(this)
                    @Suppress("DEPRECATION") requireActivity().resources.updateConfiguration(resources.configuration.also { it.setLocale(this) }, resources.displayMetrics)
                }
                requireActivity().recreate()
            }
            getString(R.string.pref_enable_auto_tracking) -> {
                if (sharedPreferences.getBooleanOf(key)) {
                    settingsViewModel.scheduleAutoTracking()
                } else {
                    if (isServiceBound && gpsService != null && gpsService!!.isTracking.value == true) {
                        if (gpsService!!.wayPoints.isEmpty()) {
                            emptyWayPointsDialog.show()
                        } else {
                            MaterialDialog(requireContext()).show {
                                input(prefill = gpsService!!.traceBeginningTime!!.toReadableDate(), hintRes = R.string.hint_name_your_trace) { _, text -> /*todo spinner progress*/
                                    trackingControlViewModel.saveTrack(gpsService!!.traceBeginningTime!!, text.toString(), gpsService!!.wayPoints)

                                    WorkManager.getInstance(requireContext().applicationContext).cancelUniqueWork(TAG_AUTO_START_WORKER)
                                    WorkManager.getInstance(requireContext().applicationContext).cancelUniqueWork(TAG_AUTO_STOP_WORKER)
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

            getString(R.string.pref_tracking_start_time), getString(R.string.pref_tracking_stop_time) -> {
                settingsViewModel.scheduleAutoTracking()
            }
        }
    }
}