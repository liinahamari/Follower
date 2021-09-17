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

package dev.liinahamari.follower.screens.address_trace

import android.app.Application
import android.location.Geocoder
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava3.cachedIn
import androidx.paging.rxjava3.flowable
import androidx.paging.rxjava3.mapAsync
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.BaseViewModel
import dev.liinahamari.follower.ext.toReadableDate
import dev.liinahamari.follower.model.WayPointDao
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*
import javax.inject.Inject

class AddressTraceViewModel @Inject constructor(private val context: Application, private val wayPointDao: WayPointDao) : BaseViewModel() {
    @ExperimentalCoroutinesApi
    fun getLogsByTraceId(traceId: Long): Flowable<PagingData<MapPointer>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_CAPACITY.toInt(),
            enablePlaceholders = true,
            maxSize = (PAGE_CAPACITY * 5).toInt(),
            prefetchDistance = (PAGE_CAPACITY / 2).toInt(),
            initialLoadSize = PAGE_CAPACITY.toInt()
        ),
        pagingSourceFactory = { wayPointDao.getAllByTrackId(traceId) }
    )
        .flowable
        .map {
            it.mapAsync { wp ->
                Single.just(Geocoder(context, Locale.getDefault()))
                    .map {
                        kotlin.runCatching {
                            it.getFromLocation(wp.latitude, wp.longitude, 1)
                                .first()
                                .getAddressLine(0)
                        }.getOrNull() ?: String.format(context.getString(R.string.address_unknown), wp.longitude, wp.latitude)
                    }
                    .map { address ->
                        MapPointer(address, wp.latitude, wp.longitude, wp.time.toReadableDate())
                    }
            }
        }
        .cachedIn(viewModelScope)
}
