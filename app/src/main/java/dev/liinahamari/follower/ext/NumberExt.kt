package dev.liinahamari.follower.ext

import java.math.BigDecimal
import java.math.RoundingMode

fun Double.round(places: Int) = BigDecimal.valueOf(this).setScale(places, RoundingMode.HALF_UP).toDouble()