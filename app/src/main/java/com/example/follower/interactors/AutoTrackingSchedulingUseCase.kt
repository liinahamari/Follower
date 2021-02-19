package com.example.follower.interactors

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.example.follower.R
import com.example.follower.ext.isTimeBetweenTwoTimes
import com.example.follower.ext.minutesFromMidnightToHourlyTime
import com.example.follower.ext.now
import com.example.follower.helper.rx.BaseComposers
import com.example.follower.services.ACTION_START_TRACKING
import com.example.follower.services.ACTION_STOP_TRACKING
import com.example.follower.services.LocationTrackingService
import io.reactivex.Single
import java.util.*
import javax.inject.Inject

const val ID_AUTO_TRACKING_START = 1
const val ID_AUTO_TRACKING_STOP = 2

class AutoTrackingSchedulingUseCase @Inject constructor(private val sharedPreferences: SharedPreferences, private val context: Context, private val alarmManager: AlarmManager, private val baseComposers: BaseComposers) {
    fun execute(): Single<SchedulingResult> = Single.just(sharedPreferences.getInt(context.getString(R.string.pref_tracking_start_time), -1) to sharedPreferences.getInt(context.getString(R.string.pref_tracking_stop_time), -1))
        .doOnSuccess { require(it.first >= 0 && it.second >= 0) }
        .map { minutesFromMidnightToHourlyTime(it.first) to minutesFromMidnightToHourlyTime(it.second) }
        .compose(baseComposers.applySingleSchedulers())
        .doOnSuccess {
            if (isTimeBetweenTwoTimes(it.first, it.second, now())) {
                context.startService(Intent(context, LocationTrackingService::class.java).apply {
                    action = ACTION_START_TRACKING
                })
            } else {
                val startPendingIntent = PendingIntent.getService(
                    context,
                    ID_AUTO_TRACKING_START,
                    Intent(context, LocationTrackingService::class.java).apply {
                        action = ACTION_START_TRACKING
                        /*todo next launch timestamp or WORKER?*/
                    },
                    0
                )
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, getNextLaunchTime(it.first), startPendingIntent)
            }
        }
        .doOnSuccess {
            val stopPendingIntent = PendingIntent.getService(
                context,
                ID_AUTO_TRACKING_STOP,
                Intent(context, LocationTrackingService::class.java).apply {
                    action = ACTION_STOP_TRACKING
                },
                0
            )
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, getNextLaunchTime(it.second), stopPendingIntent)
        }
        .map<SchedulingResult> { SchedulingResult.Success }
        .onErrorReturn { SchedulingResult.Failure }

    /** @param time - Timestamp, implies hours (in 24-hour format) and minutes divided with separator ":". For example, 21:12

     *  @return - Timestamp in format of milliseconds, denoting time in future.
     * */
    private fun getNextLaunchTime(time: String, calendar: Calendar = Calendar.getInstance()): Long { /*todo try to find something like this in JodaTime*/
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        val hours = time.split(":")[0].toInt()
        val minutes = time.split(":")[1].toInt()
        return calendar.apply {
            if (hours > calendar.get(Calendar.HOUR_OF_DAY) || hours == calendar.get(Calendar.HOUR_OF_DAY) && minutes > calendar.get(Calendar.MINUTE)) {
                set(Calendar.DAY_OF_WEEK, today)
            } else {
                add(Calendar.DAY_OF_YEAR, 1)
            }
            set(Calendar.HOUR_OF_DAY, hours)
            set(Calendar.MINUTE, minutes)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}

sealed class SchedulingResult {
    object Success : SchedulingResult()
    object Failure : SchedulingResult()
}