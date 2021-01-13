package com.example.follower.screens.show_trace

import android.os.Bundle
import com.example.follower.FlightRecorder
import com.example.follower.FollowerApp
import com.example.follower.R
import com.example.follower.base.BaseActivity
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_show_trace.*
import javax.inject.Inject

class ShowTraceActivity : BaseActivity(R.layout.activity_show_trace) {
    @Inject lateinit var mapper: AddressMapper
    @Inject lateinit var logger: FlightRecorder

    override fun onCreate(savedInstanceState: Bundle?) {
        (applicationContext as FollowerApp)
            .appComponent.showTraceComponent().build()
            .inject(this)

        super.onCreate(savedInstanceState)

        subscriptions += Single.fromCallable {
            logger.getEntireRecord()
                .split("\n\n")
                .asSequence()
                .filter { it.contains("Location Changed") }
                .map {
                    val (lat, long) = "Location Changed\\. lat:(\\d+\\.\\d+), long:(\\d+\\.\\d+)".toRegex().find(it)!!.groupValues.drop(1)
                    mapper.transform(lat.toDouble(), long.toDouble())
                }
                .map { it to "(\\w+\\s*\\d*[\\\\/\\w]*\\d*),".toRegex().find(it)!!.groupValues[1] }
                .distinctBy { it.second }
                .map { it.first }
                .joinToString(separator = "\n")
        }
            .doOnError { logger.e(stackTrace = it.stackTrace) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer {
                logsContainer.text = it
            })
    }
}