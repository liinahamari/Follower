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

package dev.liinahamari.follower.screens.settings

import android.app.Application
import androidx.biometric.BiometricManager
import dev.liinahamari.follower.R
import dev.liinahamari.follower.helper.rx.BaseComposers
import io.reactivex.rxjava3.core.Single

@SettingsScope
class BiometricAvailabilityValidationUseCase constructor(private val app: Application, private val baseComposers: BaseComposers) {
    fun execute(): Single<BiometricAvailabilityResult> = Single.fromCallable {
        return@fromCallable when {
            BiometricManager.from(app).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ||
                    BiometricManager.from(app).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricAvailabilityResult.NotAvailable(R.string.summary_lack_of_fingerprint_sensor)
            BiometricManager.from(app).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailabilityResult.NotAvailable(R.string.summary_sensor_unavailable)
            BiometricManager.from(app).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricAvailabilityResult.NotAvailable(R.string.summary_security_vulnerability)
            BiometricManager.from(app).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> BiometricAvailabilityResult.NotAvailable(R.string.unknown_error)
            BiometricManager.from(app).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailabilityResult.NotAvailable(R.string.summary_you_dont_have_fingerprint_presented)
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