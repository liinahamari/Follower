package com.example.follower.base

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.example.follower.R
import com.example.follower.ext.getStringOf
import kotlinx.android.synthetic.main.fragment_map.*
import org.osmdroid.api.IMapController
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.TilesOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider

open class MapFragment : BaseFragment(R.layout.fragment_map) {
    private lateinit var mapController: IMapController

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view.findViewById(R.id.map) as MapView) {
            mapController = controller
            isTilesScaledToDpi = true
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(17.0)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

            map.overlays.add(CompassOverlay(context, InternalCompassOrientationProvider(context), this).apply {
                enableCompass()
                setCompassCenter(36f, 36f + (0 / requireContext().resources.displayMetrics.density))
            })

            if (PreferenceManager.getDefaultSharedPreferences(requireContext()).getStringOf(getString(R.string.pref_theme))!!.toInt() == AppCompatDelegate.MODE_NIGHT_YES) {
                overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
            }
        }
    }

    protected fun centerMap(lat: Double, long: Double, animated: Boolean = true) = with(GeoPoint(lat, long)) {
        if (animated) mapController.animateTo(this) else mapController.setCenter(this)
    }
}
