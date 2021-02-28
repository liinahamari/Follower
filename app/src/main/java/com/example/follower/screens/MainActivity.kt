package com.example.follower.screens

import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.follower.FollowerApp
import com.example.follower.R
import com.example.follower.base.BaseFragment
import com.squareup.seismic.ShakeDetector
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.follower_pager.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(R.layout.activity_main), ShakeDetector.Listener {
    @Inject lateinit var sensorManager: SensorManager
    private val shakeDetector: ShakeDetector by lazy { ShakeDetector(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as FollowerApp).appComponent.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun hearShake() = mainActivityFragmentContainer.findNavController().navigate(R.id.action_to_logs)
    override fun onResume() = super.onResume().also { shakeDetector.start(sensorManager) }
    override fun onPause() = super.onPause().also { shakeDetector.stop() }
    override fun onSupportNavigateUp(): Boolean = findNavController(R.id.mainActivityFragmentContainer).navigateUp()
}

class PagerContainerFragment : BaseFragment(R.layout.follower_pager) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = super.onViewCreated(view, savedInstanceState)
        .also { NavigationUI.setupWithNavController(bottomNavView, childFragmentManager.findFragmentById(R.id.pagerContainer)!!.findNavController()) }
}