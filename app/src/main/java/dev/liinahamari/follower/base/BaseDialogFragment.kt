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

package dev.liinahamari.follower.base

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import dev.liinahamari.follower.FollowerApp
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject

open class BaseDialogFragment: DialogFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    val subscriptions = CompositeDisposable()

    @CallSuper
    override fun onDestroyView() = super.onDestroyView().also { subscriptions.clear() }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupClicks()
        setupViewModelSubscriptions()
    }

    protected open fun setupViewModelSubscriptions() = Unit
    protected open fun setupClicks() = Unit

    override fun onAttach(context: Context) {
        (context.applicationContext as FollowerApp).appComponent.inject(this)
        super.onAttach(context)
    }
}