package dev.liinahamari.follower.screens

import android.content.Context
import android.content.res.Configuration
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.squareup.seismic.ShakeDetector
import dev.liinahamari.follower.FollowerApp
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.BaseFragment
import dev.liinahamari.follower.base.BaseViewModel
import dev.liinahamari.follower.ext.isIgnoringBatteryOptimizations
import dev.liinahamari.follower.helper.SingleLiveEvent
import dev.liinahamari.follower.model.PreferenceRepository
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.follower_pager.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainActivity : AppCompatActivity(R.layout.activity_main), ShakeDetector.Listener {
    @Inject lateinit var sensorManager: SensorManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: MainActivityViewModel by viewModels { viewModelFactory }

    private val disposable = CompositeDisposable()
    private var navigated = false

    private var shakeDetector: ShakeDetector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as FollowerApp).appComponent.inject(this)
        shakeDetector = ShakeDetector(this)
        super.onCreate(savedInstanceState)
        setupViewModelSubscriptions()
        viewModel.getForcedThemeEvent()
    }

    private fun setupViewModelSubscriptions() {
        viewModel.themeEvent.observe(this, {
            setDefaultNightMode(it)
        })
    }

    @MainThread
    override fun hearShake() { //todo to settings fragment
        if (navigated.not()) {
            navigated = true
            mainActivityFragmentContainer.findNavController().navigate(R.id.action_to_logs)

            disposable += Observable.timer(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { navigated = false }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        shakeDetector = null
        disposable.clear()
    }

    override fun onResume() {
        super.onResume()
        shakeDetector!!.start(sensorManager)
        viewModel.updateIsIgnoringBatteryOptimizations(isIgnoringBatteryOptimizations())
    }

    override fun onPause() = super.onPause().also { shakeDetector!!.stop() }
    override fun onSupportNavigateUp(): Boolean = findNavController(R.id.mainActivityFragmentContainer).navigateUp()

    override fun attachBaseContext(base: Context) {
        with (viewModel.getSavedLocale()) {
            Locale.setDefault(this)
            super.attachBaseContext(base.createConfigurationContext(Configuration().also { it.setLocale(this) }))
        }
    }

    class MainActivityViewModel @Inject constructor(private val preferenceRepository: PreferenceRepository): BaseViewModel() {
        private val _themeEvent = SingleLiveEvent<Int>()
        val themeEvent: LiveData<Int> get() = _themeEvent

        fun getForcedThemeEvent() {
            disposable += preferenceRepository.theme.subscribe {
                _themeEvent.value = it
            }
        }

        fun getSavedLocale(): Locale = preferenceRepository.language.blockingSingle()

        fun updateIsIgnoringBatteryOptimizations(value: Boolean) {
            disposable += preferenceRepository.isIgnoringBatteryOptimizations.subscribe {
                if ( value != it) {
                    preferenceRepository.updateIsIgnoringBatteryOptimizations(value)
                }
            }
        }
    }
}

class PagerContainerFragment : BaseFragment(R.layout.follower_pager) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = super.onViewCreated(view, savedInstanceState)
        .also { NavigationUI.setupWithNavController(bottomNavView, childFragmentManager.findFragmentById(R.id.pagerContainer)!!.findNavController()) }
}