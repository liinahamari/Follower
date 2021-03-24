package dev.liinahamari.follower.screens

import android.content.Context
import android.content.SharedPreferences
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import dev.liinahamari.follower.FollowerApp
import dev.liinahamari.follower.base.BaseFragment
import dev.liinahamari.follower.ext.provideUpdatedContextWithNewLocale
import com.squareup.seismic.ShakeDetector
import dev.liinahamari.follower.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.follower_pager.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainActivity : AppCompatActivity(R.layout.activity_main), ShakeDetector.Listener {
    @Inject lateinit var sensorManager: SensorManager
    @Inject lateinit var prefs: SharedPreferences

    private val clicks = CompositeDisposable()
    private var navigated = false

    private var shakeDetector: ShakeDetector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as FollowerApp).appComponent.inject(this)
        shakeDetector = ShakeDetector(this)
        super.onCreate(savedInstanceState)

        setDefaultNightMode(prefs.getString(getString(R.string.pref_theme), null)!!.toInt()) /*NPE can be caused by lack of defaultValue in preferences.xml of android:key="@string/pref_theme" */
    }

    @MainThread
    override fun hearShake() {
        if (navigated.not()) {
            navigated = true
            mainActivityFragmentContainer.findNavController().navigate(R.id.action_to_logs)

            clicks += Observable.timer(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { navigated = false }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        shakeDetector = null
        clicks.clear()
    }

    override fun onResume() = super.onResume().also { shakeDetector!!.start(sensorManager) }
    override fun onPause() = super.onPause().also { shakeDetector!!.stop() }
    override fun onSupportNavigateUp(): Boolean = findNavController(R.id.mainActivityFragmentContainer).navigateUp()
    override fun attachBaseContext(base: Context) = super.attachBaseContext(base.provideUpdatedContextWithNewLocale())
}

class PagerContainerFragment : BaseFragment(R.layout.follower_pager) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = super.onViewCreated(view, savedInstanceState)
        .also { NavigationUI.setupWithNavController(bottomNavView, childFragmentManager.findFragmentById(R.id.pagerContainer)!!.findNavController()) }
}