package com.example.follower.di.modules

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.core.os.CancellationSignal
import androidx.fragment.app.FragmentActivity
import com.example.follower.R
import com.example.follower.helper.FlightRecorder
import com.example.follower.screens.biometric.Authenticator
import com.example.follower.screens.biometric.BiometricCallback
import com.example.follower.screens.biometric.BiometricDialog
import dagger.Module
import dagger.Provides
import java.security.KeyStore
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Named
import javax.inject.Scope

private const val KEY_NAME = "keyName"
private const val BIOMETRIC_DIALOG_TITLE = "BIOMETRIC_DIALOG_TITLE"
private const val BIOMETRIC_DIALOG_DESCRIPTION = "BIOMETRIC_DIALOG_DESCRIPTION"

/*TODO investigate DaggerLazy*/
@Module
class BiometricModule(private val activity: FragmentActivity, private val onSuccessfulAuth: () -> Unit, private val onFailedAuth: () -> Unit = {}) {
    @BiometricScope
    @Provides
    fun provideCipher(@Named(KEY_NAME) keyName: String, keyStore: KeyStore?): Cipher? = kotlin.runCatching {
        Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}")
            .apply {
                keyStore?.load(null)
                init(Cipher.ENCRYPT_MODE, keyStore?.getKey(keyName, null) as SecretKey)
            }
    }.getOrNull()

    @BiometricScope
    @Provides
    fun provideKeyStore(@Named(KEY_NAME) keyName: String): KeyStore? = kotlin.runCatching {
        KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
            with(KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")) {
                init(
                    KeyGenParameterSpec.Builder(keyName, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setUserAuthenticationRequired(true)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .build()
                )
                generateKey()
            }
        }
    }.getOrNull()

    @BiometricScope
    @Provides
    fun provideAuthenticator(
        biometricCallback: BiometricCallback,
        @Named(APP_CONTEXT) context: Context,
        promptInfo: BiometricPrompt.PromptInfo?,
        authCallbackV23to27: FingerprintManagerCompat.AuthenticationCallback,
        authCallbackV28: BiometricPrompt.AuthenticationCallback?,
        dialogV23to27: BiometricDialog,
        cipher: Cipher?,
        keyStore: KeyStore?
    ): Authenticator = object : Authenticator {
        override fun authenticate() {
            when {
                ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED -> {
                    biometricCallback.onBiometricAuthenticationPermissionNotGranted()
                    return
                }
                FingerprintManagerCompat.from(context).isHardwareDetected.not() -> {
                    biometricCallback.onBiometricSensorMissing()
                    return
                }
                FingerprintManagerCompat.from(context).hasEnrolledFingerprints().not() -> {
                    biometricCallback.onBiometricAuthenticationNotAvailable()
                    return
                }
                else -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        BiometricPrompt(activity, authCallbackV28!!).authenticate(promptInfo!!)
                    } else {
                        if (keyStore != null && cipher != null) {
                            FingerprintManagerCompat.from(context)
                                .authenticate(FingerprintManagerCompat.CryptoObject(cipher), 0, CancellationSignal(), authCallbackV23to27, null)
                            dialogV23to27.show()
                        }
                    }
                }
            }
        }
    }

    @BiometricScope
    @Provides
    @Named(KEY_NAME)
    fun provideKeyName(): String = UUID.randomUUID().toString()

    @BiometricScope
    @Provides
    fun provideBiometricDialogV23to28(
        @Named(BIOMETRIC_DIALOG_TITLE) title: String,
        @Named(BIOMETRIC_DIALOG_DESCRIPTION) description: String,
        biometricCallback: BiometricCallback
    ): BiometricDialog = BiometricDialog(activity, biometricCallback, title, description)

    @BiometricScope
    @Provides
    fun provideBiometricDialogForV28(
        @Named(BIOMETRIC_DIALOG_TITLE) title: String,
        @Named(BIOMETRIC_DIALOG_DESCRIPTION) description: String,
        @Named(APP_CONTEXT) context: Context
    ): BiometricPrompt.PromptInfo? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setDescription(description)
            .setNegativeButtonText(context.getString(android.R.string.cancel))
            .build()
    } else null

    @BiometricScope
    @Provides
    @Named(BIOMETRIC_DIALOG_TITLE)
    fun provideDialogTitle(@Named(APP_CONTEXT) context: Context): String = context.getString(R.string.title_records_access)

    @BiometricScope
    @Provides
    @Named(BIOMETRIC_DIALOG_DESCRIPTION)
    fun provideDialogDescription(@Named(APP_CONTEXT) context: Context): String = context.getString(R.string.title_fingerprint_instruction)

    @BiometricScope
    @Provides
    fun provideAuthCallbackV28(biometricCallback: BiometricCallback): BiometricPrompt.AuthenticationCallback? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = biometricCallback.onAuthenticationSuccessful()
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) = biometricCallback.onAuthenticationError(errorCode, errString)
            override fun onAuthenticationFailed() = biometricCallback.onAuthenticationFailed()
        }
    } else null

    @BiometricScope
    @Provides
    fun provideAuthCallbackV23to27(biometricCallback: BiometricCallback, dialog: BiometricDialog, @Named(APP_CONTEXT) context: Context): FingerprintManagerCompat.AuthenticationCallback = object : FingerprintManagerCompat.AuthenticationCallback() {
        override fun onAuthenticationError(errMsgId: Int, errString: CharSequence) {
            dialog.updateStatus(errString.toString())
            biometricCallback.onAuthenticationError(errMsgId, errString)
        }

        override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence) {
            dialog.updateStatus(helpString.toString())
            biometricCallback.onAuthenticationHelp(helpMsgId, helpString)
        }

        override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult) {
            dialog.dismiss()
            biometricCallback.onAuthenticationSuccessful()
        }

        override fun onAuthenticationFailed() {
            dialog.updateStatus(context.getString(R.string.title_not_recognized))
            biometricCallback.onAuthenticationFailed()
        }
    }

    @BiometricScope
    @Provides
    fun provideLoggingCallback(logger: FlightRecorder): BiometricCallback = object : BiometricCallback {
        override fun onBiometricSensorMissing() = logger.i { "biometric auth: sensor missing" }
        override fun onBiometricAuthenticationNotAvailable() = logger.i { "biometric auth not available" }
        override fun onBiometricAuthenticationPermissionNotGranted() = logger.i { "biometric auth: permission not granted" }
        override fun onAuthenticationFailed() = onFailedAuth.invoke().also { logger.i { "biometric auth failed" } }
        override fun onAuthenticationCancelled() = onFailedAuth.invoke().also { logger.i { "biometric auth cancelled" } }
        override fun onAuthenticationSuccessful() = onSuccessfulAuth.invoke().also { logger.i { "biometric auth succeed" } }
        override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence) = logger.i { "biometric auth: onAuthenticationHelp(). helpCode: $helpCode, helpString: $helpString" }
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) = onFailedAuth.invoke().also { logger.i { "biometric auth error. errorCode: $errorCode, errString: $errString" } }
    }
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class BiometricScope