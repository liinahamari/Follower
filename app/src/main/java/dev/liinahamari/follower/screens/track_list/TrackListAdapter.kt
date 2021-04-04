package dev.liinahamari.follower.screens.track_list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import dev.liinahamari.follower.ext.throttleFirst
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.longClicks
import dev.liinahamari.follower.databinding.ItemTrackBinding
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.extensions.LayoutContainer

class TrackListAdapter(private val longClickCallback: (id: Long) -> Unit, private val clickCallback: (id: Long) -> Unit) : RecyclerView.Adapter<TrackListAdapter.ViewHolder>() {
    private val clicks = CompositeDisposable()
    var tracks: MutableList<TrackUi> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun removeTask(id: Long) = tracks.remove(tracks.first { it.id == id }).also { notifyDataSetChanged() }
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) = clicks.clear()
    override fun getItemCount() = tracks.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(ItemTrackBinding.inflate(LayoutInflater.from(parent.context), parent, false).root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding?.track = tracks[position]
        clicks += holder.itemView.longClicks()
            .throttleFirst()
            .map { tracks[position].id }
            .subscribe { longClickCallback.invoke(it) }
        clicks += holder.itemView.clicks()
            .throttleFirst()
            .map { tracks[position].id }
            .subscribe { clickCallback.invoke(it) }
    }

    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        val binding: ItemTrackBinding? = DataBindingUtil.bind(containerView)
    }
}
