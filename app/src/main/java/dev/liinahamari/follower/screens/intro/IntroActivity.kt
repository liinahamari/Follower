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

package dev.liinahamari.follower.screens.intro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro2
import com.github.appintro.AppIntroFragment
import com.github.appintro.AppIntroPageTransformerType
import dev.liinahamari.follower.R
import dev.liinahamari.follower.ext.isAppFirstLaunched
import dev.liinahamari.follower.ext.provideUpdatedContextWithNewLocale
import dev.liinahamari.follower.ext.trackFistLaunch
import dev.liinahamari.follower.screens.RouteActivity

class IntroActivity : AppIntro2() {
    private val slideFragments: List<AppIntroFragment> by lazy {
        listOf(
            AppIntroFragment.createInstance(
                title = getString(R.string.title_welcome_to_follower),
                description = getString(R.string.summary_welcome),
                imageDrawable = R.drawable.sc_background,
                titleColorRes = R.color.white,
                descriptionColorRes = R.color.white,
                backgroundColorRes = R.color.teal_700
            ),
            AppIntroFragment.createInstance( //todo half-by-half map and addresses view on a picture
                title = getString(R.string.title_track_observing),
                description = getString(R.string.summary_track_observing),
                imageDrawable = R.drawable.sc_address_list,
                titleColorRes = R.color.white,
                descriptionColorRes = R.color.white,
                backgroundColorRes = R.color.purple_500
            ),
            AppIntroFragment.createInstance(
                title = getString(R.string.title_try_dark_theme),
                description = getString(R.string.summary_dark_theme),
                imageDrawable = R.drawable.sc_dark_map,
                titleColorRes = R.color.white,
                descriptionColorRes = R.color.white,
                backgroundColorRes = R.color.purple_700
            ),
            AppIntroFragment.createInstance(
                title = getString(R.string.title_sharing_and_import),
                description = getString(R.string.summary_sharing_and_import),
                imageDrawable = R.drawable.sc_share,
                titleColorRes = R.color.white,
                descriptionColorRes = R.color.white,
                backgroundColorRes = R.color.purple_200
            )
        )
        //TODO fingerprint/pin feature
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isAppFirstLaunched()) {
            trackFistLaunch()
            setupIntroScreenConfig()
            slideFragments.forEach(::addSlide)
        } else {
            finish()
            startActivity(Intent(this, RouteActivity::class.java))
        }
    }

    private fun setupIntroScreenConfig() {
        setImmersiveMode()
        isSystemBackButtonLocked = true
        setTransformer(AppIntroPageTransformerType.Flow)
        isColorTransitionsEnabled = true
    }

    override fun onSkipPressed(currentFragment: Fragment?) = finish().also {
        startActivity(Intent(this, RouteActivity::class.java))
    }

    override fun onDonePressed(currentFragment: Fragment?) = finish().also {
        startActivity(Intent(this, RouteActivity::class.java))
    }

    override fun attachBaseContext(base: Context) = super.attachBaseContext(base.provideUpdatedContextWithNewLocale())
}