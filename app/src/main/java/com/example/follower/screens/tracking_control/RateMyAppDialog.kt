package com.example.follower.screens.tracking_control

import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.follower.R
import com.example.follower.ext.writeBooleanOf

class RateMyAppDialog(private val sharedPreferences: SharedPreferences) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = AlertDialog.Builder(requireContext())
        .setIcon(R.drawable.ic_baseline_star_rate_24)
        .setNeutralButton(R.string.title_dont_ask_againg) { _, _ -> sharedPreferences.writeBooleanOf(getString(R.string.pref_never_show_rate_app), true) }
        .setTitle(R.string.title_rate_our_app)
        .setPositiveButton(R.string.yes) { _, _ ->
            val appPackageName: String = requireActivity().packageName
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
            } catch (anfe: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
            }
        }
        .setNegativeButton(R.string.no, null)
        .create()
}