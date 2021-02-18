package com.example.follower.screens.settings

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.afollestad.materialdialogs.MaterialDialog
import com.example.follower.FollowerApp
import com.example.follower.R
import com.example.follower.base.BaseViewModel
import com.example.follower.ext.errorToast
import com.example.follower.ext.getStringOf
import com.example.follower.ext.writeStringOf
import com.example.follower.helper.SingleLiveEvent
import com.example.follower.helper.rx.BaseComposers
import com.example.follower.interactors.DEFAULT_LOCATION_UPDATE_INTERVAL
import com.example.follower.interactors.DEFAULT_TIME_UPDATE_INTERVAL
import com.example.follower.interactors.ResetToDefaultsState
import com.example.follower.interactors.SettingsPrefsInteractor
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.plusAssign
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<SettingsViewModel> { viewModelFactory }

    private val loadingDialog by lazy {
        Dialog(requireContext(), R.style.DialogNoPaddingNoTitle).apply {
            setContentView(R.layout.dialog_saving)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
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
        viewModel.loadingEvent.observe(viewLifecycleOwner, {
            when {
                loadingDialog.isShowing.not() && it -> loadingDialog.show()
                loadingDialog.isShowing && it.not() -> loadingDialog.dismiss()
            }
        })
        viewModel.errorEvent.observe(viewLifecycleOwner, { errorToast(getString(it)) })
        viewModel.resetToDefaultsEvent.observe(viewLifecycleOwner, { requireActivity().recreate() })
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
                positiveButton(R.string.title_continue) { viewModel.resetOptionsToDefaults() }
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
        }
    }
}