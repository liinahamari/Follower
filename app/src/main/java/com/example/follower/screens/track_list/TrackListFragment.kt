package com.example.follower.screens.track_list

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.follower.R
import com.example.follower.base.BaseFragment
import com.example.follower.ext.errorToast
import kotlinx.android.synthetic.main.fragment_track_list.*

class TrackListFragment: BaseFragment(R.layout.fragment_track_list) {
    private val viewModel by activityViewModels<TrackListViewModel> { viewModelFactory }

    private val tracksAdapter = TrackListAdapter(::removeTrack, ::goToMap)

    private fun removeTrack(id: Long) = viewModel.removeTask(id)
    private fun goToMap(id: Long) = NavHostFragment.findNavController(this).navigate(R.id.action_to_map, bundleOf(getString(R.string.arg_addressFragment_trackId) to id))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupTrackList()
        setupViewModelSubscriptions()
    }

    private fun setupViewModelSubscriptions() {
        viewModel.errorEvent.observe(viewLifecycleOwner) { errorMessage ->
            errorToast(getString(errorMessage))
        }
        viewModel.fetchAllTracksEvent.observe(viewLifecycleOwner) {
            tracksAdapter.tracks = it.toMutableList()
        }
        viewModel.removeTrackEvent.observe(viewLifecycleOwner) { id ->
            tracksAdapter.removeTask(id)
        }
    }

    private fun setupTrackList() {
        trackList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tracksAdapter
        }
    }

    override fun onResume() = super.onResume().also { viewModel.fetchTracks() }
}