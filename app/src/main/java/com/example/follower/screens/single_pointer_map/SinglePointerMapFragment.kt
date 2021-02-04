package com.example.follower.screens.single_pointer_map

import android.os.Bundle
import android.view.View
import com.example.follower.R
import com.example.follower.base.MapFragment
import com.example.follower.ext.createStandardMarker
import kotlinx.android.synthetic.main.fragment_map.*

class SinglePointerMapFragment : MapFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val longitude = arguments?.getFloat(getString(R.string.arg_toSinglePointerMap_Longitude), -999.0f)!!
        val latitude = arguments?.getFloat(getString(R.string.arg_toSinglePointerMap_Latitude), -999.0f)!!
        require(latitude != -999.0f && longitude != -999.0f)
        map.createStandardMarker(longitude.toDouble(), latitude.toDouble())
            .also { map.overlays.add(it) }
            .also { centerMap(latitude.toDouble(), longitude.toDouble()) }
    }
}