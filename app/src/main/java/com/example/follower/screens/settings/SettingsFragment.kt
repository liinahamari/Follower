package com.example.follower.screens.settings

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.follower.FollowerApp
import com.example.follower.R
import com.example.follower.di.modules.*
import com.example.follower.di.scopes.BiometricScope
import com.example.follower.ext.*
import com.example.follower.helper.CustomToast.errorToast
import com.example.follower.helper.CustomToast.infoToast
import com.example.follower.screens.tracking_control.CODE_PERMISSION_LOCATION
import com.example.follower.screens.tracking_control.PERMISSION_BACKGROUND_LOCATION
import com.example.follower.screens.tracking_control.PERMISSION_LOCATION
import dagger.Lazy
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@BiometricScope
class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    @Inject lateinit var authenticator: Lazy<Authenticator>
    @Inject lateinit var viewModel: SettingsViewModel
    @Inject lateinit var prefs: SharedPreferences

    @Inject
    @Named(DIALOG_LOADING)
    lateinit var loadingDialog: Dialog

    @Inject
    @Named(DIALOG_RESET_TO_DEFAULTS)
    lateinit var resetDialog: Dialog

    private var themeId = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModelSubscriptions()
        viewModel.isBiometricValidationAvailable()
        changeDrawableColors()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = super.onCreateView(inflater, container, savedInstanceState).also { prefs.registerOnSharedPreferenceChangeListener(this) }
    override fun onDestroyView() = super.onDestroyView().also { prefs.unregisterOnSharedPreferenceChangeListener(this) }
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) = setPreferencesFromResource(R.xml.preferences, rootKey)
    private fun resetToDefaults() = viewModel.resetOptionsToDefaults()

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
        viewModel.biometricNotAvailable.observe(viewLifecycleOwner, {
            findPreference<SwitchPreferenceCompat>(getString(R.string.pref_enable_biometric_protection))!!.apply {
                summary = getString(it)
                isEnabled = false
            }
        })
        viewModel.errorEvent.observe(viewLifecycleOwner, { errorToast(getString(it)) })
        viewModel.successfulSchedulingEvent.observe(viewLifecycleOwner, { infoToast(getString(it)) })
        viewModel.autoTrackingCancellingEvent.observe(viewLifecycleOwner, { infoToast(getString(it)) })
        viewModel.resetToDefaultsEvent.observe(viewLifecycleOwner, { requireActivity().recreate() })
    }

    override fun onAttach(context: Context) {
        (requireContext().applicationContext as FollowerApp)
            .appComponent
            .biometricComponent(
                BiometricModule(requireActivity(),
                    onSuccessfulAuth = {
                        findPreference<SwitchPreferenceCompat>(getString(R.string.pref_enable_biometric_protection))!!.isChecked = false
                        prefs.writeBooleanOf(getString(R.string.pref_enable_biometric_protection), false)
                    },
                    onFailedAuth = { findPreference<SwitchPreferenceCompat>(getString(R.string.pref_enable_biometric_protection))!!.isChecked = true
                        prefs.writeBooleanOf(getString(R.string.pref_enable_biometric_protection), true)
                    }
                )
            )
            .settingsComponent(SettingsModule(activity = requireActivity(), resetToDefaults = ::resetToDefaults))
            .inject(this)

        super.onAttach(context)

        themeId = prefs.getStringOf(getString(R.string.pref_theme))!!.toInt()
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        if (isDetached.not()) {
            when (preference?.key) {
                getString(R.string.pref_reset_to_default) -> resetDialog.show()
            }
        }
        return super.onPreferenceTreeClick(preference)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        @Suppress("DEPRECATION") /* new API with registerForActivityResult(ActivityResultContract, ActivityResultCallback)} instead doesn't work! :( */
        /*Maybe someday... https://developer.android.com/training/permissions/requesting*/
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (isDetached.not() && requestCode == CODE_PERMISSION_LOCATION) {
            val permissionsToHandle = mutableListOf(PERMISSION_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                /* https://stackoverflow.com/questions/58816066/android-10-q-access-background-location-permission */
                permissionsToHandle.add(PERMISSION_BACKGROUND_LOCATION)
            }

            handleUsersReactionToPermissions(
                permissionsToHandle = permissionsToHandle,
                allPermissions = permissions.toList(),
                doIfAllowed = { viewModel.scheduleAutoTracking() },
                doIfDenied = {
                    errorToast(getString(R.string.error_location_permission_denied))
                    findPreference<SwitchPreferenceCompat>(getString(R.string.pref_enable_auto_tracking))?.isChecked = false
                    prefs.writeBooleanOf(getString(R.string.pref_enable_auto_tracking), false)
                },
                doIfNeverAskAgain = {
                    errorToast(getString(R.string.error_location_permission_denied))
                    prefs.writeBooleanOf(getString(R.string.pref_enable_auto_tracking), false)
                    findPreference<SwitchPreferenceCompat>(getString(R.string.pref_enable_auto_tracking))?.isChecked = false
                }
            )
        }
    }

    @SuppressLint("ApplySharedPref")
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            requireContext().getString(R.string.pref_theme) -> {
                if (sharedPreferences.getStringOf(key)!!.toInt() != themeId) {
                    AppCompatDelegate.setDefaultNightMode(sharedPreferences.getStringOf(requireContext().getString(R.string.pref_theme))!!.toInt())
                    changeDrawableColors()
                }
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
                    val permissions = mutableListOf(PERMISSION_LOCATION)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        permissions.add(PERMISSION_BACKGROUND_LOCATION)
                    }
                    if (hasAllPermissions(permissions)) {
                        viewModel.scheduleAutoTracking()
                    } else {
                        @Suppress("DEPRECATION")
                        // new API with registerForActivityResult(ActivityResultContract, ActivityResultCallback)} instead doesn't work! :(
                        // Maybe someday... https://developer.android.com/training/permissions/requesting
                        requestPermissions(permissions.toTypedArray(), CODE_PERMISSION_LOCATION)
                    }
                } else {
                    viewModel.cancelAutoTracking()
                }
            }
            getString(R.string.pref_enable_biometric_protection) -> {
                if (sharedPreferences.getBooleanOf(key).not()) {
                    authenticator.get().authenticate()
                }
            }
            getString(R.string.pref_acra_enable) -> sharedPreferences.writeBooleanOf(getString(R.string.pref_acra_disable), sharedPreferences.getBooleanOf(key).not())

            getString(R.string.pref_tracking_start_time), getString(R.string.pref_tracking_stop_time) -> viewModel.scheduleAutoTracking() /*no need to cancel, cause ExistingPeriodicWorkPolicy.REPLACE politics applying in AutoTrackingSchedulingUseCase*/
        }
    }

    private fun changeDrawableColors() {
        with(requireContext()) {
            adaptToNightModeState(findPreference<Preference>(getString(R.string.pref_theme))?.icon)
            adaptToNightModeState(findPreference<Preference>(getString(R.string.pref_lang))?.icon)
            adaptToNightModeState(findPreference<Preference>(getString(R.string.pref_report_bug))?.icon)
            adaptToNightModeState(findPreference<Preference>(getString(R.string.pref_enable_biometric_protection))?.icon)
            adaptToNightModeState(findPreference<Preference>(getString(R.string.pref_acra_enable))?.icon)
        }
    }
}