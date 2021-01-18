package com.example.follower.screens.logs

import android.os.Bundle
import com.example.follower.FollowerApp
import com.example.follower.R
import com.example.follower.base.BaseActivity
import com.example.follower.helper.FlightRecorder
import com.example.follower.ext.throttleFirst
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.activity_logs.*
import javax.inject.Inject

class LogsActivity: BaseActivity(R.layout.activity_logs) {
    @Inject lateinit var logger: FlightRecorder

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as FollowerApp).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        logsContainer.text = logger.getEntireRecord()
        subscriptions += eraseLogButton
            .clicks()
            .throttleFirst()
            .subscribe { logger.clear().also { logsContainer.text = "" } }
    }
}