package com.example.follower.ext

import android.content.Context
import android.content.SharedPreferences
import com.example.follower.R

fun SharedPreferences.getBooleanOf(keyToValue: String) = getBoolean(keyToValue, false)
fun SharedPreferences.getStringOf(keyToValue: String) = getString(keyToValue, null)
fun SharedPreferences.getIntOf(keyToValue: String) = getInt(keyToValue, Integer.MIN_VALUE)
fun SharedPreferences.writeBooleanOf(keyToValue: String, value: Boolean) = edit().also { it.putBoolean(keyToValue, value) }.apply()
fun SharedPreferences.writeStringOf(keyToValue: String, value: String) = edit().also { it.putString(keyToValue, value) }.apply()
fun SharedPreferences.writeIntOf(keyToValue: String, value: Int) = edit().also { it.putInt(keyToValue, value) }.apply()

fun SharedPreferences.incrementAppLaunchCounter(context: Context) = edit().also { it.putInt(context.getString(R.string.pref_app_launch_counter), getInt(context.getString(R.string.pref_app_launch_counter), 0).inc()) }.apply()