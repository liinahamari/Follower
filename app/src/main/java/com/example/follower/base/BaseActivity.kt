package com.example.follower.base

import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.follower.FollowerApp
import com.example.follower.helper.SingleLiveEvent
import com.example.follower.interactors.BaseActivitySettingsInteractor
import com.example.follower.interactors.LocaleChangedResult
import com.example.follower.interactors.NightModeChangesResult
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

open class BaseActivity(@LayoutRes layout: Int) : AppCompatActivity(layout) {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<BaseActivityViewModel> { viewModelFactory }

    protected val subscriptions = CompositeDisposable()
    override fun onDestroy() = super.onDestroy().also { subscriptions.clear() }

    private lateinit var currentLocale: String

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as FollowerApp).appComponent.inject(this)

        super.onCreate(savedInstanceState)
//        currentLocale = resources.configuration.getLocalesLanguage()

        viewModel.recreateEvent.observe(this, Observer {
            recreate()
        })
        viewModel.nightModeChangedEvent.observe(this, Observer {
            AppCompatDelegate.setDefaultNightMode(it)
            recreate()
        })
        viewModel.checkNightModeState(AppCompatDelegate.getDefaultNightMode())
    }

    override fun onRestart() = super.onRestart().also { viewModel.checkLocaleChanged(currentLocale) }
//    override fun attachBaseContext(base: Context) = super.attachBaseContext(base.provideUpdatedContextWithNewLocale())

    class BaseActivityViewModel @Inject constructor(private val prefInteractor: BaseActivitySettingsInteractor): BaseViewModel() {
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