package com.example.follower.services.location_tracking

import android.content.Context
import android.content.SharedPreferences
import com.example.follower.R
import com.example.follower.di.modules.APP_CONTEXT
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named

const val DEFAULT_TIME_UPDATE_INTERVAL = 5L
const val DEFAULT_LOCATION_UPDATE_INTERVAL = 10f

class LocationPreferenceInteractor @Inject constructor(private val sharedPreferences: SharedPreferences, @Named(APP_CONTEXT) private val context: Context) {
    fun getTimeIntervalBetweenUpdates(): Single<GetTimeIntervalResult> = Single.just(context.getString(R.string.pref_min_location_update_interval))
        .map { sharedPreferences.getString(it, null)?.toLong() ?: DEFAULT_TIME_UPDATE_INTERVAL }
        .map { return@map if(it == 0L) DEFAULT_TIME_UPDATE_INTERVAL else it }
        .map { it * 1000 }
        .map<GetTimeIntervalResult> { GetTimeIntervalResult.Success(it) }
        .onErrorReturn { GetTimeIntervalResult.Success(DEFAULT_TIME_UPDATE_INTERVAL) }

    fun getDistanceBetweenUpdates(): Single<GetDistanceResult> = Single.just(context.getString(R.string.pref_min_distance))
        .map { sharedPreferences.getString(it, null)?.toFloat() ?: DEFAULT_LOCATION_UPDATE_INTERVAL }
        .map { return@map if(it == 0f) DEFAULT_LOCATION_UPDATE_INTERVAL else it }
        .map<GetDistanceResult> { GetDistanceResult.Success(it) }
        .onErrorReturn { GetDistanceResult.Success(DEFAULT_LOCATION_UPDATE_INTERVAL) }
}

sealed class GetTimeIntervalResult {
    data class Success(val timeInterval: Long) : GetTimeIntervalResult()
}

sealed class GetDistanceResult {
    data class Success(val distanceBetweenUpdates: Float) : GetDistanceResult()
}