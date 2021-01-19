package com.example.follower.screens

import android.content.Intent
import android.hardware.SensorManager
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.follower.FollowerApp
import com.example.follower.R
import com.example.follower.base.BaseActivity
import com.example.follower.screens.logs.LogsActivity
import com.squareup.seismic.ShakeDetector
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : BaseActivity(R.layout.activity_main), ShakeDetector.Listener {
    @Inject lateinit var sensorManager: SensorManager

    private val shakeDetector: ShakeDetector by lazy { ShakeDetector(this) }
    override fun hearShake() = startActivity(Intent(this, LogsActivity::class.java))

    override fun onResume() = super.onResume().also { shakeDetector.start(sensorManager) }
    override fun onPause() = super.onPause().also { shakeDetector.stop() }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as FollowerApp).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        bottom_navigation_view.setupWithNavController((container as NavHostFragment).navController)
    }
}
