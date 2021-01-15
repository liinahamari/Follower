package com.example.follower.screens

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.follower.screens.map.MapFragment
import com.example.follower.screens.tracking_control.TrackingControlFragment

class MainScreenViewPagerAdapter(supportFragmentManager: FragmentManager) : FragmentStatePagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getCount(): Int = 2

    override fun getItem(position: Int): Fragment = when (position) {
        0 -> TrackingControlFragment()
        1 -> MapFragment()
        else -> throw IllegalStateException()
    }
}
