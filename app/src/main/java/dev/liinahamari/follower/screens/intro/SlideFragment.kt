/*
 * Copyright 2020-2021 liinahamari
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.liinahamari.follower.screens.intro

import android.os.Bundle
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import dev.liinahamari.follower.R
import dev.liinahamari.follower.databinding.FragmentGenericSlideBinding

const val ARG_TITLE = "dev.liinahamari.follower.screens.intro.ARG_TITLE"
const val ARG_DESCRIPTION = "dev.liinahamari.follower.screens.intro.ARG_DESCRIPTION"
const val ARG_IMAGE = "dev.liinahamari.follower.screens.intro.ARG_IMAGE"
const val ARG_BACKGROUND_COLOR = "dev.liinahamari.follower.screens.intro.ARG_BACKGROUND_COLOR"

class SlideFragment : Fragment(R.layout.fragment_generic_slide) {
    private val ui by viewBinding(FragmentGenericSlideBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.introRoot.setBackgroundResource(requireArguments().getInt(ARG_BACKGROUND_COLOR))
        ui.introTitle.text = requireArguments().getString(ARG_TITLE)
        ui.introImage.setImageResource(requireArguments().getInt(ARG_IMAGE))
        ui.introDescription.text = requireArguments().getString(ARG_DESCRIPTION)
    }

    companion object {
        fun newInstance(title: String, description: String, @ColorRes backgroundColor: Int, @DrawableRes image: Int) = SlideFragment()
            .apply {
                arguments = bundleOf(ARG_TITLE to title, ARG_DESCRIPTION to description, ARG_BACKGROUND_COLOR to backgroundColor, ARG_IMAGE to image)
            }
    }
}