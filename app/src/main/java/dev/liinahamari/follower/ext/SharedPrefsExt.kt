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

package dev.liinahamari.follower.ext

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dev.liinahamari.follower.R

fun Context.getDefaultSharedPreferences(): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

fun SharedPreferences.getBooleanOf(keyToValue: String) = getBoolean(keyToValue, false)
fun SharedPreferences.getStringOf(keyToValue: String) = getString(keyToValue, null)
fun SharedPreferences.getIntOf(keyToValue: String) = getInt(keyToValue, Integer.MIN_VALUE)
fun SharedPreferences.writeBooleanOf(keyToValue: String, value: Boolean) = edit().also { it.putBoolean(keyToValue, value) }.apply()
fun SharedPreferences.writeStringOf(keyToValue: String, value: String) = edit().also { it.putString(keyToValue, value) }.apply()
fun SharedPreferences.writeIntOf(keyToValue: String, value: Int) = edit().also { it.putInt(keyToValue, value) }.apply()

fun SharedPreferences.incrementAppLaunchCounter(context: Context) = edit().also {
    it.putInt(
        context.getString(R.string.pref_app_launcher_counter),
        getInt(context.getString(R.string.pref_app_launcher_counter), 0).inc()
    )
}.apply()

fun Context.trackFistLaunch() = getDefaultSharedPreferences().edit().also {
    it.putBoolean(
        getString(R.string.pref_is_app_first_launched),
        true
    )
}.apply()

/** If nothing stored by key pref_is_app_first_launched, then function returns TRUE */
fun Context.isAppFirstLaunched(): Boolean = getDefaultSharedPreferences().getBoolean(getString(R.string.pref_is_app_first_launched), false).not()