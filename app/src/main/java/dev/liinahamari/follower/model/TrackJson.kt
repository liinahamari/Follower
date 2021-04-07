package dev.liinahamari.follower.model

import com.google.gson.annotations.SerializedName

/** Workaround for fields' obfuscation while creating JSON */

data class TrackJson(
    @SerializedName("title")
    val title: String,
    @SerializedName("time")
    val time: Long,
    @SerializedName("way_points")
    val wayPoints: Array<WayPointJson>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TrackJson

        if (title != other.title) return false
        if (time != other.time) return false
        if (!wayPoints.contentEquals(other.wayPoints)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + wayPoints.contentHashCode()
        return result
    }
}

data class WayPointJson(
    @SerializedName("track_id")
    val trackId: Long,
    @SerializedName("provider")
    val provider: String,
    @SerializedName("lat")
    val latitude: Double,
    @SerializedName("lon")
    val longitude: Double,
    @SerializedName("time")
    val time: Long
)