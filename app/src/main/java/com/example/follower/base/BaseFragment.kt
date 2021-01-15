package com.example.follower.base

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import io.reactivex.disposables.CompositeDisposable

open class BaseFragment(@LayoutRes layout: Int): Fragment(layout) {
    val subscriptions = CompositeDisposable()
    override fun onDestroyView() = super.onDestroyView().also { subscriptions.clear() }
}