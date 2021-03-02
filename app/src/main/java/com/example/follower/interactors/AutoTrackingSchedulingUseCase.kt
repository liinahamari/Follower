package com.example.follower.interactors

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.*
import com.example.follower.R
import com.example.follower.di.scopes.SettingsScope
import com.example.follower.ext.isServiceRunning
import com.example.follower.ext.isTimeBetweenTwoTimes
import com.example.follower.ext.minutesFromMidnightToHourlyTime
import com.example.follower.ext.now
import com.example.follower.helper.rx.BaseComposers
import com.example.follower.services.LocationTrackingService
import com.example.follower.workers.AutoStartTrackingWorker
import com.example.follower.workers.AutoStopTrackingWorker
import com.example.follower.workers.TAG_AUTO_START_WORKER
import com.example.follower.workers.TAG_AUTO_STOP_WORKER
import io.reactivex.Single
import io.reactivex.rxkotlin.toCompletable
import java.util.*
import java.util.concurrent.TimeUnit

@SettingsScope
class AutoTrackingSchedulingUseCase constructor(private val sharedPreferences: SharedPreferences, private val context: Context, private val baseComposers: BaseComposers, private val workManager: WorkManager) {
    fun cancelAutoTracking(): Single<CancelAutoTrackingResult> = Single.fromCallable {
        workManager.cancelUniqueWork(TAG_AUTO_STOP_WORKER)
        workManager.cancelUniqueWork(TAG_AUTO_START_WORKER)
    }
        .flatMapCompletable { it.result.toCompletable() }
        .toSingleDefault<CancelAutoTrackingResult>(CancelAutoTrackingResult.Success)
        .onErrorReturn { CancelAutoTrackingResult.Failure }

    fun setupStartAndStop(): Single<SchedulingStartStopResult> =
        Single.just(sharedPreferences.getInt(context.getString(R.string.pref_tracking_start_time), -1) to sharedPreferences.getInt(context.getString(R.string.pref_tracking_stop_time), -1))
            .doOnSuccess { require(it.first >= 0 && it.second >= 0) }
            .map { minutesFromMidnightToHourlyTime(it.first) to minutesFromMidnightToHourlyTime(it.second) }
            .compose(baseComposers.applySingleSchedulers())
            .doOnSuccess {
                if (isTimeBetweenTwoTimes(it.first, it.second, now())) {
                    if (context.isServiceRunning(LocationTrackingService::class.java).not()) {
                        workManager.enqueueUniquePeriodicWork(TAG_AUTO_START_WORKER, ExistingPeriodicWorkPolicy.KEEP, defaultConstraints<AutoStartTrackingWorker>().build())
                    }
                } else {
                    Log.d("a", "START_WORKER start delay ${(getNextLaunchTime(it.first) - System.currentTimeMillis()) / 3600000}")
                    workManager.enqueueUniquePeriodicWork(
                        TAG_AUTO_START_WORKER,
                        ExistingPeriodicWorkPolicy.KEEP,
                        defaultConstraints<AutoStartTrackingWorker>()
                            .setInitialDelay(getNextLaunchTime(it.first) - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                            .build()
                    )
                }
            }
            .doOnEvent { startStopTimeValues, _ ->
                Log.d("a", "STOP_WORKER stop delay ${(getNextLaunchTime(startStopTimeValues.second) - System.currentTimeMillis()) / 3600000}")
                workManager.enqueueUniquePeriodicWork(
                    TAG_AUTO_STOP_WORKER,
                    ExistingPeriodicWorkPolicy.KEEP,
                    defaultConstraints<AutoStopTrackingWorker>()
                        .setInitialDelay(getNextLaunchTime(startStopTimeValues.second) - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                        .build()
                )
            }
            .map<SchedulingStartStopResult> { SchedulingStartStopResult.Success }
            .onErrorReturn { SchedulingStartStopResult.Failure }

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

    private inline fun <reified T : ListenableWorker> defaultConstraints() = PeriodicWorkRequestBuilder<T>(1, TimeUnit.DAYS)
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(true) /*todo preference?*/
                .setRequiresStorageNotLow(true)
                .build()
        )
        .setBackoffCriteria(BackoffPolicy.LINEAR, PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
}

sealed class SchedulingStartStopResult {
    object Success : SchedulingStartStopResult()
    object Failure : SchedulingStartStopResult()
}

sealed class CancelAutoTrackingResult {
    object Success : CancelAutoTrackingResult()
    object Failure : CancelAutoTrackingResult()
}