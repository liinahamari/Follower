package com.example.follower.screens.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.example.follower.BuildConfig
import com.example.follower.FollowerApp
import com.example.follower.R
import com.example.follower.ext.*
import com.example.follower.helper.FlightRecorder
import kotlinx.android.synthetic.main.fragment_map.*
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.TilesOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import javax.inject.Inject

const val EXTRA_TRACK_ID = "MapFragment.track_id"

class MapFragment : Fragment() {
    @Inject
    lateinit var logger: FlightRecorder
    private lateinit var mapController: IMapController

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<MapFragmentViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {
        (requireActivity().application as FollowerApp).appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val layout = inflater.inflate(R.layout.fragment_map, container, false)
        with(layout.findViewById(R.id.map) as MapView) {
            Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
            Configuration.getInstance().osmdroidBasePath = requireActivity().getExternalFilesDir(null)

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
        return layout.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        with(requireActivity().intent.getLongExtra(EXTRA_TRACK_ID, -1L)) {
        with(1L) {
            require(this > 0)
            viewModel.getTrack(this)
        }
        setupViewModelSubscriptions()
    }

    private fun setupViewModelSubscriptions() {
        viewModel.errorEvent.observe(viewLifecycleOwner, { toast(it) })
        viewModel.getTrackEvent.observe(viewLifecycleOwner, {
            it.map { wayPoint -> map.standardMarker(wayPoint.first, wayPoint.second) }
                .apply { map.overlays.addAll(this) }
        })
    }

    fun showLocationErrorToast(locationManager: LocationManager) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            toast(getString(R.string.error_location_permission_denied))
        } else if (locationManager.isGpsEnabled().not() && locationManager.isNetworkLocationEnabled().not()) {
            toast(getString(R.string.error_location_offline))
        }
    }

    private fun centerMap(lat: Double, long: Double, animated: Boolean = true) {
        val position = GeoPoint(lat, long)
        if (animated) mapController.animateTo(position) else mapController.setCenter(position)
    }
}