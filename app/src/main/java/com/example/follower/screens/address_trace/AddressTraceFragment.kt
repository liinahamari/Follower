package com.example.follower.screens.address_trace

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.follower.R
import com.example.follower.base.BaseFragment
import com.example.follower.ext.errorToast
import com.example.follower.screens.map.Latitude
import com.example.follower.screens.map.Longitude
import kotlinx.android.synthetic.main.fragment_address_trace.*
import kotlinx.android.synthetic.main.fragment_track_list.*

class AddressTraceFragment : BaseFragment(R.layout.fragment_address_trace) {
    private val viewModel by viewModels<AddressTraceViewModel> { viewModelFactory }
    private val adapter = AddressesAdapter(::openMap)

    override fun onResume() = super.onResume().also {
        with(arguments?.getLong(getString(R.string.arg_addressFragment_trackId), -1L)!!) {
            require(this > 0L)
            viewModel.getAddressTrace(this)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addressRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AddressTraceFragment.adapter
        }
    }

    override fun setupViewModelSubscriptions() {
        viewModel.loadingEvent.observe(viewLifecycleOwner, { progressBar.isVisible = it })
        viewModel.errorEvent.observe(viewLifecycleOwner, { errorToast(it) })
        viewModel.getAddressesEvent.observe(viewLifecycleOwner, { adapter.addresses = it })
    }

    private fun openMap(coords: Pair<Longitude, Latitude>) = NavHostFragment
        .findNavController(this)
        .navigate(
            R.id.action_to_single_pointer_map, bundleOf(
                getString(R.string.arg_toSinglePointerMap_Longitude) to coords.first.toFloat(),
                getString(R.string.arg_toSinglePointerMap_Latitude) to coords.second.toFloat()
            )
        )
}