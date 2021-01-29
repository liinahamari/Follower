package com.example.follower.base

import android.content.Context
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.follower.FollowerApp
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

open class BaseFragment(@LayoutRes layout: Int): Fragment(layout) {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    val subscriptions = CompositeDisposable()
    override fun onDestroyView() = super.onDestroyView().also { subscriptions.clear() }

    override fun onAttach(context: Context) = super.onAttach(context).also { (context.applicationContext as FollowerApp).appComponent.inject(this) }
}