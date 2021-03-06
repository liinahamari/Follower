/*
Copyright 2020-2021 liinahamari

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

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
import com.squareup.seismic.ShakeDetector
import dev.liinahamari.follower.FollowerApp
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.BaseFragment
import dev.liinahamari.follower.ext.getBooleanOf
import dev.liinahamari.follower.ext.isIgnoringBatteryOptimizations
import dev.liinahamari.follower.ext.provideUpdatedContextWithNewLocale
import dev.liinahamari.follower.ext.writeBooleanOf
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
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

    override fun onResume() {
        super.onResume()
        shakeDetector!!.start(sensorManager)
        if (isIgnoringBatteryOptimizations() != prefs.getBooleanOf(getString(R.string.pref_battery_optimization))) {
            prefs.writeBooleanOf(getString(R.string.pref_battery_optimization), isIgnoringBatteryOptimizations())
        }
    }
    override fun onPause() = super.onPause().also { shakeDetector!!.stop() }
    override fun onSupportNavigateUp(): Boolean = findNavController(R.id.mainActivityFragmentContainer).navigateUp()
    override fun attachBaseContext(base: Context) = super.attachBaseContext(base.provideUpdatedContextWithNewLocale())
}

class PagerContainerFragment : BaseFragment(R.layout.follower_pager) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = super.onViewCreated(view, savedInstanceState)
        .also { NavigationUI.setupWithNavController(bottomNavView, childFragmentManager.findFragmentById(R.id.pagerContainer)!!.findNavController()) }
}