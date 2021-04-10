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
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.jakewharton.rxbinding3.view.clicks
import dev.liinahamari.follower.FollowerApp
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.BoundFragment
import dev.liinahamari.follower.di.modules.DIALOG_EMPTY_WAYPOINTS
import dev.liinahamari.follower.di.modules.DIALOG_PERMISSION_EXPLANATION
import dev.liinahamari.follower.di.modules.DIALOG_RATE_MY_APP
import dev.liinahamari.follower.di.modules.TrackingControlModule
import dev.liinahamari.follower.ext.hasAllPermissions
import dev.liinahamari.follower.ext.startService
import dev.liinahamari.follower.ext.throttleFirst
import dev.liinahamari.follower.ext.toReadableDate
import dev.liinahamari.follower.services.location_tracking.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_tracking_control.*
import javax.inject.Inject
import javax.inject.Named

const val PERMISSION_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
@RequiresApi(Build.VERSION_CODES.Q) const val PERMISSION_BACKGROUND_LOCATION = Manifest.permission.ACCESS_BACKGROUND_LOCATION

/*todo, add distance, points*/

@TrackingControlScope
class TrackingControlFragment : BoundFragment(R.layout.fragment_tracking_control) {
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
            startService(LocationTrackingService::class.java, action = ACTION_START_TRACKING)
        } else {
            locationPermissionExplanationDialog.show()
        }
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
            .subscribe { way_points_counter.text = String.format(getString(R.string.title_way_points_collected), it) }
    }

    override fun onAttach(context: Context) {
        (context.applicationContext as FollowerApp)
            .appComponent
            .trackingControlComponent(TrackingControlModule(activity = requireActivity()))
            .inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.checkShowRateMyApp()
    }

    override fun setupViewModelSubscriptions() {
        viewModel.showRateMyAppEvent.observe(this, { rateMyAppDialog.show(childFragmentManager, RateMyAppDialog::class.java.simpleName) })
    }

    override fun setupClicks() {
        subscriptions += btn_start_tracking.clicks()
            .throttleFirst(750L)
            .subscribe {
                val permissions = mutableListOf(PERMISSION_LOCATION)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    permissions.add(PERMISSION_BACKGROUND_LOCATION)
                }
                if (hasAllPermissions(permissions)) {
                    startService(LocationTrackingService::class.java, action = ACTION_START_TRACKING)
                } else {
                    geoPermission.launch(permissions.toTypedArray())
                }
            }

        subscriptions += btn_stop_tracking.clicks()
            .throttleFirst(750L)
            .subscribe {
                if (isServiceBound && gpsService != null) {
                    if (gpsService!!.isTrackEmpty) {
                        emptyWayPointsDialog.show()
                    } else {
                        MaterialDialog(requireContext()).show {
                            cancelable(false)
                            negativeButton(res = R.string.discard) {
                                startService(LocationTrackingService::class.java, action = ACTION_DISCARD_TRACK)
                            }

                            input(prefill = gpsService!!.trackBeginningTime!!.toReadableDate(), hintRes = R.string.hint_name_your_track) { _, text ->

                                startService(
                                    LocationTrackingService::class.java,
                                    action = ACTION_RENAME_TRACK_AND_STOP_TRACKING,
                                    bundle = Bundle().apply {
                                        putCharSequence(ARG_AUTO_SAVE, text)
                                    })
                            }
                        }
                    }
                } else {
                    logger.wtf { "problem with service binding... gpsService == null (${gpsService == null})" }
                    throw RuntimeException()
                }
            }
    }

    /*todo investigate why NPE happens here and why lifecycle fires twice*/
    private fun toggleButtons(isTracking: Boolean) {
        btn_start_tracking?.isEnabled = isTracking.not()
        btn_stop_tracking?.isEnabled = isTracking
        txt_status?.text = getString(if (isTracking) R.string.title_tracking else R.string.title_gps_ready)
        way_points_counter.isVisible = isTracking
    }
}