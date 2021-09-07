/*
Copyright 2020-2021 liinahamari

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package dev.liinahamari.follower.screens.trace_map

import android.content.Context
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.MapFragment
import dev.liinahamari.follower.ext.MarkerType
import dev.liinahamari.follower.ext.appComponent
import dev.liinahamari.follower.ext.createMarker
import dev.liinahamari.follower.ext.getScreenHeightPx
import dev.liinahamari.follower.helper.CustomToast.errorToast
import kotlinx.android.synthetic.main.fragment_map.*
import org.osmdroid.util.BoundingBox
import javax.inject.Inject

class TraceFragment : MapFragment() {
    @Inject lateinit var viewModel: TraceFragmentViewModel

    override fun onAttach(context: Context) {
        appComponent
            ?.roadBuildingComponent()
            ?.inject(this)
        super.onAttach(context) }

    override fun onResume() = super.onResume().also {
        with(arguments?.getLong(getString(R.string.arg_addressFragment_trackId), -9999L)!!) {
            require(this != -9999L)
            viewModel.getTrack(this)
        }
    }

    override fun setupViewModelSubscriptions() {
        viewModel.errorEvent.observe(viewLifecycleOwner, { errorToast(it) })

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