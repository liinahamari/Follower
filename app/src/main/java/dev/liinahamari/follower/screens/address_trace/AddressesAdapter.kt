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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding4.view.clicks
import dev.liinahamari.follower.databinding.ItemAddressBinding
import dev.liinahamari.follower.ext.throttleFirst
import dev.liinahamari.follower.screens.trace_map.Latitude
import dev.liinahamari.follower.screens.trace_map.Longitude
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import kotlinx.android.extensions.LayoutContainer

class AddressesAdapter constructor(private val mapCallback: (coordinates: Pair<Longitude, Latitude>, time: String) -> Unit) : RecyclerView.Adapter<AddressesAdapter.ViewHolder>() {
    private val clicks = CompositeDisposable()
    var addresses: List<MapPointer> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) = clicks.clear()
    override fun getItemCount() = addresses.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(ItemAddressBinding.inflate(LayoutInflater.from(parent.context), parent, false).root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding?.address = addresses[position]
        clicks += holder.itemView.clicks()
            .throttleFirst()
            .subscribe { mapCallback.invoke(addresses[position].lon to addresses[position].lat, addresses[position].time) }
    }

    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        var binding: ItemAddressBinding? = DataBindingUtil.bind(containerView)
    }
}