package dev.liinahamari.follower.base

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dev.liinahamari.follower.FollowerApp
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

open class BaseFragment(@LayoutRes layout: Int): Fragment(layout) {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    val subscriptions = CompositeDisposable()
    override fun onDestroyView() = super.onDestroyView().also { subscriptions.clear() }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViewModelSubscriptions()
        setupClicks()
    }

    protected open fun setupViewModelSubscriptions() = Unit
    protected open fun setupClicks() = Unit

    override fun onAttach(context: Context) {
        (context.applicationContext as FollowerApp).appComponent.inject(this)
        super.onAttach(context)
    }
}