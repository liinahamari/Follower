package com.example.follower.base

import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable

open class BaseActivity: AppCompatActivity() {
    protected val subscriptions = CompositeDisposable()
    override fun onDestroy() = super.onDestroy().also { subscriptions.clear() }
}