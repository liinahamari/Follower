/*
Copyright 2020-2021 liinahamari

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package dev.liinahamari.follower.services.location_tracking

import android.content.Context
import android.content.SharedPreferences
import dev.liinahamari.follower.R
import dev.liinahamari.follower.di.modules.APP_CONTEXT
import io.reactivex.rxjava3.core.Single
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