package com.example.follower.screens

import android.content.Context
import android.content.SharedPreferences
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.follower.FollowerApp
import com.example.follower.R
import com.example.follower.base.BaseFragment
import com.example.follower.base.BaseViewModel
import com.example.follower.ext.getLocalesLanguage
import com.example.follower.ext.getStringOf
import com.example.follower.ext.provideUpdatedContextWithNewLocale
import com.example.follower.ext.writeStringOf
import com.example.follower.helper.FlightRecorder
import com.example.follower.helper.SingleLiveEvent
import com.example.follower.helper.rx.BaseComposers
import com.squareup.seismic.ShakeDetector
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.follower_pager.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(R.layout.activity_main), ShakeDetector.Listener {
    @Inject lateinit var sensorManager: SensorManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<MainActivityViewModel> { viewModelFactory }

    private var shakeDetector: ShakeDetector? = null
    private lateinit var currentLocale: String

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as FollowerApp).appComponent.inject(this)
        shakeDetector = ShakeDetector(this)

        super.onCreate(savedInstanceState)

        setupViewModelSubscriptions()
        currentLocale = resources.configuration.getLocalesLanguage()
        viewModel.checkNightModeState(AppCompatDelegate.getDefaultNightMode())
    }

    override fun hearShake() = mainActivityFragmentContainer.findNavController().navigate(R.id.action_to_logs)
    override fun onResume() = super.onResume().also { shakeDetector!!.start(sensorManager) }
    override fun onPause() = super.onPause().also { shakeDetector!!.stop() }
    override fun onSupportNavigateUp(): Boolean = findNavController(R.id.mainActivityFragmentContainer).navigateUp()
    override fun onDestroy() = super.onDestroy().also { shakeDetector = null }
    override fun onRestart() = super.onRestart().also { viewModel.checkLocaleChanged(currentLocale) }
    override fun attachBaseContext(base: Context) = super.attachBaseContext(base.provideUpdatedContextWithNewLocale())

    private fun setupViewModelSubscriptions(){
        viewModel.recreateEvent.observe(this, {
            recreate()
        })
        viewModel.nightModeChangedEvent.observe(this, {
            AppCompatDelegate.setDefaultNightMode(it)
            recreate()
        })
    }

    class MainActivityViewModel @Inject constructor(private val prefInteractor: MainActivitySettingsInteractor): BaseViewModel() {
        private val _setNightModeValueAndRecreateEvent = SingleLiveEvent<Int>()
        val nightModeChangedEvent: LiveData<Int> get() = _setNightModeValueAndRecreateEvent

        private val _recreateEvent = SingleLiveEvent<Any>()
        val recreateEvent: LiveData<Any> get() = _recreateEvent

        fun checkNightModeState(toBeCompared: Int) {
            disposable += prefInteractor.handleThemeChanges(toBeCompared).subscribe {
                if (it is NightModeChangesResult.Success) {
                    _setNightModeValueAndRecreateEvent.value = it.code
                }
            }
        }

        fun checkLocaleChanged(currentLocale: String) {
            disposable += prefInteractor.checkLocaleChanged(currentLocale).subscribe {
                if (it is LocaleChangedResult.Success) {
                    _recreateEvent.call()
                }
            }
        }
    }
}

class MainActivitySettingsInteractor @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val baseComposers: BaseComposers,
    private val logger: FlightRecorder,
    private val context: Context
) {
    fun handleThemeChanges(toBeCompared: Int): Maybe<NightModeChangesResult> = Single.fromCallable {
        kotlin.runCatching { sharedPreferences.getStringOf(context.getString(R.string.pref_theme)) }.getOrThrow()
    }
        .map { it.toInt() }
        .filter { it != toBeCompared }
        .map<NightModeChangesResult> { if (it == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM || it == AppCompatDelegate.MODE_NIGHT_NO || it == AppCompatDelegate.MODE_NIGHT_YES) NightModeChangesResult.Success(it) else NightModeChangesResult.Success(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) }
        .onErrorReturn {
            if (it is NullPointerException) { /**pref_theme contains null: doing initial setup... */
                try {
                    with (AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
                        sharedPreferences.writeStringOf(context.getString(R.string.pref_theme), this.toString())
                        NightModeChangesResult.Success(this)
                    }
                } catch (e: Throwable) {
                    NightModeChangesResult.SharedChangesCorruptionError
                }
            } else {
                NightModeChangesResult.SharedChangesCorruptionError
            }
        }.onErrorReturn { NightModeChangesResult.SharedChangesCorruptionError }
        .doOnError { logger.e(label = "Problem with changing theme!", stackTrace = it.stackTrace) }
        .compose(baseComposers.applyMaybeSchedulers())

    fun checkLocaleChanged(currentLocale: String): Maybe<LocaleChangedResult> = Single.just(currentLocale)
        .filter { sharedPreferences.getStringOf(context.getString(R.string.pref_lang)).equals(it).not() }
        .map<LocaleChangedResult> { LocaleChangedResult.Success }
        .onErrorReturn { LocaleChangedResult.SharedPreferencesCorruptionError }
        .doOnError { logger.e(label = "locale change", stackTrace = it.stackTrace) }
        .compose(baseComposers.applyMaybeSchedulers())
}

sealed class NightModeChangesResult {
    data class Success(val code: Int) : NightModeChangesResult()
    object SharedChangesCorruptionError : NightModeChangesResult()
}

sealed class LocaleChangedResult {
    object Success : LocaleChangedResult()
    object SharedPreferencesCorruptionError : LocaleChangedResult()
}


class PagerContainerFragment : BaseFragment(R.layout.follower_pager) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = super.onViewCreated(view, savedInstanceState)
        .also { NavigationUI.setupWithNavController(bottomNavView, childFragmentManager.findFragmentById(R.id.pagerContainer)!!.findNavController()) }
}