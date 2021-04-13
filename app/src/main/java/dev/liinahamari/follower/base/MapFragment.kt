package dev.liinahamari.follower.base

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import dev.liinahamari.follower.R
import dev.liinahamari.follower.helper.SingleLiveEvent
import dev.liinahamari.follower.model.PreferenceRepository
import io.reactivex.rxjava3.kotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_map.*
import org.osmdroid.api.IMapController
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.TilesOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import javax.inject.Inject

open class MapFragment : BaseFragment(R.layout.fragment_map) {
    private val viewModel: MapFragmentViewModel by viewModels { viewModelFactory }
    private lateinit var mapController: IMapController
    private lateinit var tilesOverlay: TilesOverlay

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view.findViewById(R.id.map) as MapView) {
            mapController = controller
            isTilesScaledToDpi = true
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT)
            tilesOverlay = overlayManager.tilesOverlay

            map.overlays.add(CompassOverlay(context, InternalCompassOrientationProvider(context), this).apply {
                enableCompass()
                setCompassCenter(36f, 36f + (0 / requireContext().resources.displayMetrics.density))
            })
        }
        viewModel.isMapColorsNeedToBeInverted()
    }

    override fun setupViewModelSubscriptions() {
        viewModel.invertMapColorsEvent.observe(viewLifecycleOwner, {
            tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
        })
    }

    protected fun centerMap(lat: Double, long: Double, animated: Boolean = true) = with(GeoPoint(lat, long)) {
        if (animated) mapController.animateTo(this) else mapController.setCenter(this)
    }

    class MapFragmentViewModel @Inject constructor(private val preferenceRepository: PreferenceRepository) : BaseViewModel() {
        private val _invertMapColorsEvent = SingleLiveEvent<Any>()
        val invertMapColorsEvent: LiveData<Any> get() = _invertMapColorsEvent

        fun isMapColorsNeedToBeInverted() {
            disposable += preferenceRepository.isDarkThemeEnabled.subscribe {
                if (it) {
                    _invertMapColorsEvent.call()
                }
            }
        }
    }
}