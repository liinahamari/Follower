package com.example.follower.screens.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import com.example.follower.R
import com.example.follower.di.scopes.SettingsScope
import com.example.follower.helper.rx.BaseComposers
import io.reactivex.Single

@SettingsScope
class BiometricAvailabilityValidationUseCase constructor(/*@Named(APP_CONTEXT) */private val context: Context, private val baseComposers: BaseComposers) {
    fun execute(): Single<BiometricAvailabilityResult> = Single.fromCallable {
        return@fromCallable when {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED -> BiometricAvailabilityResult.NotAvailable(R.string.summary_lack_of_fingerprint_permission)
            FingerprintManagerCompat.from(context).isHardwareDetected.not() -> BiometricAvailabilityResult.NotAvailable(R.string.summary_lack_of_fingerprint_sensor)
            FingerprintManagerCompat.from(context).hasEnrolledFingerprints().not() -> BiometricAvailabilityResult.NotAvailable(R.string.summary_you_dont_have_fingerprint_presented)
            else -> BiometricAvailabilityResult.Available
        }
    }
        .onErrorReturn { BiometricAvailabilityResult.NotAvailable(R.string.error_unexpected) }
        .compose(baseComposers.applySingleSchedulers())
}

sealed class BiometricAvailabilityResult {
    object Available: BiometricAvailabilityResult()
    data class NotAvailable(val explanation: Int): BiometricAvailabilityResult()
}