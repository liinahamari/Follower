package com.example.follower.screens.settings

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TimePicker
import androidx.preference.PreferenceDialogFragmentCompat
import com.example.follower.ext.minutesFromMidnightToHourlyTime

class TimePickerPreferenceDialog : PreferenceDialogFragmentCompat() {
    private lateinit var timepicker: TimePicker

    override fun onCreateDialogView(context: Context?): View = TimePicker(context, null, android.R.style.Widget_Material_TimePicker).also { timepicker = it }

    override fun onBindDialogView(view: View?) {
        super.onBindDialogView(view)
        timepicker.apply {
            val minutesAfterMidnight = (preference as TimePickerPreference).getPersistedMinutesFromMidnight()
            setIs24HourView(true)
            hour = minutesAfterMidnight / 60
            minute = minutesAfterMidnight % 60
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            val minutesAfterMidnight = (timepicker.hour * 60) + timepicker.minute
            (preference as TimePickerPreference).persistMinutesFromMidnight(minutesAfterMidnight)
            preference.summary = minutesFromMidnightToHourlyTime(minutesAfterMidnight)
        }
    }

    companion object {
        fun newInstance(key: String) = TimePickerPreferenceDialog().also {
            it.arguments = Bundle(1).apply {
                putString(ARG_KEY, key)
            }
        }
    }
}