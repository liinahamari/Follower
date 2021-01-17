package com.example.follower

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.follower.base.BaseActivity
import com.example.follower.helper.FlightRecorder
import com.jakewharton.rxbinding3.view.clicks
import kotlinx.android.synthetic.main.activity_logs.*
import javax.inject.Inject
import io.reactivex.rxkotlin.plusAssign

class LogsActivity: BaseActivity(R.layout.activity_logs) {
    @Inject lateinit var logger: FlightRecorder

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as FollowerApp).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        supportActionBar?.title = this::class.java.simpleName
        logsContainer.text = logger.getEntireRecord()
        subscriptions += eraseLogButton
            .clicks()
            .throttleFirst()
            .subscribe { logger.clear().also { logsContainer.text = "" } }
    }
}