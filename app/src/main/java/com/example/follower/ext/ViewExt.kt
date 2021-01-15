package com.example.follower.ext

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import es.dmoral.toasty.Toasty

fun Context.toast(message: String) = Toasty.error(this, message, Toast.LENGTH_LONG, true).show()
fun Fragment.toast(message: String) = context?.toast(message)
fun Activity.toast(message: String) = applicationContext.toast(message)

fun Context.getStatusBarHeight(): Int = resources.getIdentifier("status_bar_height", "dimen", "android")
