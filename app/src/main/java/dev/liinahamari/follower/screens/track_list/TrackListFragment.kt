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

package dev.liinahamari.follower.screens.track_list

import android.app.Service
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.SubMenu
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.MenuCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
import com.jakewharton.rxbinding4.view.clicks
import dagger.Lazy
import dev.liinahamari.follower.FollowerApp
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.BoundFragment
import dev.liinahamari.follower.di.modules.Authenticator
import dev.liinahamari.follower.di.modules.BiometricModule
import dev.liinahamari.follower.di.scopes.BiometricScope
import dev.liinahamari.follower.ext.adaptToNightModeState
import dev.liinahamari.follower.ext.throttleFirst
import dev.liinahamari.follower.helper.CustomToast.errorToast
import dev.liinahamari.follower.screens.logs.TEXT_TYPE
import dev.liinahamari.follower.services.location_tracking.LocationTrackingService
import io.reactivex.rxjava3.kotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_track_list.*
import me.saket.cascade.CascadePopupMenu
import javax.inject.Inject

const val EXT_JSON = ".json"
private const val EXT_TXT = ".txt"
private const val FTP = "ftp"

@BiometricScope
class TrackListFragment : BoundFragment(R.layout.fragment_track_list), SharedPreferences.OnSharedPreferenceChangeListener {
    @Inject lateinit var sharedPreferences: SharedPreferences
    @Inject lateinit var authenticator: Lazy<Authenticator>

    private var gpsService: LocationTrackingService? = null

    private val viewModel by activityViewModels<TrackListViewModel> { viewModelFactory }
    private val tracksAdapter = TrackListAdapter(::showMenu, ::getTrackDisplayMode)

