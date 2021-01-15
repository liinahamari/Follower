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

class MainActivity : BaseActivity(R.layout.activity_main) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pager.adapter = MainScreenViewPagerAdapter(supportFragmentManager)
        globalMenu.setupWithViewPager(pager)
    }

    private inner class MainScreenViewPagerAdapter(supportFragmentManager: FragmentManager) : FragmentStatePagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int = 3

        override fun getItem(position: Int): Fragment = when (position) {
            0 -> TrackingControlFragment()
            1 -> MapFragment()
            2 -> SettingsFragment()
            else -> throw IllegalStateException()
        }
    }
}
