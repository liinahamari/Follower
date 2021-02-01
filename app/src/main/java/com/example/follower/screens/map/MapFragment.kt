package com.example.follower.screens.map

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceManager
import com.example.follower.R
import com.example.follower.base.BaseFragment
import com.example.follower.ext.*
import kotlinx.android.synthetic.main.fragment_map.*
import org.osmdroid.api.IMapController
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.TilesOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider

class MapFragment : BaseFragment(R.layout.fragment_map) {
    private lateinit var mapController: IMapController

    private val viewModel by viewModels<MapFragmentViewModel> { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(arguments?.getLong(getString(R.string.arg_addressFragment_trackId), -1L)!!) {
            require(this > 0L)
            viewModel.getTrack(this)
        }

        with(view.findViewById(R.id.map) as MapView) {
            mapController = controller
            isTilesScaledToDpi = true
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(13.0)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

            overlays.add(CompassOverlay(context, InternalCompassOrientationProvider(context), this).apply {
                enableCompass()
                setCompassCenter(36f, 36f + (0 / requireContext().resources.displayMetrics.density))
            })

            if (PreferenceManager.getDefaultSharedPreferences(requireContext()).getStringOf(getString(R.string.pref_theme))!!.toInt() == MODE_NIGHT_YES) /*todo: to repo*/ {
                overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
            }
        }
    }

    override fun setupViewModelSubscriptions() {
        viewModel.errorEvent.observe(viewLifecycleOwner, { errorToast(it) })
        viewModel.getTrackAsLine.observe(viewLifecycleOwner, {
            map.overlays.add(RoadManager.buildRoadOverlay(it))
            map.invalidate()
            with(it.mRouteHigh.first()) {
                centerMap(latitude, longitude)
            }
        })
        viewModel.getTrackAsMarkerSet.observe(viewLifecycleOwner, {
            it.map { wayPoint -> map.standardMarker(wayPoint.first, wayPoint.second) }
                .apply { map.overlays.addAll(this) }
            with(it.first()) {
                centerMap(second, first)
            }
        })
    }

    fun showLocationErrorToast(locationManager: LocationManager) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            errorToast(getString(R.string.error_location_permission_denied))
        } else if (locationManager.isGpsEnabled().not() && locationManager.isNetworkLocationEnabled().not()) {
            errorToast(getString(R.string.error_location_offline))
        }
    }

    private fun centerMap(lat: Double, long: Double, animated: Boolean = true) {
        with(GeoPoint(lat, long)) {
            if (animated) mapController.animateTo(this) else mapController.setCenter(this)
        }
    }
}