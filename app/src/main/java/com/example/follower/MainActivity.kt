package com.example.follower

import android.content.Intent
import android.hardware.SensorManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.follower.base.BaseActivity
import com.example.follower.screens.logs.LogsActivity
import com.example.follower.screens.settings.SettingsFragment
import com.example.follower.screens.map.MapFragment
import com.example.follower.screens.tracking_control.TrackingControlFragment
import com.squareup.seismic.ShakeDetector
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

private const val PAGE_EXTRA = "MainActivity.page_extra"

class MainActivity : BaseActivity(R.layout.activity_main), ShakeDetector.Listener {
    @Inject lateinit var sensorManager: SensorManager
    private val shakeDetector: ShakeDetector by lazy { ShakeDetector(this) }
    override fun hearShake() = startActivity(Intent(this, LogsActivity::class.java))

    override fun onResume() = super.onResume().also { shakeDetector.start(sensorManager) }
    override fun onPause() = super.onPause().also { shakeDetector.stop() }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as FollowerApp).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        with(pager) {
            adapter = MainScreenViewPagerAdapter(supportFragmentManager)
            globalMenu.setupWithViewPager(this)
        }

    }/*todo bug with different states of menu and pager*/

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        with(savedInstanceState.getInt(PAGE_EXTRA)) {
            globalMenu.selectTabAt(this, false)
            pager.currentItem = this
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(PAGE_EXTRA, pager.currentItem)
    }

    private inner class MainScreenViewPagerAdapter(supportFragmentManager: FragmentManager) : FragmentStatePagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val fragments = arrayOf(TrackingControlFragment(), MapFragment(), SettingsFragment())
        override fun getCount(): Int = fragments.size
        override fun getItem(position: Int): Fragment = fragments[position]
    }
}
