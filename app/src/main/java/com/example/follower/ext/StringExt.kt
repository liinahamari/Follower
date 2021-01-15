package com.example.follower.ext

import java.text.SimpleDateFormat
import java.util.*
/** Represents date and in such format: "day_of_month concise_month_name year 24_format_hours:minutes"
 *  For example:
 *  23 Dec 2014 00:12
 *  01 May 2020 05:55
 *  */
const val DATE_PATTERN_FOR_LOGGING = "dd EEE MMM yyyy HH:mm"

fun Long.toReadableDate(): String = SimpleDateFormat(DATE_PATTERN_FOR_LOGGING).format(Date(this))
