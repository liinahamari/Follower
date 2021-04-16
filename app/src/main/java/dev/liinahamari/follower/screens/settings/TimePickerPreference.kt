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
import android.util.AttributeSet
import androidx.preference.DialogPreference
import dev.liinahamari.follower.ext.minutesFromMidnightToHourlyTime

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