    private val pickFiles = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        viewModel.importTracks(it, gpsService!!.isTracking.value!!)
    }

    private fun getTrackDisplayMode(trackId: Long) = viewModel.getTrackDisplayMode(trackId)
    private fun showMenu(id: Long) = showCascadeMenu(id)

    override fun onAttach(context: Context) = super.onAttach(context).also {
        (context.applicationContext as FollowerApp)
            .appComponent
            .biometricComponent(
                BiometricModule(
                    activity = requireActivity(),
                    onSuccessfulAuth = { viewModel.fetchTracks(isServiceBound && gpsService?.isTracking?.value == true) })
            )
            .inject(this)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    private fun showDialogMapOrAddresses(trackId: Long) {
        MaterialDialog(requireContext()).show {
            var rememberChoice = false

            cancelable(false)
            checkBoxPrompt(res = R.string.title_remember_choice) { isChecked ->
                rememberChoice = isChecked
            }
            positiveButton(R.string.title_show_map) {
                with(getString(R.string.pref_value_track_display_mode_map)) {
                    if (rememberChoice) viewModel.saveDisplayType(this)
                    displayTrackWith(this, trackId)
                }
            }
            negativeButton(R.string.title_show_addresses) {
                with(getString(R.string.pref_value_track_display_mode_addresses_list)) {
                    if (rememberChoice) viewModel.saveDisplayType(this)
                    displayTrackWith(this, trackId)
                }
            }
        }
    }

    private fun displayTrackWith(mode: String, trackId: Long) {
        val action: Int = when (mode) {
            getString(R.string.pref_value_track_display_mode_map) -> R.id.action_to_map
            getString(R.string.pref_value_track_display_mode_addresses_list) -> R.id.action_to_addresses_list
            else -> throw IllegalStateException()
        }
        NavHostFragment.findNavController(this@TrackListFragment)
            .navigate(action, bundleOf(getString(R.string.arg_addressFragment_trackId) to trackId))
    }

    override fun getBindingTarget(): Class<out Service> = LocationTrackingService::class.java

    override fun onServiceConnected(binder: IBinder) {
        gpsService = (binder as LocationTrackingService.LocationServiceBinder).getService()
    }

    override fun onServiceDisconnected() {
        gpsService = null
    }

    override fun onDetach() {
        super.onDetach()
        gpsService = null
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun setupClicks() {
        subscriptions += importFab.clicks()
            .throttleFirst()
            .subscribe {
                pickFiles.launch(arrayOf("*/*")) //todo investigate how to filter by extension
            }

        if (sharedPreferences.getBoolean(getString(R.string.pref_enable_biometric_protection), false)) {
            subscriptions += ivLock.clicks()
                .throttleFirst()
                .subscribe { authenticator.get().authenticate() }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTrackList()
    }

    override fun setupViewModelSubscriptions() {
        viewModel.errorEvent.observe(viewLifecycleOwner) { errorMessage ->
            errorToast(errorMessage)
        }
        viewModel.emptyTrackListEvent.observe(viewLifecycleOwner) {
            emptyListTv.isVisible = true
            trackList.isVisible = false
            ivLock.isVisible = false
        }
        viewModel.nonEmptyTrackListEvent.observe(viewLifecycleOwner) {
            emptyListTv.isVisible = false
            ivLock.isVisible = false
            trackList.isVisible = true

            tracksAdapter.tracks = it.toMutableList()
        }
        viewModel.removeTrackEvent.observe(viewLifecycleOwner) { id ->
            tracksAdapter.removeTask(id)
            if (tracksAdapter.tracks.isEmpty()) {
                emptyListTv.isVisible = true
                trackList.isVisible = false
            }
        }
        viewModel.trackDisplayModeEvent.observe(viewLifecycleOwner) { trackAndDisplayMode ->
            when (trackAndDisplayMode.first) {
                getString(R.string.pref_value_track_display_mode_addresses_list), getString(R.string.pref_value_track_display_mode_map) -> {
                    displayTrackWith(trackAndDisplayMode.first, trackAndDisplayMode.second)
                }
                getString(R.string.pref_value_track_display_mode_none) -> showDialogMapOrAddresses(trackAndDisplayMode.second)
            }
        }
        viewModel.shareJsonEvent.observe(viewLifecycleOwner) { trackJsonAndName ->
            Intent(Intent.ACTION_SEND).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                type = TEXT_TYPE
                putExtra(Intent.EXTRA_SUBJECT, String.format(getString(R.string.title_sharing_track), trackJsonAndName.second))
                putExtra(Intent.EXTRA_STREAM, trackJsonAndName.first)
            }.also { startActivity(it) }
        }
    }

    private fun setupTrackList() {
        ivLock.isVisible = false
        trackList.isVisible = true

        trackList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tracksAdapter
        }
    }

    override fun onResume() = super.onResume().also {
        if (sharedPreferences.getBoolean(getString(R.string.pref_enable_biometric_protection), false)) {
            ivLock.isVisible = true
            trackList.isVisible = false
            emptyListTv.isVisible = false

            authenticator.get().authenticate()
        } else {
            viewModel.fetchTracks(isServiceBound && gpsService?.isTracking?.value == true)
        }
    }

    private fun showCascadeMenu(trackId: Long) {
        val popupMenu = CascadePopupMenu(requireContext(), requireView())
        popupMenu.menu.apply {
            MenuCompat.setGroupDividerEnabled(this, true)

            addSubMenu(getString(R.string.title_track_representing)).also {
                    it.add(getString(R.string.title_addresses_list))
                        .setOnMenuItemClickListener {
                            displayTrackWith(getString(R.string.pref_value_track_display_mode_addresses_list), trackId)
                            true
                        }
                    it.add(getString(R.string.title_map))
                        .setOnMenuItemClickListener {
                            displayTrackWith(getString(R.string.pref_value_track_display_mode_map), trackId)
                            true
                        }
                it.setIcon(R.drawable.ic_baseline_map_24)
            }
            addSubMenu(getString(R.string.share)).also {
                val addShareTargets = { sub: SubMenu ->
                    sub.add(EXT_JSON)
                        .setOnMenuItemClickListener {
                            viewModel.createSharedJsonFileForTrack(trackId, EXT_JSON)
                            true
                        }
                    sub.add(EXT_TXT)
                        .setOnMenuItemClickListener {
                            viewModel.createSharedJsonFileForTrack(trackId, EXT_TXT)
                            true
                        }
                }

                it.add(FTP).setOnMenuItemClickListener {
                    FtpSharingFragment()
                        .apply { arguments = bundleOf(ARG_TRACK_ID to trackId) }
                        .show(childFragmentManager, FtpSharingFragment::class.java.simpleName)
                    true
                }

                it.setIcon(requireContext().adaptToNightModeState(ResourcesCompat.getDrawable(resources, R.drawable.ic_share, null)))
                addShareTargets(it.addSubMenu(R.string.as_a_file))
            }
            addSubMenu(getString(R.string.delete)).also {
                it.setIcon(requireContext().adaptToNightModeState(ResourcesCompat.getDrawable(resources, R.drawable.ic_delete, null)))
                it.setHeaderTitle(getString(R.string.are_you_sure))

                it.add(R.string.yes)
                    .setIcon(requireContext().adaptToNightModeState(ResourcesCompat.getDrawable(resources, R.drawable.ic_toast_success, null)))
                    .setOnMenuItemClickListener {
                        viewModel.removeTrack(trackId)
                        true
                    }

                it.add(getString(android.R.string.cancel))
                    .setIcon(requireContext().adaptToNightModeState(ResourcesCompat.getDrawable(resources, R.drawable.ic_close_24, null)))
                    .setOnMenuItemClickListener {
                        popupMenu.navigateBack()
                        true
                    }
            }
        }
        popupMenu.show()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == getString(R.string.pref_theme)) {
            tracksAdapter.notifyDataSetChanged()
        }
    }
}