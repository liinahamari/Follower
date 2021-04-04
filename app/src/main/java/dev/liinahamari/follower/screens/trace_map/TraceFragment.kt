package dev.liinahamari.follower.screens.trace_map

import android.content.Context
import dev.liinahamari.follower.FollowerApp
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.MapFragment
import dev.liinahamari.follower.ext.MarkerType
import dev.liinahamari.follower.ext.createMarker
import dev.liinahamari.follower.ext.getScreenHeightPx
import dev.liinahamari.follower.helper.CustomToast.errorToast
import kotlinx.android.synthetic.main.fragment_map.*
import org.osmdroid.util.BoundingBox
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

        viewModel.getTrackAsLineEvent.observe(viewLifecycleOwner, {
            map.overlays.add(it.road)

            if (it.startPoint != it.finishPoint) {
                markTrackStartAndFinishAndZoom(it.boundingBox, it.startPoint, it.finishPoint)
            }
        })

        viewModel.getTrackAsMarkerSet.observe(viewLifecycleOwner, {
            it.wayPoints.map { wp -> map.createMarker(wp.lon, wp.lat, MarkerType.WAYPOINT, wp.readableTimeStamp) }
                .apply { map.overlays.addAll(this) }

            if (it.startPoint != it.finishPoint) {
                markTrackStartAndFinishAndZoom(it.boundingBox, it.startPoint, it.finishPoint)
            }
        })
    }

    private fun markTrackStartAndFinishAndZoom(boundingBox: BoundingBox, startPoint: WayPointUi, finishPointUi: WayPointUi) {
        map.overlays.add(map.createMarker(startPoint.lon, startPoint.lat, MarkerType.START, startPoint.readableTimeStamp))

        map.overlays.add(map.createMarker(finishPointUi.lon, finishPointUi.lat, MarkerType.END, finishPointUi.readableTimeStamp))

        map.invalidate()
        map.zoomToBoundingBox(boundingBox, true, getScreenHeightPx() / 10)
    }
}