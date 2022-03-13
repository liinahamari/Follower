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

package dev.liinahamari.follower.screens.track_list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding4.view.clicks
import com.jakewharton.rxbinding4.view.longClicks
import dev.liinahamari.follower.R
import dev.liinahamari.follower.ext.adaptToNightModeState
import dev.liinahamari.follower.ext.throttleFirst
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

class TrackListAdapter(
    private val longClickCallback: (id: Long) -> Unit,
    private val clickCallback: (id: Long) -> Unit
) : RecyclerView.Adapter<TrackListAdapter.ViewHolder>() {
    private val clicks = CompositeDisposable()
    var tracks: MutableList<TrackUi> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun removeTask(id: Long) = tracks.remove(tracks.first { it.id == id }).also { notifyDataSetChanged() }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) = clicks.clear()
    override fun getItemCount() = tracks.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.trackDescription.text = tracks[position].title
        if (tracks[position].isImported) {
            with(holder.importedIv) {
                context.adaptToNightModeState(drawable)
            }
        }
        clicks += holder.itemView.longClicks()
            .throttleFirst()
            .map { tracks[position].id }
            .subscribe { longClickCallback.invoke(it) }
        clicks += holder.itemView.clicks()
            .throttleFirst()
            .map { tracks[position].id }
            .subscribe { clickCallback.invoke(it) }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val trackDescription: TextView = view.findViewById(R.id.trackDescriptionTv)
        val importedIv: ImageView = view.findViewById(R.id.importedIv)
    }
}
