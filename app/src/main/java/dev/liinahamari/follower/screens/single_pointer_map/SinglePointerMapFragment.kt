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

package dev.liinahamari.follower.screens.single_pointer_map

import android.os.Bundle
import android.view.View
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.MapFragment
import dev.liinahamari.follower.ext.MarkerType
import dev.liinahamari.follower.ext.createMarker
import dev.liinahamari.follower.ext.getScreenHeightPx
import kotlinx.android.synthetic.main.fragment_map.*
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint

class SinglePointerMapFragment : MapFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val time: String = arguments?.getString(getString(R.string.arg_toSinglePointerMap_Time), getString(R.string.unknown_time))!!
        val longitude = arguments?.getFloat(getString(R.string.arg_toSinglePointerMap_Longitude), -999.0f)!!
        val latitude = arguments?.getFloat(getString(R.string.arg_toSinglePointerMap_Latitude), -999.0f)!!

        require(latitude != -999.0f && longitude != -999.0f)

        map.maxZoomLevel = 14.0
        map.createMarker(longitude.toDouble(), latitude.toDouble(), MarkerType.STANDARD, time)
            .also { map.overlays.add(it) }
            .also {
                map.zoomToBoundingBox(
                    BoundingBox.fromGeoPointsSafe(listOf(GeoPoint(latitude.toDouble(), longitude.toDouble()))),
                    true,
                    getScreenHeightPx() / 10
                )
            }
    }
}