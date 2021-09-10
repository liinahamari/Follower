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

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import dev.liinahamari.follower.R
import dev.liinahamari.follower.ext.getDefaultSharedPreferences
import dev.liinahamari.follower.ext.getIntOf
import dev.liinahamari.follower.ext.provideUpdatedContextWithNewLocale
import dev.liinahamari.follower.screens.RouteActivity

class IntroActivity : AppIntro() {
    @SuppressLint("MissingSuperCall") /*bug!*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (getDefaultSharedPreferences().getIntOf(getString(R.string.pref_app_launch_counter)) != 1) {
            finish()
            startActivity(Intent(this, RouteActivity::class.java))
        } else {
            addSlide(SlideFragment.newInstance(getString(R.string.title_welcome_to_follower), getString(R.string.summary_welcome), R.color.teal_700, R.drawable.sc_background))
            addSlide(SlideFragment.newInstance(getString(R.string.title_track_observing), getString(R.string.summary_track_observing), R.color.purple_500, R.drawable.sc_address_list)) //todo half-by-half map and addresses view on a picture
            addSlide(SlideFragment.newInstance(getString(R.string.title_try_dark_theme), getString(R.string.summmary_dark_theme), R.color.purple_700, R.drawable.sc_dark_map))
            addSlide(SlideFragment.newInstance(getString(R.string.title_sharing_and_import), getString(R.string.summary_sharing_and_import), R.color.purple_200, R.drawable.sc_share))
            //TODO fingerprint/pin feature
        }
    }

    override fun onSkipPressed(currentFragment: Fragment?) = startActivity(Intent(this, RouteActivity::class.java))
    override fun onDonePressed(currentFragment: Fragment?) = startActivity(Intent(this, RouteActivity::class.java))
    override fun onBackPressed() = Unit // Does nothing, user have to see intro :)
    override fun attachBaseContext(base: Context) = super.attachBaseContext(base.provideUpdatedContextWithNewLocale())
}