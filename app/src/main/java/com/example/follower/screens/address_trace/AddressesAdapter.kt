package com.example.follower.screens.address_trace

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.follower.databinding.ItemAddressBinding
import com.example.follower.ext.throttleFirst
import com.example.follower.screens.trace_map.Latitude
import com.example.follower.screens.trace_map.Longitude
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.extensions.LayoutContainer

class AddressesAdapter constructor(private val mapCallback: (coordinates: Pair<Longitude, Latitude>) -> Unit) : RecyclerView.Adapter<AddressesAdapter.ViewHolder>() {
    private val clicks = CompositeDisposable()
    var addresses: List<MapPointer> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) = clicks.clear()
    override fun getItemCount() = addresses.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(ItemAddressBinding.inflate(LayoutInflater.from(parent.context), parent, false).root)

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        holder.binding = null
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding?.address = addresses[position]
        clicks += holder.itemView.clicks()
            .throttleFirst()
            .map { addresses[position].lon to addresses[position].lat }
            .subscribe { mapCallback.invoke(it) }
    }

    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        var binding: ItemAddressBinding? = DataBindingUtil.bind(containerView)
    }
}