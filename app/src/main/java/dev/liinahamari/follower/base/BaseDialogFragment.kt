package dev.liinahamari.follower.base

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.fragment.app.DialogFragment
import io.reactivex.disposables.CompositeDisposable

open class BaseDialogFragment: DialogFragment() {
    val subscriptions = CompositeDisposable()

    @CallSuper
    override fun onDestroyView() = super.onDestroyView().also { subscriptions.clear() }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupClicks()
    }

    protected open fun setupClicks() = Unit

}