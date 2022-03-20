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

package dev.liinahamari.follower.screens.tracking_control

import android.Manifest
import android.app.Service
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.jakewharton.rxbinding4.view.clicks
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.BoundFragment
import dev.liinahamari.follower.databinding.FragmentTrackingControlBinding
import dev.liinahamari.follower.di.modules.DIALOG_EMPTY_WAYPOINTS
import dev.liinahamari.follower.di.modules.DIALOG_PERMISSION_EXPLANATION
import dev.liinahamari.follower.di.modules.DIALOG_RATE_MY_APP
import dev.liinahamari.follower.di.modules.TrackingControlModule
import dev.liinahamari.follower.ext.*
import dev.liinahamari.follower.helper.delegates.RxSubscriptionDelegateImpl
import dev.liinahamari.follower.helper.delegates.RxSubscriptionsDelegate
import dev.liinahamari.follower.model.TrackMode
import dev.liinahamari.follower.services.location_tracking.*
import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import javax.inject.Inject
import javax.inject.Named

const val PERMISSION_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
@RequiresApi(Build.VERSION_CODES.Q) const val PERMISSION_BACKGROUND_LOCATION = Manifest.permission.ACCESS_BACKGROUND_LOCATION

/*todo, add distance, points*/

@TrackingControlScope
class TrackingControlFragment :
    BoundFragment(R.layout.fragment_tracking_control),
    RxSubscriptionsDelegate by RxSubscriptionDelegateImpl()
{
    private val ui by viewBinding(FragmentTrackingControlBinding::bind)

    private val viewModel by viewModels<TrackingControlViewModel> { viewModelFactory }
    private val boundServiceDisposables = CompositeDisposable()

    @Named(DIALOG_PERMISSION_EXPLANATION)
    @Inject lateinit var locationPermissionExplanationDialog: AlertDialog

    @Named(DIALOG_EMPTY_WAYPOINTS)
    @Inject lateinit var emptyWayPointsDialog: AlertDialog

    @Named(DIALOG_RATE_MY_APP)
    @Inject lateinit var rateMyAppDialog: DialogFragment

    private var gpsService: LocationTrackingService? = null

    private val geoPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (it.values.all { accepted -> accepted }) {
            startForegroundService(LocationTrackingService::class.java, action = ACTION_START_TRACKING)
        } else {
            locationPermissionExplanationDialog.show()
        }
    }

    override fun onDestroy() {
        disposeSubscriptions()
        super.onDestroy()
    }

    override fun getBindingTarget(): Class<out Service> = LocationTrackingService::class.java

    override fun onServiceDisconnected() {
        gpsService = null
        toggleButtons(false)
    }

    override fun onDetach() {
        super.onDetach()
        gpsService = null
        boundServiceDisposables.clear()
    }

    override fun onServiceConnected(binder: IBinder) {
        boundServiceDisposables.clear()
        gpsService = (binder as LocationTrackingService.LocationServiceBinder).getService()

        boundServiceDisposables += gpsService!!
            .isTracking
            .subscribe { toggleButtons(it) }

        boundServiceDisposables += gpsService!!
            .wayPointsCounter
            .subscribe { ui.wayPointsCounter.text = String.format(getString(R.string.title_way_points_collected), it) }
    }

    override fun onAttach(context: Context) {
        appComponent
            ?.trackingControlComponent(TrackingControlModule(activity = requireActivity()))
            ?.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.checkShowRateMyApp()
    }

    override fun setupViewModelSubscriptions() {
        viewModel.showRateMyAppEvent.observe(this) { rateMyAppDialog.show(childFragmentManager, RateMyAppDialog::class.java.simpleName) }
    }

    override fun setupClicks() {
        setupTrackMode()
        ui.btnStartTracking.clicks()
            .throttleFirst()
            .addToDisposable {
                val permissions = mutableListOf(PERMISSION_LOCATION)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    permissions.add(PERMISSION_BACKGROUND_LOCATION)
                }
                if (hasAllPermissions(permissions)) {
                    val trackMode: TrackMode =
                        ui.trackModeCarIv.takeIf { it.isSelected }?.let { TrackMode.CAR }
                            ?: ui.trackModeBikeIv.takeIf { it.isSelected }?.let { TrackMode.BIKE }
                            ?: ui.trackModeWalkIv.takeIf { it.isSelected }!!.let { TrackMode.WALK }

                    startForegroundService(
                        LocationTrackingService::class.java,
                        action = ACTION_START_TRACKING,
                        bundle = bundleOf(ARG_TRACK_MODE to trackMode)
                    )
                } else {
                    geoPermission.launch(permissions.toTypedArray())
                }
            }

        ui.btnStopTracking.clicks()
            .throttleFirst()
            .addToDisposable {
                if (isServiceBound && gpsService != null) {
                    if (gpsService!!.isTrackEmpty) {
                        emptyWayPointsDialog.show()
                    } else {
                        MaterialDialog(requireContext()).show {
                            cancelable(false)
                            negativeButton(res = R.string.discard) {
                                startForegroundService(LocationTrackingService::class.java, action = ACTION_DISCARD_TRACK)
                            }

                            input(prefill = gpsService!!.trackBeginningTime!!.toReadableDate(), hintRes = R.string.hint_name_your_track) { _, text ->

                                startForegroundService(
                                    LocationTrackingService::class.java,
                                    action = ACTION_RENAME_TRACK_AND_STOP_TRACKING,
                                    bundle = Bundle().apply {
                                        putCharSequence(ARG_AUTO_SAVE, text)
                                    })
                            }
                        }
                    }
                } else {
                    FlightRecorder.w { "problem with service binding... gpsService == null (${gpsService == null})" }
                    throw RuntimeException()
                }
            }
    }

    private fun setupTrackMode() {
        ui.trackModeCarIv.isSelected = true

        ui.trackModeCarIv.clicks()
            .throttleFirst()
            .addToDisposable {
                ui.trackModeCarIv.isSelected = true
                ui.trackModeBikeIv.isSelected = false
                ui.trackModeWalkIv.isSelected = false
            }

        ui.trackModeWalkIv.clicks()
            .throttleFirst()
            .addToDisposable {
                ui.trackModeCarIv.isSelected = false
                ui.trackModeBikeIv.isSelected = false
                ui.trackModeWalkIv.isSelected = true
            }

        ui.trackModeBikeIv.clicks()
            .throttleFirst()
            .addToDisposable {
                ui.trackModeCarIv.isSelected = false
                ui.trackModeBikeIv.isSelected = true
                ui.trackModeWalkIv.isSelected = false
            }
    }

    /*todo investigate why NPE happens here and why lifecycle fires twice*/
    private fun toggleButtons(isTracking: Boolean) {
        ui.btnStartTracking.isEnabled = isTracking.not()
        ui.btnStopTracking.isEnabled = isTracking
        ui.txtStatus.text = getString(if (isTracking) R.string.title_tracking else R.string.title_gps_ready)
        ui.wayPointsCounter.isVisible = isTracking
    }
}