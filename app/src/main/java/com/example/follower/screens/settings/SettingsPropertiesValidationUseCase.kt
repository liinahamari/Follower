package com.example.follower.screens.settings

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import com.example.follower.R
import com.example.follower.ext.getBooleanOf
import com.example.follower.helper.rx.BaseComposers
import io.reactivex.Maybe
import io.reactivex.Single

@SettingsScope
class SettingsPropertiesValidationUseCase constructor(/*@Named(APP_CONTEXT) */private val context: Context, private val baseComposers: BaseComposers, private val prefs: SharedPreferences) {
    fun isBiometricAvailable(): Single<BiometricAvailabilityResult> = Single.fromCallable {
        return@fromCallable when {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED -> BiometricAvailabilityResult.NotAvailable(R.string.summary_lack_of_fingerprint_permission)
            FingerprintManagerCompat.from(context).isHardwareDetected.not() -> BiometricAvailabilityResult.NotAvailable(R.string.summary_lack_of_fingerprint_sensor)
            FingerprintManagerCompat.from(context).hasEnrolledFingerprints().not() -> BiometricAvailabilityResult.NotAvailable(R.string.summary_you_dont_have_fingerprint_presented)
            else -> BiometricAvailabilityResult.Available
        }
    }
        .onErrorReturn { BiometricAvailabilityResult.NotAvailable(R.string.error_unexpected) }
        .compose(baseComposers.applySingleSchedulers())

    fun isMatchingSystemThemeAvailable(): Maybe<MatchSystemThemeResult> = Single.just(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        .filter { it }
        .map { prefs.getBooleanOf(context.getString(R.string.pref_match_system_theme)) }
        .map<MatchSystemThemeResult> { MatchSystemThemeResult.Available(it) }
        .onErrorReturn { MatchSystemThemeResult.NotAvailable(R.string.db_error) }
}

sealed class BiometricAvailabilityResult {
    object Available: BiometricAvailabilityResult()
    data class NotAvailable(val explanation: Int): BiometricAvailabilityResult()
}

sealed class MatchSystemThemeResult {
    data class Available(val isEnabled: Boolean): MatchSystemThemeResult()
    data class NotAvailable(val explanation: Int): MatchSystemThemeResult()
}