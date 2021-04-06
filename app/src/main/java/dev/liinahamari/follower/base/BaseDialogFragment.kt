package dev.liinahamari.follower.base

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import dev.liinahamari.follower.FollowerApp
import dev.liinahamari.follower.helper.FlightRecorder
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

open class BaseDialogFragment: DialogFragment() {
    @Inject lateinit var logger: FlightRecorder
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    val subscriptions = CompositeDisposable()

    @CallSuper
    override fun onDestroyView() = super.onDestroyView().also { subscriptions.clear() }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupClicks()
    }

    protected open fun setupClicks() = Unit

    override fun onAttach(context: Context) {
        (context.applicationContext as FollowerApp).appComponent.inject(this)
        super.onAttach(context)
    }
}