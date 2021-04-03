package dev.liinahamari.follower

import dev.liinahamari.follower.ext.isTimeBetweenTwoTimes
import org.junit.Assert.assertFalse
import org.junit.Test

class IsTimeInRangeTest {
    @Test
    fun isTimeBetweenTwoTimeTest() {
        assert(isTimeBetweenTwoTimes("07:00", "17:30", "15:30"))
        assertFalse(isTimeBetweenTwoTimes("17:00", "21:30", "16:30"))
        assert(isTimeBetweenTwoTimes("23:00", "04:00", "02:00"))
        assertFalse(isTimeBetweenTwoTimes("00:30", "06:00", "06:00"))
        assert(isTimeBetweenTwoTimes("00:00", "09:00", "00:00"))
        assert(isTimeBetweenTwoTimes("00:00", "09:00", "08:59"))
        assertFalse(isTimeBetweenTwoTimes("00:00", "09:00", "09:00"))
    }
}
