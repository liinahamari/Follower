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

package dev.liinahamari.follower.networking

import dev.liinahamari.follower.model.TrackMode
import dev.liinahamari.follower.screens.trace_map.Latitude
import dev.liinahamari.follower.screens.trace_map.Longitude
import io.reactivex.rxjava3.core.Single
import retrofit2.Response
import retrofit2.http.*

interface ServerService {
    @DELETE("tracks/{time}")
    fun delete(@Path("time") id: String): Single<Response<Unit>>

    @GET("tracks")
    fun getAll(): Single<List<ServerTrack>>

    @PUT("tracks/{time}")
    fun replace(@Body request: ServerTrack, @Path("time") trackId: Long): Single<Response<Unit>>

    @POST("tracks")
    fun put(@Body request: ServerTrack): Single<Response<Unit>>
}

data class ServerTrack(var time: Long, var title: String, var wayPoints: Array<Pair<Longitude, Latitude>>, val trackMode: TrackMode) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ServerTrack

        if (time != other.time) return false
        if (title != other.title) return false
        if (!wayPoints.contentEquals(other.wayPoints)) return false
        if (trackMode != other.trackMode) return false

        return true
    }

    override fun hashCode(): Int {
        var result = time.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + wayPoints.contentHashCode()
        result = 31 * result + trackMode.hashCode()
        return result
    }
}
