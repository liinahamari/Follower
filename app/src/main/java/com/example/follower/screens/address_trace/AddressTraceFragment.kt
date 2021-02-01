package com.example.follower.screens.address_trace

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.example.follower.R
import com.example.follower.base.BaseFragment
import com.example.follower.ext.errorToast
import kotlinx.android.synthetic.main.fragment_address_trace.*

class AddressTraceFragment : BaseFragment(R.layout.fragment_address_trace) {
    private val viewModel by viewModels<AddressTraceViewModel> { viewModelFactory }

    override fun onResume() = super.onResume().also {
        with(arguments?.getLong(getString(R.string.arg_addressFragment_trackId), -1L)!!) {
            require(this > 0L)
            viewModel.getAddressTrace(this)
        }
    }

    override fun setupViewModelSubscriptions(){
        viewModel.loadingEvent.observe(viewLifecycleOwner, { progressBar.isVisible = it })
        viewModel.errorEvent.observe(viewLifecycleOwner, { errorToast(it) })
        viewModel.getTrackEvent.observe(viewLifecycleOwner, { logsContainer.text = it.joinToString(separator = "\n") })
    }
}