package com.example.follower.ext

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/** Represents date and in such format: "day_of_month concise_month_name 24_format_hours:minutes"
 *  For example:
 *  23 Dec 00:12
 *  01 May 05:55
 *  */
private const val DATE_PATTERN_FOR_LOGGING = "dd EEE MMM HH:mm"

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
fun now(): String = SimpleDateFormat(TIME_PATTERN_HOURS_24_MINUTES, Locale.getDefault()).format(Date())
fun today(): String = SimpleDateFormat(DATE_PATTERN_FOR_LOGGING, Locale.getDefault()).format(Date())

fun Date.toSavingTimeStamp(): String = SimpleDateFormat(PATTERN_SAVING_TIMESTAMP, Locale.US).format(date)

fun minutesFromMidnightToHourlyTime(minutes: Int): String {
    val hour: Int = minutes / 60
    val minute: Int = minutes % 60
    return String.format("%02d:%02d", hour, minute)
}
