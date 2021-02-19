package com.example.follower.screens.settings

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.follower.R
import com.example.follower.base.BaseViewModel
import com.example.follower.ext.isTimeBetweenTwoTimes
import com.example.follower.ext.minutesFromMidnightToHourlyTime
import com.example.follower.ext.now
import com.example.follower.ext.toReadableDate
import com.example.follower.helper.SingleLiveEvent
import com.example.follower.interactors.ResetToDefaultsState
import com.example.follower.interactors.SettingsPrefsInteractor
import com.example.follower.services.ACTION_START_TRACKING
import com.example.follower.services.ACTION_STOP_TRACKING
import com.example.follower.services.LocationTrackingService
import io.reactivex.rxkotlin.plusAssign
import java.util.*
import javax.inject.Inject

class SettingsViewModel @Inject constructor(private val prefInteractor: SettingsPrefsInteractor, private val nextLaunchTimeCalculationUseCase: NextLaunchTimeCalculationUseCase) : BaseViewModel() {
    private val _errorEvent = SingleLiveEvent<Int>()
    val errorEvent: LiveData<Int> get() = _errorEvent

    private val _resetToDefaultsEvent = SingleLiveEvent<Any>()
    val resetToDefaultsEvent: LiveData<Any> get() = _resetToDefaultsEvent

    private val _loadingEvent = SingleLiveEvent<Boolean>()
    val loadingEvent: LiveData<Boolean> get() = _loadingEvent

    fun resetOptionsToDefaults() {
        disposable += prefInteractor.resetOptionsToDefaults().subscribe {
            when (it) {
                is ResetToDefaultsState.Success -> {
                    _loadingEvent.value = false
                    _resetToDefaultsEvent.call()
                }
                is ResetToDefaultsState.Failure -> {
                    _loadingEvent.value = false
                    _errorEvent.value = R.string.error_unexpected
                }
                is ResetToDefaultsState.Loading -> _loadingEvent.value = true
            }
        }
    }


    fun scheduleAutoTracking() = nextLaunchTimeCalculationUseCase.execute()
}

const val ID_AUTO_TRACKING_START = 1
const val ID_AUTO_TRACKING_STOP = 2

class NextLaunchTimeCalculationUseCase @Inject constructor(private val sharedPreferences: SharedPreferences, private val context: Context, private val alarmManager: AlarmManager) {
    fun execute() {
        /*TODO: MAPPER?*/
        val start = sharedPreferences.getInt(context.getString(R.string.pref_tracking_start_time), -1)
        val stop = sharedPreferences.getInt(context.getString(R.string.pref_tracking_stop_time), -1)
        require(start >= 0 && stop >= 0)

        val startTime = minutesFromMidnightToHourlyTime(start)
        val stopTime = minutesFromMidnightToHourlyTime(stop)

        if (isTimeBetweenTwoTimes(startTime, stopTime, now())) {
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
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, getNextLaunchTime(startTime), startPendingIntent)
        }

        val stopPendingIntent = PendingIntent.getService(
            context,
            ID_AUTO_TRACKING_STOP,
            Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_STOP_TRACKING
            },
            0
        )
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, getNextLaunchTime(stopTime), stopPendingIntent)
    }

    /** @param time - Timestamp, implies hours (in 24-hour format) and minutes divided with separator ":". For example, 21:12

     *  @return - Timestamp in format of milliseconds, denoting time in future.
     * */
    private fun getNextLaunchTime(time: String, calendar: Calendar = Calendar.getInstance()): Long {
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