package com.example.follower.screens.trace_map

import android.content.Context
import com.example.follower.FollowerApp
import com.example.follower.R
import com.example.follower.base.MapFragment
import com.example.follower.ext.MarkerType
import com.example.follower.ext.createMarker
import com.example.follower.ext.getScreenHeightPx
import com.example.follower.helper.CustomToast.errorToast
import kotlinx.android.synthetic.main.fragment_map.*
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

class TraceFragment : MapFragment() {
    @Inject
    lateinit var viewModel: TraceFragmentViewModel

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
            map.zoomToBoundingBox(BoundingBox.fromGeoPointsSafe(it.mRouteHigh), true, getScreenHeightPx() / 10)
        })

        viewModel.getTrackAsMarkerSet.observe(viewLifecycleOwner, {
            it.subList(1, it.size - 1).map { wayPoint -> map.createMarker(wayPoint.first, wayPoint.second, MarkerType.WAYPOINT) }
                .apply { map.overlays.addAll(this) }

            with(it.first()) {
                map.overlays.add(map.createMarker(first, second, MarkerType.START))
            }
            with(it.last()) {
                map.overlays.add(map.createMarker(first, second, MarkerType.END))
            }

            map.zoomToBoundingBox(BoundingBox.fromGeoPointsSafe(it.map { GeoPoint(it.second, it.first) }), true, getScreenHeightPx() / 10)
        })
    }
}