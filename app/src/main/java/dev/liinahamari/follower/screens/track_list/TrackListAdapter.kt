package dev.liinahamari.follower.screens.track_list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.longClicks
import dev.liinahamari.follower.databinding.ItemTrackBinding
import dev.liinahamari.follower.db.entities.Track
import dev.liinahamari.follower.ext.throttleFirst
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.extensions.LayoutContainer

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Track>() {
    override fun areItemsTheSame(oldItem: Track, newItem: Track) = oldItem.time == newItem.time
    override fun areContentsTheSame(oldItem: Track, newItem: Track) = oldItem == newItem
}

class TrackListAdapter(private val longClickCallback: (id: Long, position: Int) -> Unit, private val clickCallback: (id: Long) -> Unit) : PagedListAdapter<Track, TrackListAdapter.ViewHolder>(DIFF_CALLBACK) {
    private val clicks = CompositeDisposable()

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) = clicks.clear()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(ItemTrackBinding.inflate(LayoutInflater.from(parent.context), parent, false).root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding?.track = getItem(position)
        clicks += holder.itemView.longClicks()
            .throttleFirst()
            .map { getItem(position)?.time }
            .subscribe { longClickCallback.invoke(it!!, position) }
        clicks += holder.itemView.clicks()
            .throttleFirst()
            .map { getItem(position)?.time }
            .subscribe { clickCallback.invoke(it!!) }
    }

    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        val binding: ItemTrackBinding? = DataBindingUtil.bind(containerView)
    }
}