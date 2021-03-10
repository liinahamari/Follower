package com.example.follower

import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.test.platform.app.InstrumentationRegistry
import com.example.follower.ext.getDefaultSharedPreferences
import com.example.follower.ext.getStringOf
import com.example.follower.ext.writeStringOf
import com.example.follower.helper.FlightRecorder
import com.example.follower.helper.rx.BaseComposers
import com.example.follower.helper.rx.TestSchedulers
import com.example.follower.screens.DarkThemeInteractor
import com.example.follower.screens.NightModeChangesResult
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class DarkThemeApplyingTest {
    private val context = InstrumentationRegistry.getInstrumentation().context
    private val themePrefId = context.getString(R.string.pref_theme)
    private val sharedPrefs = context.getDefaultSharedPreferences()
    private val logger = FlightRecorder(createTempFile())
    private val nightModePreferenceInteractor = DarkThemeInteractor(sharedPrefs, BaseComposers(TestSchedulers(), logger), logger, context)

    @Test
    fun `if no value persisted, write default -- MODE_NIGHT_FOLLOW_SYSTEM -- and recreate`() {
        assert(sharedPrefs.getStringOf(themePrefId) == null)

        nightModePreferenceInteractor.handleThemeChanges(MODE_NIGHT_YES)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertResult(NightModeChangesResult.Success(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM))
        assert(sharedPrefs.getStringOf(themePrefId) == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString())
    }

    @Test
    fun `NightModeValue -- if the same value persisted, return no result`() {
        sharedPrefs.writeStringOf(themePrefId, MODE_NIGHT_YES.toString())
        assert(sharedPrefs.getStringOf(themePrefId)!!.toInt() == MODE_NIGHT_YES)

        nightModePreferenceInteractor.handleThemeChanges(MODE_NIGHT_YES)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertNoValues()
        assert(sharedPrefs.getStringOf(themePrefId) == MODE_NIGHT_YES.toString())
    }

    @Test
    fun `if night mode status changes in system, then reflect those changes in the app`() {
        sharedPrefs.writeStringOf(themePrefId, MODE_NIGHT_YES.toString())
        assert(sharedPrefs.getStringOf(themePrefId)!!.toInt() == MODE_NIGHT_YES)

        nightModePreferenceInteractor.handleThemeChanges(MODE_NIGHT_NO)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValue(NightModeChangesResult.Success(MODE_NIGHT_NO))
        assert(sharedPrefs.getStringOf(themePrefId) == MODE_NIGHT_NO.toString())
    }

    @Test
    fun `if something is wrong with SharedPreferences, return NightModePreferencesResult # SharedPreferencesCorruptionError`() {
        val sharedPrefMock = Mockito.mock(SharedPreferences::class.java)
        Mockito.`when`(sharedPrefMock.getStringOf(themePrefId)).thenThrow(RuntimeException::class.java)
        val nightModePreferenceInteractor = DarkThemeInteractor(sharedPrefMock, BaseComposers(TestSchedulers(), logger), logger, context)

        nightModePreferenceInteractor.handleThemeChanges(MODE_NIGHT_YES).test()
            .assertNoErrors()
            .assertComplete()
            .assertResult(NightModeChangesResult.SharedChangesCorruptionError)
    }
}