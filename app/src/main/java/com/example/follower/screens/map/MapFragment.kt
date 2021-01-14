package com.example.follower.screens.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.location.LocationManager.NETWORK_PROVIDER
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.follower.R
import com.example.follower.ext.getStatusBarHeight
import com.example.follower.ext.isGpsEnabled
import com.example.follower.ext.isNetworkLocationEnabled
import com.example.follower.ext.toast
import kotlinx.android.synthetic.main.fragment_map.view.*
import org.osmdroid.api.IMapController
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider

class MapFragment: Fragment() {
    private lateinit var mapController: IMapController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val layout = inflater.inflate(R.layout.fragment_map, container, false)
        with(layout.findViewById(R.id.map) as MapView) {
            mapController = controller
            isTilesScaledToDpi = true
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(9.5)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

            overlays.add(CompassOverlay(context, InternalCompassOrientationProvider(context), this).apply {
                enableCompass()
                setCompassCenter(36f, 36f + (0 / requireContext().resources.displayMetrics.density))
            })
        }
        centerMap(Location(NETWORK_PROVIDER)) //todo: permission
        return layout.rootView
    }

    fun showLocationErrorToast(locationManager: LocationManager) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            toast(getString(R.string.error_location_permission_denied))
        } else if (locationManager.isGpsEnabled().not() && locationManager.isNetworkLocationEnabled().not()) {
            toast(getString(R.string.error_location_offline))
        }
    }

    private fun centerMap(location: Location, animated: Boolean = true) {
        val position = GeoPoint(59.436962, 24.753574)
//        val position = GeoPoint(location.latitude, location.longitude)
        if (animated) mapController.animateTo(position) else mapController.setCenter(position)
    }
}

