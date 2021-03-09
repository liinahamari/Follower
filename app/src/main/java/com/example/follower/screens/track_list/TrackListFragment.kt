package com.example.follower.screens.track_list

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.SubMenu
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.MenuCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
import com.example.follower.FollowerApp
import com.example.follower.R
import com.example.follower.base.BaseFragment
import com.example.follower.di.modules.BiometricModule
import com.example.follower.ext.throttleFirst
import com.example.follower.helper.CustomToast.errorToast
import com.example.follower.screens.biometric.Authenticator
import com.example.follower.screens.logs.TEXT_TYPE
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_track_list.*
import me.saket.cascade.CascadePopupMenu
import me.saket.cascade.allChildren
import javax.inject.Inject

const val EXT_JSON = ".json"
const val EXT_TXT = ".txt"

class TrackListFragment : BaseFragment(R.layout.fragment_track_list) {
    @Inject lateinit var sharedPreferences: SharedPreferences
    @Inject lateinit var authenticator: Authenticator

    private val viewModel by activityViewModels<TrackListViewModel> { viewModelFactory }
    private val tracksAdapter = TrackListAdapter(::showMenu, ::getTrackDisplayMode)

    private fun getTrackDisplayMode(trackId: Long) = viewModel.getTrackDisplayMode(trackId)
    private fun showMenu(id: Long) = showCascadeMenu(id)

    override fun onAttach(context: Context) = super.onAttach(context).also {
        (context.applicationContext as FollowerApp)
            .appComponent
            .biometricComponent(BiometricModule(
                activity = requireActivity(),
                onSuccessfulAuth = { viewModel.fetchTracks() })
            )
            .inject(this)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTrackList()

        if (sharedPreferences.getBoolean(getString(R.string.pref_enable_biometric_protection), false)) {
            subscriptions += ivLock.clicks()
                .throttleFirst()
                .subscribe { authenticator.authenticate() }
        }
    }

    override fun setupViewModelSubscriptions() {
        viewModel.errorEvent.observe(viewLifecycleOwner) { errorMessage ->
            errorToast(getString(errorMessage))
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

            authenticator.authenticate()
        } else {
            viewModel.fetchTracks()
        }
    }

    private fun showCascadeMenu(trackId: Long) {
        val popupMenu = CascadePopupMenu(requireContext(), requireView())
        popupMenu.menu.apply {
            MenuCompat.setGroupDividerEnabled(this, true)

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
                it.setIcon(R.drawable.ic_share)
                addShareTargets(it.addSubMenu(R.string.as_a_file))
            }
            addSubMenu(getString(R.string.delete)).also {
                it.setIcon(R.drawable.ic_delete)
                it.setHeaderTitle(getString(R.string.are_you_sure))

                it.add(R.string.yes)
                    .setIcon(R.drawable.ic_toast_success)
                    .setOnMenuItemClickListener {
                        viewModel.removeTrack(trackId)
                        true
                    }

                it.add(getString(android.R.string.cancel))
                    .setIcon(R.drawable.ic_close_24)
                    .setOnMenuItemClickListener {
                        popupMenu.navigateBack()
                        true
                    }
            }
        }
        popupMenu.show()
    }
}