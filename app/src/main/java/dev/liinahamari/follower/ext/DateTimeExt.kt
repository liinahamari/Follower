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

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/** Represents date and in such format: "year-month-day_of_month 24_format_hours:minutes:seconds.milliseconds"
 *  For example:
 *  2020-12-23 00:12:11:101
 *  */
const val DATE_PATTERN_FOR_LOGGING = "yyyy-MM-dd HH:mm:ss:SSS"

/** Represents hours and minutes in hours:minutes way. Hours will be shown in 24-hour format. For example,
 *  00:12
 *  05:55
 *  22:00
 *  */
const val TIME_PATTERN_HOURS_24_MINUTES = "HH:mm"


/** Represents full-format date (year-month-dayOfMonth-hours-minutes-seconds). Example:
 *  2020-05-21-21-55-00
 *  */
const val PATTERN_SAVING_TIMESTAMP = "yyyy-MM-dd-HH-mm-ss"

fun Long.toReadableDate(): String = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.UK).format(Date(this))

/** String representing current hours in 24 format and minutes, with `:` delimiter. For example:
 * 00:52
 * 08:11
 * */
fun nowHoursAndMinutesOnly(): String = SimpleDateFormat(TIME_PATTERN_HOURS_24_MINUTES, Locale.getDefault()).format(Date())
fun now(): String = SimpleDateFormat(DATE_PATTERN_FOR_LOGGING, Locale.getDefault()).format(Date())

fun minutesFromMidnightToHourlyTime(minutes: Int): String {
    val hour: Int = minutes / 60
    val minute: Int = minutes % 60
    return String.format("%02d:%02d", hour, minute)
}

/** @param time should be presented in 24-hour format and with leading zero in hours section, if it less than 10*/
fun hourlyTimeToMinutesFromMidnight(time: String): Int {
    val hoursAndMinutes = time.split(":")
    return hoursAndMinutes.first().toInt() * 60 + hoursAndMinutes[1].toInt()
}

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@Throws(ParseException::class)
/** all the arguments should be presented in such format: "HH:mm", where HH is 24-hour format hours and mm are minutes */
fun isTimeBetweenTwoTimes(startThreshold: String, endThreshold: String, verifiableTime: String): Boolean {
    val reg = "^([0-1][0-9]|2[0-3]):([0-5][0-9])$"
    if (startThreshold.matches(reg.toRegex()) && endThreshold.matches(reg.toRegex()) && verifiableTime.matches(reg.toRegex())) {
        val start: Calendar = Calendar.getInstance().apply { time = SimpleDateFormat(TIME_PATTERN_HOURS_24_MINUTES, Locale.UK).parse(startThreshold) }

        val end: Calendar = Calendar.getInstance().apply { time = SimpleDateFormat(TIME_PATTERN_HOURS_24_MINUTES, Locale.UK).parse(verifiableTime) }

        val verifiable: Calendar = Calendar.getInstance().apply { time = SimpleDateFormat(TIME_PATTERN_HOURS_24_MINUTES, Locale.UK).parse(endThreshold) }

        if (endThreshold < startThreshold) {
            verifiable.add(Calendar.DATE, 1)
            end.add(Calendar.DATE, 1)
        }
        return (end.time.after(start.time) || end.time.compareTo(start.time) == 0) && end.time.before(verifiable.time)
    } else throw IllegalArgumentException()
}
