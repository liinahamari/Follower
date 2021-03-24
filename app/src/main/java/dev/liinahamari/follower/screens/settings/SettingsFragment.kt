package dev.liinahamari.follower.screens.settings

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import dev.liinahamari.follower.FollowerApp
import dev.liinahamari.follower.R
import dev.liinahamari.follower.di.modules.BiometricModule
import dev.liinahamari.follower.di.modules.DIALOG_LOADING
import dev.liinahamari.follower.di.modules.DIALOG_RESET_TO_DEFAULTS
import dev.liinahamari.follower.di.modules.SettingsModule
import dev.liinahamari.follower.ext.*
import dev.liinahamari.follower.helper.CustomToast.errorToast
import dev.liinahamari.follower.helper.CustomToast.infoToast
import dev.liinahamari.follower.screens.biometric.Authenticator
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

    private var themeId = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModelSubscriptions()
        viewModel.isBiometricValidationAvailable()
        changeDrawableColors(themeId)
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
                    onSuccessfulAuth = { findPreference<SwitchPreferenceCompat>(getString(R.string.pref_enable_biometric_protection))!!.isChecked = false },
                    onFailedAuth = { findPreference<SwitchPreferenceCompat>(getString(R.string.pref_enable_biometric_protection))!!.isChecked = true }
                )
            )
            .settingsComponent(SettingsModule(activity = requireActivity(), resetToDefaults = ::resetToDefaults))
            .inject(this)

        super.onAttach(context)

        themeId = prefs.getStringOf(getString(R.string.pref_theme))!!.toInt()
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        if (isDetached.not() && preference?.key == getString(R.string.pref_reset_to_default)) {
            resetDialog.show()
        }
        return super.onPreferenceTreeClick(preference)
    }

    @SuppressLint("ApplySharedPref")
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            requireContext().getString(R.string.pref_theme) -> {
                if (sharedPreferences.getStringOf(key)!!.toInt() != themeId) {
                    AppCompatDelegate.setDefaultNightMode(sharedPreferences.getStringOf(requireContext().getString(R.string.pref_theme))!!.toInt())
                    changeDrawableColors(sharedPreferences.getStringOf(key)!!.toInt())
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
                    viewModel.scheduleAutoTracking()
                } else {
                    viewModel.cancelAutoTracking()
                }
            }
            getString(R.string.pref_enable_biometric_protection) -> {
                if (sharedPreferences.getBooleanOf(key).not()) {
                    authenticator.authenticate()
                }
            }
            getString(R.string.pref_acra_enable) -> sharedPreferences.writeBooleanOf(getString(R.string.pref_acra_disable), sharedPreferences.getBooleanOf(key).not())
        }
    }

    private fun changeDrawableColors(theme: Int) {
        val isNightMode: Boolean = when (theme) {
            MODE_NIGHT_NO -> false
            MODE_NIGHT_YES -> true
            else -> deviceHasDarkThemeEnabled()
        }
        findPreference<Preference>(getString(R.string.pref_theme))?.setColorDependantOnNightMode(isNightMode)
        findPreference<Preference>(getString(R.string.pref_lang))?.setColorDependantOnNightMode(isNightMode)
        findPreference<Preference>(getString(R.string.pref_report_bug))?.setColorDependantOnNightMode(isNightMode)
        findPreference<Preference>(getString(R.string.pref_enable_biometric_protection))?.setColorDependantOnNightMode(isNightMode)
        findPreference<Preference>(getString(R.string.pref_acra_enable))?.setColorDependantOnNightMode(isNightMode)
    }
}