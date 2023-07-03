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

package dev.liinahamari.follower.model

import com.google.gson.annotations.SerializedName

/** Workaround for fields' obfuscation while creating JSON */

data class TrackJson(
    @SerializedName("title")
    val title: String,
    @SerializedName("time")
    val time: Long,
    @SerializedName("way_points")
    val wayPoints: Array<WayPointJson>,
    @SerializedName("track_mode")
    val trackMode: TrackMode,
    @SerializedName("track_length")
    val trackLength: Double
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
