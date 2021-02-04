package com.example.follower.screens.trace_map

import androidx.fragment.app.viewModels
import com.example.follower.R
import com.example.follower.base.MapFragment
import com.example.follower.ext.createFollowerMarker
import com.example.follower.ext.errorToast
import kotlinx.android.synthetic.main.fragment_map.*
import org.osmdroid.bonuspack.routing.RoadManager

class TraceFragment : MapFragment() {
    private val viewModel by viewModels<TraceFragmentViewModel> { viewModelFactory }

    override fun onResume() = super.onResume().also {
        with(arguments?.getLong(getString(R.string.arg_addressFragment_trackId), -9999L)!!) {
            require(this != -9999L)
            viewModel.getTrack(this)
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
            it.map { wayPoint -> map.createFollowerMarker(wayPoint.first, wayPoint.second) }
                .apply { map.overlays.addAll(this) }
            with(it.first()) {
                centerMap(second, first)
            }
        })
    }
}