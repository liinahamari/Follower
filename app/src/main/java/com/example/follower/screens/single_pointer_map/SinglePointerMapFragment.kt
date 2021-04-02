package com.example.follower.screens.single_pointer_map

import android.os.Bundle
import android.view.View
import com.example.follower.R
import com.example.follower.base.MapFragment
import com.example.follower.ext.MarkerType
import com.example.follower.ext.createMarker
import com.example.follower.ext.getScreenHeightPx
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