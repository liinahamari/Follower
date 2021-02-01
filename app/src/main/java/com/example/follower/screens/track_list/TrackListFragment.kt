package com.example.follower.screens.track_list

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
import com.example.follower.R
import com.example.follower.base.BaseFragment
import com.example.follower.ext.errorToast
import kotlinx.android.synthetic.main.fragment_track_list.*

class TrackListFragment : BaseFragment(R.layout.fragment_track_list) {
    private val viewModel by activityViewModels<TrackListViewModel> { viewModelFactory }
    private val tracksAdapter = TrackListAdapter(::removeTrack, ::getTrackDisplayMode)

    private fun getTrackDisplayMode(trackId: Long) = viewModel.getTrackDisplayMode(trackId)
    private fun removeTrack(id: Long) = viewModel.removeTask(id)

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
    }

    override fun setupViewModelSubscriptions() {
        viewModel.errorEvent.observe(viewLifecycleOwner) { errorMessage ->
            errorToast(getString(errorMessage))
        }
        viewModel.fetchAllTracksEvent.observe(viewLifecycleOwner) {
            tracksAdapter.tracks = it.toMutableList()
        }
        viewModel.removeTrackEvent.observe(viewLifecycleOwner) { id ->
            tracksAdapter.removeTask(id)
        }
        viewModel.trackDisplayModeEvent.observe(viewLifecycleOwner) { trackAndDisplayMode ->
            when (trackAndDisplayMode.first) {
                getString(R.string.pref_value_track_display_mode_addresses_list), getString(R.string.pref_value_track_display_mode_map) -> {
                    displayTrackWith(trackAndDisplayMode.first, trackAndDisplayMode.second)
                }
                getString(R.string.pref_value_track_display_mode_none) -> showDialogMapOrAddresses(trackAndDisplayMode.second)
            }
        }
    }

    private fun setupTrackList() = trackList.apply {
        layoutManager = LinearLayoutManager(requireContext())
        adapter = tracksAdapter
    }

    override fun onResume() = super.onResume().also { viewModel.fetchTracks() }
}