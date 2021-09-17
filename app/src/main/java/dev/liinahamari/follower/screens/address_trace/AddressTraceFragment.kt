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

package dev.liinahamari.follower.screens.address_trace

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.BaseFragment
import dev.liinahamari.follower.helper.CustomToast.errorToast
import dev.liinahamari.follower.screens.trace_map.Latitude
import dev.liinahamari.follower.screens.trace_map.Longitude
import io.reactivex.rxjava3.kotlin.addTo
import jp.wasabeef.recyclerview.animators.FadeInAnimator
import kotlinx.android.synthetic.main.fragment_address_trace.*
import kotlinx.android.synthetic.main.fragment_track_list.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

class AddressTraceFragment : BaseFragment(R.layout.fragment_address_trace) {
    private val viewModel by viewModels<AddressTraceViewModel> { viewModelFactory }
    private val adapter = AddressesAdapter(::openMap)

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addressRv.apply {
            layoutManager = LinearLayoutManager(context)
            itemAnimator = FadeInAnimator()
            adapter = this@AddressTraceFragment.adapter
        }

        with(arguments?.getLong(getString(R.string.arg_addressFragment_trackId), -1L)!!) {
            require(this > 0L)
            viewModel.getLogsByTraceId(this)
                .subscribe {
                    adapter.submitData(lifecycle, it)
                }?.addTo(subscriptions)
        }
    }

    override fun setupViewModelSubscriptions() {
        viewModel.errorEvent.observe(viewLifecycleOwner, { errorToast(it) })
    }

    private fun openMap(coordsAndTime: Triple<Longitude, Latitude, ReadableDate>) = NavHostFragment
        .findNavController(this)
        .navigate(
            R.id.action_to_single_pointer_map, bundleOf(
                getString(R.string.arg_toSinglePointerMap_Time) to coordsAndTime.third,
                getString(R.string.arg_toSinglePointerMap_Longitude) to coordsAndTime.first.toFloat(),
                getString(R.string.arg_toSinglePointerMap_Latitude) to coordsAndTime.second.toFloat()
            )
        )
}