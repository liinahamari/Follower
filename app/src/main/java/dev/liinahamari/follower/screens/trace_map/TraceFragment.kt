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
import android.text.InputType.TYPE_CLASS_NUMBER
import by.kirich1409.viewbindingdelegate.viewBinding
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.MapFragment
import dev.liinahamari.follower.databinding.FragmentMapBinding
import dev.liinahamari.follower.ext.MarkerType
import dev.liinahamari.follower.ext.appComponent
import dev.liinahamari.follower.ext.createMarker
import dev.liinahamari.follower.ext.getScreenHeightPx
import dev.liinahamari.follower.ext.round
import dev.liinahamari.follower.helper.CustomToast.errorToast
import dev.liinahamari.follower.screens.track_list.TrackListFragment
import org.osmdroid.util.BoundingBox
import javax.inject.Inject

class TraceFragment : MapFragment() {
    private val ui by viewBinding(FragmentMapBinding::bind)

    @Inject lateinit var viewModel: TraceFragmentViewModel

    override fun onAttach(context: Context) {
        appComponent
            ?.roadBuildingComponent()
            ?.inject(this)
        super.onAttach(context)
    }

    override fun onResume() = super.onResume().also {
        with(requireArguments().getString(getString(R.string.arg_traceQuantityMode), null)!!) {
            when (this) {
                TrackListFragment.ShowTraceQuantityMode.SINGLE_TRACE.toString() -> {
                    with(arguments?.getLong(getString(R.string.arg_addressFragment_trackId), -9999L)!!) {
                        require(this != -9999L)
                        viewModel.getTrack(this)
                    }
                }

                TrackListFragment.ShowTraceQuantityMode.ALL_TRACES.toString() -> viewModel.getAllTracks()
                else -> throw IllegalStateException()
            }
        }
    }

    override fun setupViewModelSubscriptions() {
        viewModel.errorEvent.observe(viewLifecycleOwner) { errorToast(it) }

        viewModel.updateTrackLengthEvent.observe(viewLifecycleOwner) { ui.distanceTv.text = String.format(getString(R.string.distance_x_km), it) }

        viewModel.getTrackAsLineEvent.observe(viewLifecycleOwner) {
            ui.distanceTv.text = String.format(getString(R.string.distance_x_km), it.length.round(2))
            ui.map.overlays.add(it.road)

            if (it.startPoint != it.finishPoint) {
                markTrackStartAndFinishAndZoom(it.boundingBox, it.startPoint, it.finishPoint)
            }
            if (it.length == 0.0) {
                MaterialDialog(requireContext()) //todo double validator
                    .negativeButton(res = android.R.string.cancel)
                    .cancelable(false)
                    .input(inputType = TYPE_CLASS_NUMBER, hintRes = R.string.hint_name_your_track) { _, text ->
                        viewModel.updateTrackLength(text, it.trackId)
                    }
                    .show()
            }
        }
        viewModel.getAllTrackAsLineEvent.observe(viewLifecycleOwner) {
            ui.distanceTv.text = String.format(getString(R.string.distance_x_km), it.length)
            ui.map.overlays.addAll(it.roads)
            ui.map.invalidate()
            ui.map.zoomToBoundingBox(it.boundingBox, true, getScreenHeightPx() / 10)
        }

        viewModel.getTrackAsMarkerSet.observe(viewLifecycleOwner) {
            it.wayPoints.map { wp -> ui.map.createMarker(wp.lon, wp.lat, MarkerType.WAYPOINT, wp.readableTimeStamp) }
                .apply { ui.map.overlays.addAll(this) }

            if (it.startPoint != it.finishPoint) {
                markTrackStartAndFinishAndZoom(it.boundingBox, it.startPoint, it.finishPoint)
            }
        }
    }

    private fun markTrackStartAndFinishAndZoom(boundingBox: BoundingBox, startPoint: WayPointUi, finishPointUi: WayPointUi) {
        with(ui.map) {
            overlays.add(createMarker(startPoint.lon, startPoint.lat, MarkerType.START, startPoint.readableTimeStamp))

            overlays.add(createMarker(finishPointUi.lon, finishPointUi.lat, MarkerType.END, finishPointUi.readableTimeStamp))

            invalidate()
            zoomToBoundingBox(boundingBox, true, getScreenHeightPx() / 10)
        }
    }
}
