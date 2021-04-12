package dev.liinahamari.follower.networking

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

data class ServerTrack(var time: Long, var title: String, var wayPoints: Array<Pair<Longitude, Latitude>>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ServerTrack

        if (time != other.time) return false
        if (title != other.title) return false
        if (wayPoints.contentEquals(other.wayPoints).not()) return false

        return true
    }

    override fun hashCode(): Int {
        var result = time.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + wayPoints.contentHashCode()
        return result
    }
}
