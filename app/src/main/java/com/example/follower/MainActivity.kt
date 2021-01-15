package com.example.follower

import android.os.Bundle
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
    override fun onCreate(savedInstanceState: Bundle?) {
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
