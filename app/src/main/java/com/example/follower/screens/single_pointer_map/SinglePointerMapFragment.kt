package com.example.follower.screens.single_pointer_map

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.example.follower.BuildConfig
import com.example.follower.R
import com.example.follower.base.BaseFragment
import com.example.follower.ext.createFollowerMarker
import com.example.follower.ext.createStandardMarker
import com.example.follower.ext.getStringOf
import kotlinx.android.synthetic.main.fragment_single_pointer_map.view.*
import org.osmdroid.api.IMapController
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.TilesOverlay

class SinglePointerMapFragment : BaseFragment(R.layout.fragment_single_pointer_map) {
    private lateinit var mapController: IMapController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view.findViewById(R.id.map) as MapView) {
            mapController = controller
            isTilesScaledToDpi = true
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(17.0)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

            if (PreferenceManager.getDefaultSharedPreferences(requireContext()).getStringOf(getString(R.string.pref_theme))!!.toInt() == AppCompatDelegate.MODE_NIGHT_YES) /*todo: to repo*/ {
                overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
            }

            val longitude = arguments?.getFloat(getString(R.string.arg_toSinglePointerMap_Longitude), -999.0f)!!
            val latitude = arguments?.getFloat(getString(R.string.arg_toSinglePointerMap_Latitude), -999.0f)!!
            require(latitude != -999.0f && longitude != -999.0f)
            map.createStandardMarker(longitude.toDouble(), latitude.toDouble())
                .also { map.overlays.add(it) }
                .also { centerMap(latitude.toDouble(), longitude.toDouble()) }
        }
    }

    private fun centerMap(lat: Double, long: Double, animated: Boolean = true) {
        with(GeoPoint(lat, long)) {
            if (animated) mapController.animateTo(this) else mapController.setCenter(this)
        }
    }
}