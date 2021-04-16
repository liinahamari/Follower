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

@file:Suppress("DEPRECATION")

package dev.liinahamari.follower.helper

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import dev.liinahamari.follower.R

private const val DEFAULT_TEXT_SIZE = 16f /*sp*/
private const val DEFAULT_TOAST_DURATION = LENGTH_LONG
private const val DEFAULT_TEXT_COLOR = R.color.white

object CustomToast {
    fun Context.errorToast(@StringRes message: Int) = custom(this, getString(message), R.drawable.ic_toast_error, resources.getColor(R.color.errorColor)).show()
    fun Context.infoToast(@StringRes message: Int) = custom(this, getString(message), R.drawable.ic_toast_info, resources.getColor(R.color.teal_200)).show()
    fun Context.successToast(@StringRes message: Int) = custom(this, getString(message), R.drawable.ic_toast_success, resources.getColor(R.color.successColor)).show()

    fun Context.errorToast(message: String) = custom(this, message, R.drawable.ic_toast_error, resources.getColor(R.color.errorColor)).show()
    fun Context.infoToast(message: String) = custom(this, message, R.drawable.ic_toast_info, resources.getColor(R.color.teal_200)).show()
    fun Context.successToast(message: String) = custom(this, message, R.drawable.ic_toast_success, resources.getColor(R.color.successColor)).show()

    fun Fragment.errorToast(message: String) = custom(requireActivity(), message, R.drawable.ic_toast_error, resources.getColor(R.color.errorColor)).show()
    fun Fragment.infoToast(message: String) = custom(requireActivity(), message, R.drawable.ic_toast_info, resources.getColor(R.color.teal_200)).show()
    fun Fragment.successToast(message: String) = custom(requireActivity(), message, R.drawable.ic_toast_success, resources.getColor(R.color.successColor)).show()

    fun Fragment.errorToast(@StringRes message: Int) = custom(requireActivity(), getString(message), R.drawable.ic_toast_error, resources.getColor(R.color.errorColor)).show()
    fun Fragment.infoToast(@StringRes message: Int) = custom(requireActivity(), getString(message), R.drawable.ic_toast_info, resources.getColor(R.color.teal_200)).show()
    fun Fragment.successToast(@StringRes message: Int) = custom(requireActivity(), getString(message), R.drawable.ic_toast_success, resources.getColor(R.color.successColor)).show()

    @SuppressLint("ShowToast", "InflateParams")
    fun custom(context: Context, message: String, @DrawableRes icon: Int, @ColorInt tintColor: Int): Toast = Toast.makeText(context, "", DEFAULT_TOAST_DURATION).apply {
        val toastLayout: View = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.toast_layout, null)

        toastLayout.findViewById<ImageView>(R.id.toastIcon)
            .background = AppCompatResources.getDrawable(context, icon)!!.apply { setColorFilter(context.resources.getColor(DEFAULT_TEXT_COLOR), PorterDuff.Mode.SRC_IN) }

        toastLayout.findViewById<TextView>(R.id.toastText).apply {
            text = message
            setTextColor(context.resources.getColor(DEFAULT_TEXT_COLOR))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE)
        }

        toastLayout.findViewById<CardView>(R.id.root)
            .setCardBackgroundColor(tintColor)

        view = toastLayout
    }
}