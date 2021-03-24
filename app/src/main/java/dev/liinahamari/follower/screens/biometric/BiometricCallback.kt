package dev.liinahamari.follower.screens.biometric

interface BiometricCallback {
    fun onBiometricSensorMissing()
    fun onBiometricAuthenticationNotAvailable()
    fun onBiometricAuthenticationPermissionNotGranted()
    fun onAuthenticationFailed()
    fun onAuthenticationCancelled()
    fun onAuthenticationSuccessful()
    fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence)
    fun onAuthenticationError(errorCode: Int, errString: CharSequence)
}
