package com.example.follower

import android.content.SharedPreferences
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import com.example.follower.ext.getDefaultSharedPreferences
import com.example.follower.ext.getStringOf
import com.example.follower.ext.writeStringOf
import com.example.follower.helper.FlightRecorder
import com.example.follower.helper.rx.BaseComposers
import com.example.follower.helper.rx.TestSchedulers
import com.example.follower.screens.LocaleChangedResult
import com.example.follower.screens.MainActivitySettingsInteractor
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val RUSSIAN_LOCALE = "ru"

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class InAppLanguageChangeTest {
    private val context = InstrumentationRegistry.getInstrumentation().context
    private val sharedPrefs = context.getDefaultSharedPreferences()
    private val logger = FlightRecorder(createTempFile())
    private val nightModePreferenceInteractor = MainActivitySettingsInteractor(sharedPrefs, BaseComposers(TestSchedulers(), logger), logger, context)
    private val langPrefId = context.getString(R.string.pref_lang)

    @Test
    fun `if something is wrong with SharedPreferences, return LocaleChangedResult # SharedPreferencesCorruptionError`() {
        val sharedPrefMock = Mockito.mock(SharedPreferences::class.java)
        Mockito.`when`(sharedPrefMock.getStringOf(langPrefId)).thenThrow(RuntimeException::class.java)
        val nightModePreferenceInteractor = MainActivitySettingsInteractor(sharedPrefMock, BaseComposers(TestSchedulers(), logger), logger, context)

        nightModePreferenceInteractor.checkLocaleChanged(RUSSIAN_LOCALE)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertResult(LocaleChangedResult.SharedPreferencesCorruptionError)
    }

    @Test
    fun `if the same value persisted, return no result`() {
        sharedPrefs.writeStringOf(langPrefId, RUSSIAN_LOCALE)
        assert(sharedPrefs.getStringOf(langPrefId) == RUSSIAN_LOCALE)

        nightModePreferenceInteractor.checkLocaleChanged(RUSSIAN_LOCALE)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertNoValues()
        assert(sharedPrefs.getStringOf(langPrefId) == RUSSIAN_LOCALE)
    }

    @Test
    fun `if the same value persisted, return no result`() {
        sharedPrefs.writeStringOf(langPrefId, RUSSIAN_LOCALE)
        assert(sharedPrefs.getStringOf(langPrefId) == RUSSIAN_LOCALE)

        nightModePreferenceInteractor.checkLocaleChanged("en")
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertNoValues()
        assert(sharedPrefs.getStringOf(langPrefId) == RUSSIAN_LOCALE)
    }
}