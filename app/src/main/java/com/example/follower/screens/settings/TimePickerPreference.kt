package com.example.follower.screens.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference
import com.example.follower.ext.minutesFromMidnightToHourlyTime

private const val DEFAULT_HOUR = 9
private const val DEFAULT_MINUTES_FROM_MIDNIGHT = DEFAULT_HOUR * 60

class TimePickerPreference(context: Context?, attrs: AttributeSet?) : DialogPreference(context, attrs) {
    /** Get saved preference value (in minutes from midnight), so 2 AM is represented as 120 (2*60) here */
    fun getPersistedMinutesFromMidnight(): Int = super.getPersistedInt(DEFAULT_MINUTES_FROM_MIDNIGHT)

    fun persistMinutesFromMidnight(minutesFromMidnight: Int) {
        super.persistInt(minutesFromMidnight)
        notifyChanged()
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        summary = minutesFromMidnightToHourlyTime(getPersistedMinutesFromMidnight())
    }
}