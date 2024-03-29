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

import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import com.jakewharton.rxbinding4.view.clicks
import com.jakewharton.rxbinding4.view.longClicks
import dev.liinahamari.follower.databinding.ItemTrackBinding
import dev.liinahamari.follower.ext.adaptToNightModeState
import dev.liinahamari.follower.ext.throttleFirst
import dev.liinahamari.follower.helper.delegates.DifferDelegateAdapter
import dev.liinahamari.follower.helper.delegates.Entity
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo

fun trackListAdapter(
    longClickCallback: (id: Long) -> Unit,
    clickCallback: (id: Long) -> Unit,
    disposable: CompositeDisposable
): AdapterDelegate<List<Entity<*>>> = adapterDelegateViewBinding<TrackUi, Entity<*>, ItemTrackBinding>(
    viewBinding = { layoutInflater, root -> ItemTrackBinding.inflate(layoutInflater, root, false) },
    block = {
        bind {
            with(binding) {
                trackDescriptionTv.text = item.title

                if (item.isImported) {
                    context.adaptToNightModeState(importedIv.drawable)
                }

                itemView.longClicks()
                    .throttleFirst()
                    .map { item.id }
                    .subscribe(longClickCallback::invoke)
                    .addTo(disposable)

                itemView.clicks()
                    .throttleFirst()
                    .map { item.id }
                    .subscribe(clickCallback::invoke)
                    .addTo(disposable)
            }
        }
    }
)

internal class TracksDelegateAdapter(
    longClickCallback: (id: Long) -> Unit,
    clickCallback: (id: Long) -> Unit,
    disposable: CompositeDisposable
) : DifferDelegateAdapter<Entity<*>>(
    trackListAdapter(longClickCallback, clickCallback, disposable)
)