package com.example.follower.screens.trace_map

import android.content.Context
import com.example.follower.FollowerApp
import com.example.follower.R
import com.example.follower.base.MapFragment
import com.example.follower.ext.createFollowerMarker
import com.example.follower.helper.CustomToast.errorToast
import kotlinx.android.synthetic.main.fragment_map.*
import org.osmdroid.bonuspack.routing.RoadManager
import javax.inject.Inject

class TraceFragment : MapFragment() {
    @Inject lateinit var viewModel: TraceFragmentViewModel

    override fun onAttach(context: Context) = super.onAttach(context).also {
        (context.applicationContext as FollowerApp)
            .appComponent
            .roadBuildingComponent()
            .inject(this)
    }

    override fun onResume() = super.onResume().also {
        with(arguments?.getLong(getString(R.string.arg_addressFragment_trackId), -9999L)!!) {
            require(this != -9999L)
            viewModel.getTrack(this)
        }
    }

    override fun setupViewModelSubscriptions() {
        viewModel.errorEvent.observe(viewLifecycleOwner, { errorToast(getString(it)) })
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