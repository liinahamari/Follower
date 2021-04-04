package dev.liinahamari.follower.screens.settings

import android.content.Context
import androidx.biometric.BiometricManager
import dev.liinahamari.follower.R
import dev.liinahamari.follower.di.modules.APP_CONTEXT
import dev.liinahamari.follower.helper.rx.BaseComposers
import io.reactivex.Single
import javax.inject.Named

@SettingsScope
class BiometricAvailabilityValidationUseCase constructor(@Named(APP_CONTEXT) private val context: Context, private val baseComposers: BaseComposers) {
    fun execute(): Single<BiometricAvailabilityResult> = Single.fromCallable {
        return@fromCallable when {
            BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ||
                    BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricAvailabilityResult.NotAvailable(R.string.summary_lack_of_fingerprint_sensor)
            BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailabilityResult.NotAvailable(R.string.summary_sensor_unavailable)
            BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricAvailabilityResult.NotAvailable(R.string.summary_security_vulnerability)
            BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> BiometricAvailabilityResult.NotAvailable(R.string.unknown_error)
            BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailabilityResult.NotAvailable(R.string.summary_you_dont_have_fingerprint_presented)
            else -> BiometricAvailabilityResult.Available
        }
    }
        .onErrorReturn { BiometricAvailabilityResult.NotAvailable(R.string.error_unexpected) }
        .compose(baseComposers.applySingleSchedulers())
}

sealed class BiometricAvailabilityResult {
    object Available : BiometricAvailabilityResult()
    data class NotAvailable(val explanation: Int) : BiometricAvailabilityResult()
}