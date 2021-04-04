package dev.liinahamari.follower.screens.settings

import android.app.AlarmManager
import android.app.AlarmManager.RTC_WAKEUP
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import dev.liinahamari.follower.R
import dev.liinahamari.follower.di.modules.APP_CONTEXT
import dev.liinahamari.follower.ext.isServiceRunning
import dev.liinahamari.follower.ext.isTimeBetweenTwoTimes
import dev.liinahamari.follower.ext.minutesFromMidnightToHourlyTime
import dev.liinahamari.follower.ext.nowHoursAndMinutesOnly
import dev.liinahamari.follower.helper.FlightRecorder
import dev.liinahamari.follower.helper.rx.BaseComposers
import dev.liinahamari.follower.receivers.AutoTrackingReceiver
import dev.liinahamari.follower.services.location_tracking.ACTION_START_TRACKING
import dev.liinahamari.follower.services.location_tracking.LocationTrackingService
import io.reactivex.Completable
import io.reactivex.Single
import java.util.*
import javax.inject.Named

class AutoTrackingSchedulingUseCase constructor(
    private val sharedPreferences: SharedPreferences,
    @Named(APP_CONTEXT) private val context: Context,
    private val baseComposers: BaseComposers,
    private val alarmManager: AlarmManager,
    private val logger: FlightRecorder
) {
    fun cancelAutoTracking(): Single<CancelAutoTrackingResult> = Completable.fromCallable {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context.applicationContext,
                AutoTrackingReceiver.ActionMode.ACTION_MODE_START.ordinal,
                Intent(context.applicationContext, AutoTrackingReceiver::class.java),
                0
            )
        )
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context.applicationContext,
                AutoTrackingReceiver.ActionMode.ACTION_MODE_STOP.ordinal,
                Intent(context.applicationContext, AutoTrackingReceiver::class.java),
                0
            )
        )
    }.toSingleDefault<CancelAutoTrackingResult>(CancelAutoTrackingResult.Success)
        .onErrorReturn { CancelAutoTrackingResult.Failure }
        .doOnError { logger.e("Canceling auto-tracking", it) }

    fun setupStartAndStop(): Single<SchedulingStartStopResult> =
        Single.just(sharedPreferences.getInt(context.getString(R.string.pref_tracking_start_time), -1) to sharedPreferences.getInt(context.getString(R.string.pref_tracking_stop_time), -1))
            .doOnSuccess { require(it.first >= 0 && it.second >= 0) }
            .map { minutesFromMidnightToHourlyTime(it.first) to minutesFromMidnightToHourlyTime(it.second) }
            .compose(baseComposers.applySingleSchedulers())
            .doOnSuccess {
                if (isTimeBetweenTwoTimes(it.first, it.second, nowHoursAndMinutesOnly())) {
                    if (context.isServiceRunning(LocationTrackingService::class.java).not()) {
                        context.applicationContext.startService(Intent(context.applicationContext, LocationTrackingService::class.java).apply {
                            action = ACTION_START_TRACKING
                        })
                    } else {
                        logger.i { "AUTO_START delay ${(getNextLaunchTime(it.first) - System.currentTimeMillis()) / 3600000}" }
                        scheduleAutoStart(it.first)
                    }
                } else {
                    /*todo consider 15 minutes delay of AlarmManager (+= 15 min to start if lesser)*/
                    logger.i { "AUTO_START delay ${(getNextLaunchTime(it.first) - System.currentTimeMillis()) / 3600000}" }
                    scheduleAutoStart(it.first)
                }
            }
            .doOnEvent { startStopTimeValues, _ ->
                logger.i { "AUTO_STOP delay ${(getNextLaunchTime(startStopTimeValues.second) - System.currentTimeMillis()) / 3600000}" }
                scheduleAutoStop(startStopTimeValues.second)
            }
            .map<SchedulingStartStopResult> { SchedulingStartStopResult.Success }
            .onErrorReturn { SchedulingStartStopResult.Failure }

    /** @param launchTime - Timestamp, implies hours (in 24-hour format) and minutes divided with separator ":". For example, 21:12

     *  @return - Timestamp in format of milliseconds, denoting time in future.
     * */
    private fun getNextLaunchTime(launchTime: String, calendar: Calendar = Calendar.getInstance()): Long {
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        val hours = launchTime.split(":")[0].toInt()
        val minutes = launchTime.split(":")[1].toInt()
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

    private fun scheduleAutoStart(startTimePref: String) {
        alarmManager.setExactAndAllowWhileIdle(
            RTC_WAKEUP,
            getNextLaunchTime(startTimePref),
            PendingIntent.getBroadcast(
                context.applicationContext,
                AutoTrackingReceiver.ActionMode.ACTION_MODE_START.ordinal,
                AutoTrackingReceiver.createIntent(AutoTrackingReceiver.ActionMode.ACTION_MODE_START, context.applicationContext),
                FLAG_UPDATE_CURRENT
            )
        )
    }

    private fun scheduleAutoStop(stopTimePref: String) {
        alarmManager.setExactAndAllowWhileIdle(
            RTC_WAKEUP,
            getNextLaunchTime(stopTimePref),
            PendingIntent.getBroadcast(
                context.applicationContext,
                AutoTrackingReceiver.ActionMode.ACTION_MODE_STOP.ordinal,
                AutoTrackingReceiver.createIntent(AutoTrackingReceiver.ActionMode.ACTION_MODE_STOP, context.applicationContext),
                FLAG_UPDATE_CURRENT
            )
        )
    }
}

sealed class SchedulingStartStopResult {
    object Success : SchedulingStartStopResult()
    object Failure : SchedulingStartStopResult()
}

sealed class CancelAutoTrackingResult {
    object Success : CancelAutoTrackingResult()
    object Failure : CancelAutoTrackingResult()
}