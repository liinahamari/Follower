package com.example.follower.ext

import android.content.SharedPreferences

fun SharedPreferences.getBooleanOf(keyToValue: String) = getBoolean(keyToValue, false)
fun SharedPreferences.getStringOf(keyToValue: String) = getString(keyToValue, null)
fun SharedPreferences.writeBooleanOf(keyToValue: String, value: Boolean) = edit().also { it.putBoolean(keyToValue, value) }.apply()
fun SharedPreferences.writeStringOf(keyToValue: String, value: String) = edit().also { it.putString(keyToValue, value) }.apply()
fun SharedPreferences.writeIntOf(keyToValue: String, value: Int) = edit().also { it.putInt(keyToValue, value) }.apply()