package dev.liinahamari.follower.screens.settings

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import dagger.Lazy
import dev.liinahamari.follower.FollowerApp
import dev.liinahamari.follower.R
import dev.liinahamari.follower.di.modules.*
import dev.liinahamari.follower.di.scopes.BiometricScope
import dev.liinahamari.follower.ext.*
import dev.liinahamari.follower.helper.CustomToast.errorToast
import dev.liinahamari.follower.helper.CustomToast.infoToast
import dev.liinahamari.follower.helper.CustomToast.successToast
import dev.liinahamari.follower.helper.FlightRecorder
import dev.liinahamari.follower.screens.tracking_control.PERMISSION_BACKGROUND_LOCATION
import dev.liinahamari.follower.screens.tracking_control.PERMISSION_LOCATION
import java.util.*
import javax.inject.Inject
import javax.inject.Named

private const val MANUFACTURER_XIAOMI = "Xiaomi"
private const val MANUFACTURER_HUAWEI = "HUAWEI"
private const val MANUFACTURER_SAMSUNG = "samsung"
private const val PACKAGE_SAMSUNG_DEVICE_CARE = "com.samsung.android.lool"
private const val CLASS_SAMSUNG_BATTERY_ACTIVITY_S10 = "com.samsung.android.sm.battery.ui.BatteryActivity"
private const val CLASS_SAMSUNG_BATTERY_ACTIVITY_S7 = "com.samsung.android.sm.ui.battery.BatteryActivity"

/* todo add clear cache option */
@BiometricScope
class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    @Inject lateinit var authenticator: Lazy<Authenticator>
    @Inject lateinit var viewModel: SettingsViewModel
    @Inject lateinit var prefs: SharedPreferences
    @Inject lateinit var logger: FlightRecorder

    @Inject
    @Named(DIALOG_LOADING)
    lateinit var loadingDialog: Dialog

    @Inject
    @Named(DIALOG_RESET_TO_DEFAULTS)
    lateinit var resetDialog: Dialog

    private var themeId = -1

    private val geoPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (it.values.all { accepted -> accepted }) {
            viewModel.scheduleAutoTracking()
        } else {
            errorToast(R.string.error_location_permission_denied)
            findPreference<SwitchPreferenceCompat>(getString(R.string.pref_enable_auto_tracking))?.isChecked = false
            prefs.writeBooleanOf(getString(R.string.pref_enable_auto_tracking), false)
        }
    }

    private val batteryOptimization = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            findPreference<SwitchPreferenceCompat>(getString(R.string.pref_battery_optimization))!!.isChecked = true
            prefs.writeBooleanOf(getString(R.string.pref_battery_optimization), true)
            successToast(R.string.optimization_successful)
        } else {
            findPreference<SwitchPreferenceCompat>(getString(R.string.pref_battery_optimization))!!.isChecked = false
            prefs.writeBooleanOf(getString(R.string.pref_battery_optimization), false)
        }
    }

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
        viewModel.biometricNotAvailableEvent.observe(viewLifecycleOwner, {
            findPreference<SwitchPreferenceCompat>(getString(R.string.pref_enable_biometric_protection))!!.apply {
                summary = getString(it)
                isEnabled = false
            }
        })
        viewModel.errorEvent.observe(viewLifecycleOwner, { errorToast(it) })
        viewModel.successfulSchedulingEvent.observe(viewLifecycleOwner, { infoToast(it) })
        viewModel.autoTrackingCancellingEvent.observe(viewLifecycleOwner, { infoToast(it) })
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
                    onFailedAuth = {
                        findPreference<SwitchPreferenceCompat>(getString(R.string.pref_enable_biometric_protection))!!.isChecked = true
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
                getString(R.string.pref_battery_optimization) -> openBatteryOptimizationDialogIfNeeded()
                getString(R.string.pref_battery_optimization_settings) -> openBatteryOptimizationSettings()
            }
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun openBatteryOptimizationSettings() {
        try {
            when (Build.MANUFACTURER) {
                MANUFACTURER_HUAWEI -> {
                    startActivity(
                        Intent().apply {
                            action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                            putExtra("extra_pkgname", requireContext().packageName)
                            putExtra("package: ", requireContext().packageName)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    )
                }
                MANUFACTURER_SAMSUNG -> {
                    startActivity(
                        Intent().apply {
                            component = try {
                                ComponentName(PACKAGE_SAMSUNG_DEVICE_CARE, CLASS_SAMSUNG_BATTERY_ACTIVITY_S7)
                            } catch (e: Exception) {
                                ComponentName(PACKAGE_SAMSUNG_DEVICE_CARE, CLASS_SAMSUNG_BATTERY_ACTIVITY_S10)
                            }
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                }
                MANUFACTURER_XIAOMI -> {
                    startActivity(
                        Intent().apply {
                            component = ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity")
                            putExtra("package_name", context?.packageName)
                            putExtra("package_label", R.string.app_name)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    )
                }
                else -> {
                    startActivity(
                        Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                            putExtra("extra_pkgname", context?.packageName)
                            putExtra("package: ", context?.packageName)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            errorToast(R.string.error_opening_settings)
        }
    }

    @SuppressLint("BatteryLife")
    private fun openBatteryOptimizationDialogIfNeeded() {
        if (isIgnoringBatteryOptimizations().not()) {
            batteryOptimization.launch(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${requireContext().packageName}")
            })
        } else {
            findPreference<SwitchPreferenceCompat>(getString(R.string.pref_battery_optimization))!!.isChecked = true
            prefs.writeBooleanOf(getString(R.string.pref_battery_optimization), true)
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
                        geoPermission.launch(permissions.toTypedArray())
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

            getString(R.string.pref_tracking_start_time), getString(R.string.pref_tracking_stop_time) -> viewModel.scheduleAutoTracking()
        }
    }

    private fun changeDrawableColors() {
        with(requireContext()) {
            adaptToNightModeState(findPreference<Preference>(getString(R.string.pref_battery_optimization_settings))?.icon)
            adaptToNightModeState(findPreference<Preference>(getString(R.string.pref_theme))?.icon)
            adaptToNightModeState(findPreference<Preference>(getString(R.string.pref_lang))?.icon)
            adaptToNightModeState(findPreference<Preference>(getString(R.string.pref_report_bug))?.icon)
            adaptToNightModeState(findPreference<Preference>(getString(R.string.pref_enable_biometric_protection))?.icon)
            adaptToNightModeState(findPreference<Preference>(getString(R.string.pref_acra_enable))?.icon)
        }
    }
}