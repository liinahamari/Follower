package com.example.follower.screens.settings

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.follower.FollowerApp
import com.example.follower.R
import com.example.follower.di.modules.BiometricModule
import com.example.follower.di.modules.DIALOG_LOADING
import com.example.follower.di.modules.DIALOG_RESET_TO_DEFAULTS
import com.example.follower.di.modules.SettingsModule
import com.example.follower.ext.errorToast
import com.example.follower.ext.getBooleanOf
import com.example.follower.ext.getStringOf
import com.example.follower.ext.toast
import com.example.follower.screens.biometric.Authenticator
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    @Inject lateinit var authenticator: Authenticator
    @Inject lateinit var viewModel: SettingsViewModel
    @Inject lateinit var prefs: SharedPreferences

    @Inject
    @Named(DIALOG_LOADING)
    lateinit var loadingDialog: Dialog

    @Inject
    @Named(DIALOG_RESET_TO_DEFAULTS)
    lateinit var resetDialog: Dialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModelSubscriptions()
        handleFingerprintAvailability()
    }

    /*TODO: to */
    private fun handleFingerprintAvailability() {
        val biometricPref = findPreference<SwitchPreferenceCompat>(getString(R.string.pref_enable_biometric_protection))!!
        when {
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED -> {
                biometricPref.isEnabled = false
                biometricPref.summary = getString(R.string.summary_lack_of_fingerprint_permission)
            }
            FingerprintManagerCompat.from(requireContext()).isHardwareDetected.not() -> {
                biometricPref.isEnabled = false
                biometricPref.summary = getString(R.string.summary_lack_of_fingerprint_sensor)
            }
            FingerprintManagerCompat.from(requireContext()).hasEnrolledFingerprints().not() -> {
                biometricPref.isEnabled = false
                biometricPref.summary = getString(R.string.summary_you_dont_have_fingerprint_presented)
            }
        }
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
        viewModel.loadingEvent.observe(viewLifecycleOwner, {
            when {
                loadingDialog.isShowing.not() && it -> loadingDialog.show()
                loadingDialog.isShowing && it.not() -> loadingDialog.dismiss()
            }
        })
        viewModel.errorEvent.observe(viewLifecycleOwner, { errorToast(getString(it)) })
        viewModel.successfulSchedulingEvent.observe(viewLifecycleOwner, { toast(getString(it)) })
        viewModel.resetToDefaultsEvent.observe(viewLifecycleOwner, { requireActivity().recreate() })
    }

    override fun onAttach(context: Context) = super.onAttach(context).also {
        (requireContext().applicationContext as FollowerApp)
            .appComponent
            .biometricComponent(
                BiometricModule(requireActivity(),
                    onSuccessfulAuth = { findPreference<SwitchPreferenceCompat>(getString(R.string.pref_enable_biometric_protection))!!.isChecked = false },
                    onFailedAuth = { findPreference<SwitchPreferenceCompat>(getString(R.string.pref_enable_biometric_protection))!!.isChecked = true }
                )
            )
            .settingsComponent(SettingsModule(activity = requireActivity(), resetToDefaults = ::resetToDefaults))
            .inject(this)
    }

    private fun resetToDefaults() = viewModel.resetOptionsToDefaults()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = super.onCreateView(inflater, container, savedInstanceState)
        .also { prefs.registerOnSharedPreferenceChangeListener(this) }

    override fun onDestroyView() = super.onDestroyView().also { prefs.unregisterOnSharedPreferenceChangeListener(this) }
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) = setPreferencesFromResource(R.xml.preferences, rootKey)

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        if (isDetached.not() && preference?.key == getString(R.string.pref_reset_to_default)) {
            resetDialog.show()
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
                    viewModel.scheduleAutoTracking()
                } else {
//                    alarmManager.cancel(PendingIntent.getService(requireContext().applicationContext, ID_AUTO_TRACKING_START, Intent(requireActivity(), LocationTrackingService::class.java),0))
//                    alarmManager.cancel(PendingIntent.getService(requireContext().applicationContext, ID_AUTO_TRACKING_STOP, Intent(requireActivity(), LocationTrackingService::class.java),0))
                }
            }
            getString(R.string.pref_enable_biometric_protection) -> {
                if (sharedPreferences.getBooleanOf(key).not()) {
                    authenticator.authenticate()
                }
            }
        }
    }
}