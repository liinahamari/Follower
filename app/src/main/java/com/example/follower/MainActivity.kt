package com.example.follower

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.follower.base.BaseActivity
import com.example.follower.screens.SettingsFragment
import com.example.follower.screens.map.MapFragment
import com.example.follower.screens.tracking_control.TrackingControlFragment
import kotlinx.android.synthetic.main.activity_main.*

private const val PAGE_EXTRA = "MainActivity.page_extra"

class MainActivity : BaseActivity(R.layout.activity_main) {
    private var gpsService: LocationTrackingService? = null

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            if (className.className.endsWith(LocationTrackingService::class.java.simpleName)) {
                Log.d("a", "zzz connected")
                gpsService = (service as LocationTrackingService.LocationServiceBinder).service
                subscriptions += gpsService!!.isTracking./*todo debounce?*/subscribe {
                    toggleButtons(it)
                }
            }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(pager) {
            adapter = MainScreenViewPagerAdapter(supportFragmentManager)
            globalMenu.setupWithViewPager(this)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            if (className.className == LocationTrackingService::class.java.simpleName) {
                Log.d("a", "zzz disconnected")
                gpsService = null
            }
        }
    }
    }/*todo bug with different states of menu and pager*/

    override fun onStart() = super.onStart().also { bindService(Intent(this, LocationTrackingService::class.java), serviceConnection, BIND_AUTO_CREATE) }
        gpsService?.let {
    override fun onStop() = super.onStop().also {
            unbindService(serviceConnection)
            gpsService = null
        }
    }
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
