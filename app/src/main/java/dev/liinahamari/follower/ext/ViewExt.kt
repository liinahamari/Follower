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

/*
Copyright 2020-2021 liinahamari

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package dev.liinahamari.follower.ext

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import dev.liinahamari.follower.R

fun Context.getStatusBarHeight(): Int = resources.getIdentifier("status_bar_height", "dimen", "android")

fun Context.dpToPx(dp: Int) = (dp * resources.displayMetrics.density).toInt()

fun Context.pxToDp(px: Int) = (px / resources.displayMetrics.density).toInt()

fun Context.isDarkModeEnabled(): Boolean = when (PreferenceManager.getDefaultSharedPreferences(this).getStringOf(getString(R.string.pref_theme))!!.toInt()) {
    AppCompatDelegate.MODE_NIGHT_NO -> false
    AppCompatDelegate.MODE_NIGHT_YES -> true
    else -> deviceHasDarkThemeEnabled()
}

fun Fragment.isDarkModeEnabled(): Boolean = when (PreferenceManager.getDefaultSharedPreferences(requireContext()).getStringOf(getString(R.string.pref_theme))!!.toInt()) {
    AppCompatDelegate.MODE_NIGHT_NO -> false
    AppCompatDelegate.MODE_NIGHT_YES -> true
    else -> deviceHasDarkThemeEnabled()
}

fun Context.adaptToNightModeState(drawable: Drawable?): Drawable? = drawable?.also {
    DrawableCompat.setTint(DrawableCompat.wrap(drawable), if (isDarkModeEnabled()) Color.WHITE else Color.BLACK)
}

fun Context.deviceHasDarkThemeEnabled() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
fun Fragment.deviceHasDarkThemeEnabled() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
fun Activity.deviceHasDarkThemeEnabled() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

fun Drawable.changeColor(@ColorInt color: Int): Drawable = this
    .apply { DrawableCompat.setTint(DrawableCompat.wrap(this), color) }

@Suppress("DEPRECATION")
fun Activity.getScreenHeightPx(): Int = DisplayMetrics().apply { windowManager.defaultDisplay.getMetrics(this) }.heightPixels
@Suppress("DEPRECATION")
fun Fragment.getScreenHeightPx(): Int = DisplayMetrics().apply { requireActivity().windowManager.defaultDisplay.getMetrics(this) }.heightPixels

@Suppress("DEPRECATION")
fun Activity.getScreenWidthPx(): Int = DisplayMetrics().apply { windowManager.defaultDisplay.getMetrics(this) }.widthPixels
@Suppress("DEPRECATION")
fun Fragment.getScreenWidthPx(): Int = DisplayMetrics().apply { requireActivity().windowManager.defaultDisplay.getMetrics(this) }.widthPixels