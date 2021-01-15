package com.example.follower.ext

import java.text.SimpleDateFormat
import java.util.*
/** Represents date and in such format: "day_of_month concise_month_name year 24_format_hours:minutes"
 *  For example:
 *  23 Dec 2014 00:12
 *  01 May 2020 05:55
 *  */
const val DATE_PATTERN_FOR_LOGGING = "dd EEE MMM yyyy HH:mm"

/** Represents hours and minutes in hours:minutes way. Hours will be shown in 24-hour format. For example,
 *  00:12
 *  05:55
 *  22:00
 *  */
const val TIME_PATTERN_HOURS_24_MINUTES = "HH:mm"

fun Long.toReadableDate(): String = SimpleDateFormat(DATE_PATTERN_FOR_LOGGING).format(Date(this))

/** String representing current hours in 24 format and minutes, with `:` delimiter. For example:
 * 00:52
 * 08:11
 * */
fun now(): String = SimpleDateFormat(TIME_PATTERN_HOURS_24_MINUTES, Locale.getDefault()).format(Date())
