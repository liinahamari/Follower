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

package dev.liinahamari.follower.screens.settings

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TimePicker
import androidx.preference.PreferenceDialogFragmentCompat
import dev.liinahamari.follower.ext.minutesFromMidnightToHourlyTime

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