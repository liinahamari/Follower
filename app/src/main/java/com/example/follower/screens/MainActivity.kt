package com.example.follower.screens

import android.content.Context
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
import com.example.follower.ext.provideUpdatedContextWithNewLocale
import com.example.follower.helper.SingleLiveEvent
import com.example.follower.interactors.LocaleChangedResult
import com.example.follower.interactors.MainActivitySettingsInteractor
import com.example.follower.interactors.NightModeChangesResult
import com.squareup.seismic.ShakeDetector
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

class PagerContainerFragment : BaseFragment(R.layout.follower_pager) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = super.onViewCreated(view, savedInstanceState)
        .also { NavigationUI.setupWithNavController(bottomNavView, childFragmentManager.findFragmentById(R.id.pagerContainer)!!.findNavController()) }
}