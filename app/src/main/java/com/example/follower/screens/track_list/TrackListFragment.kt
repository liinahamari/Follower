package com.example.follower.screens.track_list

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.follower.FollowerApp
import com.example.follower.MainActivity
import com.example.follower.R
import com.example.follower.base.BaseFragment
import com.example.follower.ext.toast
import com.example.follower.screens.map.MapFragment
import kotlinx.android.synthetic.main.fragment_track_list.*
import javax.inject.Inject

class TrackListFragment: BaseFragment(R.layout.fragment_track_list) {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<TrackListViewModel> { viewModelFactory }

    private val tracksAdapter = TrackListAdapter(::removeTrack, ::goToMap)

    private fun removeTrack(id: Long) = viewModel.removeTask(id)
    private fun goToMap(id: Long) = Unit

    override fun onAttach(context: Context) {
        (context.applicationContext as FollowerApp).appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupTrackList()
        setupViewModelSubscriptions()
    }

    private fun setupViewModelSubscriptions() {
        viewModel.errorEvent.observe(viewLifecycleOwner) { errorMessage ->
            toast(getString(errorMessage))
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