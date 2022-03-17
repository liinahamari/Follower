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

package dev.liinahamari.follower.helper

import android.content.Context
import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

sealed class Text {
    companion object {
        fun plain(text: String = "", args: List<Arg> = emptyList()): Text = TextPlain(text, args)
        fun res(@StringRes resId: Int, args: List<Arg> = emptyList()): Text = TextStringResource(resId, args)
        fun pluralRes(@PluralsRes pluralsId: Int, quantity: Int, args: List<Arg> = emptyList()): Text = TextPluralResource(pluralsId, quantity, args)
        fun number(number: Number): Text = TextNumber(number)
    }
}

private data class TextPlain(val text: String, val args: List<Arg>) : Text()
private data class TextStringResource(@StringRes val stringResId: Int, val args: List<Arg>) : Text()
private data class TextPluralResource(@PluralsRes val pluralRes: Int, val quantity: Int, val args: List<Arg>) : Text()
private data class TextNumber(val number: Number) : Text()

sealed class Arg {
    companion object {
        operator fun invoke(text: Text): Arg = ArgText(text)
        fun string(text: String): Arg = Arg(Text.plain(text))
        fun res(@StringRes resId: Int): Arg = Arg(Text.res(resId))
        fun number(number: Number): Arg = ArgNumber(number)
    }
}

private class ArgText(val text: Text) : Arg()
private class ArgNumber(val number: Number) : Arg()

fun Text.asString(context: Context): String = asString(context.resources)

fun Text.asString(resources: Resources): String = when (this) {
    is TextPlain -> args.map {
        it.asString(resources)
    }.let {
        text.format(it.toTypedArray())
    }

    is TextStringResource -> args.map {
        it.asString(resources)
    }.let {
        if (it.isNullOrEmpty()) {
            resources.getString(stringResId)
        } else {
            resources.getString(stringResId, it.toTypedArray())
        }
    }

    is TextNumber -> number.toString()

    is TextPluralResource -> args.map {
        it.asString(resources)
    }.let {
        if (it.isNullOrEmpty()) {
            resources.getQuantityString(pluralRes, quantity)
        } else {
            resources.getQuantityString(pluralRes, quantity, it.toTypedArray())
        }
    }
}

private fun Arg.asString(context: Context): Any = asString(context.resources)
private fun Arg.asString(resources: Resources): Any = when (this) {
    is ArgText -> text.asString(resources)
    is ArgNumber -> number
}