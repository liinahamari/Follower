package dev.liinahamari.follower.screens.settings

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
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
import dev.liinahamari.follower.model.PreferenceRepository
import dev.liinahamari.follower.screens.tracking_control.PERMISSION_BACKGROUND_LOCATION
import dev.liinahamari.follower.screens.tracking_control.PERMISSION_LOCATION
import io.reactivex.rxjava3.kotlin.zipWith
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
class SettingsFragment : PreferenceFragmentCompat() {
    @Inject lateinit var authenticator: Lazy<Authenticator>
    @Inject lateinit var viewModel: SettingsViewModel
    @Inject lateinit var prefs: PreferenceRepository
    @Inject lateinit var logger: FlightRecorder

    @JvmField
    @Named(IS_ROOTED_BOOL)
    @Inject var isRooted: Boolean = false

    @Named(DIALOG_LOADING)
    @Inject lateinit var loadingDialog: Dialog

    @Named(DIALOG_RESET_TO_DEFAULTS)
    @Inject lateinit var resetDialog: Dialog

    @Named(DIALOG_ROOT_DETECTED)
    @Inject lateinit var rootDetectionDialog: Lazy<Dialog>

    private var themeId = -1

    private val geoPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (it.values.all { accepted -> accepted }) {
            viewModel.scheduleAutoTracking()
        } else {
            errorToast(R.string.error_location_permission_denied)
            findPreference<SwitchPreferenceCompat>(getString(R.string.pref_enable_auto_tracking))?.isChecked = false
            prefs.updateIsAutoTrackingEnabled(false)
        }
    }

    private val batteryOptimization = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            findPreference<SwitchPreferenceCompat>(getString(R.string.pref_battery_optimization))!!.isChecked = true
            prefs.updateIsIgnoringBatteryOptimizations(true)
            successToast(R.string.optimization_successful)
        } else {
            findPreference<SwitchPreferenceCompat>(getString(R.string.pref_battery_optimization))!!.isChecked = false
            prefs.updateIsIgnoringBatteryOptimizations(false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModelSubscriptions()
        viewModel.isBiometricValidationAvailable()
        changeDrawableColors(prefs.isDarkThemeEnabled.blockingSingle())
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) = setPreferencesFromResource(R.xml.preferences, rootKey) /*TODO what about runtime updates of values? if i somewhere changed settings value it should be reflected here*/
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
                        prefs.updateIsEnabledBiometricProtection(false)
                    },
                    onFailedAuth = {
                        findPreference<SwitchPreferenceCompat>(getString(R.string.pref_enable_biometric_protection))!!.isChecked = true
                        prefs.updateIsEnabledBiometricProtection(true)
                    }
                )
            )
            .settingsComponent(SettingsModule(
                activity = requireActivity(),
                resetToDefaults = ::resetToDefaults,
                onAcceptDeviceRooted = {
                    prefs.updateIsRootOk(true)
                    prefs.updateIsEnabledBiometricProtection(true)
                    findPreference<SwitchPreferenceCompat>(getString(R.string.pref_enable_biometric_protection))!!.isChecked = true
                },
                onDeclineDeviceRooted = {
                    prefs.updateIsEnabledBiometricProtection(false)
                    findPreference<SwitchPreferenceCompat>(getString(R.string.pref_enable_biometric_protection))!!.isChecked = false
                })
            )
            .inject(this)

        super.onAttach(context)

        themeId = prefs.theme.blockingSingle()
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
            prefs.updateIsIgnoringBatteryOptimizations(true)
        }
    }

    fun f() {
        prefs.theme.subscribe {
            if (it != themeId) {
                AppCompatDelegate.setDefaultNightMode(it)
                changeDrawableColors(prefs.isDarkThemeEnabled.blockingSingle())
            }
        }

        prefs.language.subscribe {
            with(it) {
                Locale.setDefault(this)
                @Suppress("DEPRECATION") requireActivity().resources.updateConfiguration(resources.configuration.also { it.setLocale(this) }, resources.displayMetrics)
            }
            requireActivity().recreate()
        }

        prefs.isAutoTrackingEnabled.subscribe {
            /*FIXME inconsistency with permissions*/
            //todo show dialog - better to disable battery optimization (with library of making video suggestions!)
            if (it) {
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

        prefs.isBiometricProtectionEnabled
            .zipWith(prefs.isRootOk)
            .subscribe {
            if (isRooted.not() || (isRooted && it.second)) {
                if (it.first.not()) {
                    authenticator.get().authenticate()
                }
            } else {
                rootDetectionDialog.get().show()
            }
        }

        prefs.isAcraDisabled.subscribe {
//            prefs.updateIsAcraEnabled(it.not())
        }

        prefs.autoTrackingStartTime.mergeWith(prefs.autoTrackingStopTime)
            .subscribe { viewModel.scheduleAutoTracking() }

//            getString(R.string.pref_purge_cache) -> viewModel.purgeMapCache() /*        SqlTileWriter().purgeCache()*/
    }

    private fun changeDrawableColors(isDarkThemeEnabled: Boolean) {
        with(requireContext()) {
            adaptToNightModeState(findPreference<Preference>(getString(R.string.pref_battery_optimization_settings))?.icon, isDarkThemeEnabled)
            adaptToNightModeState(findPreference<Preference>(getString(R.string.pref_battery_optimization))?.icon, isDarkThemeEnabled)
            adaptToNightModeState(findPreference<Preference>(getString(R.string.pref_theme))?.icon, isDarkThemeEnabled)
            adaptToNightModeState(findPreference<Preference>(getString(R.string.pref_lang))?.icon, isDarkThemeEnabled)
            adaptToNightModeState(findPreference<Preference>(getString(R.string.pref_report_bug))?.icon, isDarkThemeEnabled)
            adaptToNightModeState(findPreference<Preference>(getString(R.string.pref_enable_biometric_protection))?.icon, isDarkThemeEnabled)
            adaptToNightModeState(findPreference<Preference>(getString(R.string.pref_acra_enable))?.icon, isDarkThemeEnabled)
        }
    }
}