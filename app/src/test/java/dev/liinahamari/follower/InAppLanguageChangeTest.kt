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

package dev.liinahamari.follower

import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import dev.liinahamari.follower.ext.getDefaultSharedPreferences
import dev.liinahamari.follower.ext.getLocalesLanguage
import dev.liinahamari.follower.ext.getStringOf
import dev.liinahamari.follower.ext.provideUpdatedContextWithNewLocale
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val LANGUAGE_ID_ENGLISH = "en"
private const val LANGUAGE_ID_RUSSIAN = "ru"
private const val LANGUAGE_ID_UNSUPPORTED = "de"

/* TODO: ideas for end-to-end tests:
*    First install, supported language presented on a device and translations in the app by default corresponding device language
*      First install, unsupported language presented on a device - application presented in English language
*        In-app language changed via settings and this language persisting throughout reboots and app relaunches (but not reinstalling!)
*          Device-wide language change (via system settings) cause no effect on app-wide language chosen (even if it was initial load by default - see p.1) whether it's supported or not.
* */

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class InAppLanguageChangeTest {
    private var context = InstrumentationRegistry.getInstrumentation().context
    private var sharedPrefs = context.getDefaultSharedPreferences()
    private val langPrefId = context.getString(R.string.pref_lang)

    @Before
    fun setUp() {
        sharedPrefs.edit().putString(langPrefId, null).commit()
    }

    @Test
    fun `applying EN locale on Context`() {
        assertEquals(sharedPrefs.getString(langPrefId, null), null)
        sharedPrefs.edit().putString(langPrefId, LANGUAGE_ID_ENGLISH).commit()
        assertEquals(sharedPrefs.getString(langPrefId, null), LANGUAGE_ID_ENGLISH)

        val context = context.provideUpdatedContextWithNewLocale()
        assertEquals(LANGUAGE_ID_ENGLISH, context.resources.configuration.getLocalesLanguage())
        assertEquals(LANGUAGE_ID_ENGLISH, sharedPrefs.getStringOf(langPrefId))
    }

    @Test
    fun `applying RU locale on Context`() {
        assertEquals(sharedPrefs.getString(langPrefId, null), null)
        sharedPrefs.edit().putString(langPrefId, LANGUAGE_ID_RUSSIAN).commit()
        assertEquals(sharedPrefs.getString(langPrefId, null), LANGUAGE_ID_RUSSIAN)

        val context = context.provideUpdatedContextWithNewLocale(LANGUAGE_ID_RUSSIAN, null)
        assertEquals(LANGUAGE_ID_RUSSIAN, context.resources.configuration.getLocalesLanguage())
        assertEquals(LANGUAGE_ID_RUSSIAN, sharedPrefs.getStringOf(langPrefId))
    }

    @Test
    fun `first launch - EN language chosen on device`() {
        val context = context.provideUpdatedContextWithNewLocale(null, LANGUAGE_ID_ENGLISH)
        assertEquals(LANGUAGE_ID_ENGLISH, context.resources.configuration.getLocalesLanguage())
        assertEquals(LANGUAGE_ID_ENGLISH, sharedPrefs.getStringOf(context.getString(R.string.pref_lang)))
    }

    @Test
    fun `first launch - unsupported language chosen on device`() {
        val context = context.provideUpdatedContextWithNewLocale(null, LANGUAGE_ID_UNSUPPORTED)
        assertEquals(LANGUAGE_ID_ENGLISH, context.resources.configuration.getLocalesLanguage())
        assertEquals(LANGUAGE_ID_ENGLISH, sharedPrefs.getStringOf(context.getString(R.string.pref_lang)))
    }

    @Test
    fun `first launch - RU language (supported) chosen on device`() {
        val context = context.provideUpdatedContextWithNewLocale(null, LANGUAGE_ID_RUSSIAN)
        assertEquals(LANGUAGE_ID_RUSSIAN, context.resources.configuration.getLocalesLanguage())
        assertEquals(LANGUAGE_ID_RUSSIAN, sharedPrefs.getStringOf(context.getString(R.string.pref_lang)))
    }
}