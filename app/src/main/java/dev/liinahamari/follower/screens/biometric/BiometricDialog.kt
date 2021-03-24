package dev.liinahamari.follower.screens.biometric

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import com.google.android.material.bottomsheet.BottomSheetDialog
import dev.liinahamari.follower.R
import kotlinx.android.synthetic.main.dialog_biometric.*

@SuppressLint("InflateParams")
class BiometricDialog(context: Context, private val biometricCallback: BiometricCallback, title: String, description: String): BottomSheetDialog(context, R.style.Theme_Design_BottomSheetDialog) {
    init {
        setContentView(layoutInflater.inflate(R.layout.dialog_biometric, null))
        tvTitle.text = title
        tvDescription?.text = description
        setCancelable(false)
    }

    fun updateStatus(status: String) {
        tvStatus.text = status
    }

    override fun setOnDismissListener(listener: DialogInterface.OnDismissListener?) {
        super.setOnDismissListener(listener)
        biometricCallback.onAuthenticationCancelled()
    }
}