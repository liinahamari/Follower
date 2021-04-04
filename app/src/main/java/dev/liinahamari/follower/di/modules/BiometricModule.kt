package dev.liinahamari.follower.di.modules

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dev.liinahamari.follower.R
import dev.liinahamari.follower.di.scopes.BiometricScope
import dev.liinahamari.follower.helper.FlightRecorder
import dagger.Module
import dagger.Provides
import java.security.KeyStore
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Named

private const val KEY_NAME = "biometric.keyName"

/*TODO investigate DaggerLazy*/
/*TODO support PINs*/
@Module
class BiometricModule(private val activity: FragmentActivity, private val onSuccessfulAuth: () -> Unit, private val onFailedAuth: () -> Unit = {}) {
    @BiometricScope
    @Provides
    fun provideCipher(@Named(KEY_NAME) keyName: String, keyStore: KeyStore): Cipher = Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}")
            .apply {
                keyStore.load(null)
                init(Cipher.ENCRYPT_MODE, keyStore.getKey(keyName, null) as SecretKey)
            }

    @BiometricScope
    @Provides
    fun provideKeyStore(@Named(KEY_NAME) keyName: String): KeyStore = KeyStore.getInstance("AndroidKeyStore")
            .apply {
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

    @BiometricScope
    @Provides
    fun provideAuthenticator(@Named (APP_CONTEXT) context: Context, promptInfo: BiometricPrompt.PromptInfo, authCallback: BiometricPrompt.AuthenticationCallback, cipher: Cipher): Authenticator = object : Authenticator {
        override fun authenticate() {
            if (BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
                BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), authCallback).authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            }
        }
    }

    @BiometricScope
    @Provides
    @Named(KEY_NAME)
    fun provideKeyName(): String = UUID.randomUUID().toString()

    @BiometricScope
    @Provides
    fun provideBiometricDialog(@Named (APP_CONTEXT) context: Context): BiometricPrompt.PromptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(context.getString(R.string.title_records_access))
        .setDescription(context.getString(R.string.title_fingerprint_instruction))
        .setNegativeButtonText(context.getString(android.R.string.cancel))
        .build()

    @BiometricScope
    @Provides
    fun provideAuthCallback(biometricCallback: BiometricCallback): BiometricPrompt.AuthenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = biometricCallback.onAuthenticationSuccessful()
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) = biometricCallback.onAuthenticationError(errorCode, errString)
        override fun onAuthenticationFailed() = biometricCallback.onAuthenticationFailed()
    }

    @BiometricScope
    @Provides
    fun provideLoggingCallback(logger: FlightRecorder): BiometricCallback = object : BiometricCallback {
        override fun onAuthenticationFailed() = onFailedAuth.invoke().also { logger.i { "biometric auth failed" } }
        override fun onAuthenticationSuccessful() = onSuccessfulAuth.invoke().also { logger.i { "biometric auth succeed" } }
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) = onFailedAuth.invoke().also { logger.i { "biometric auth error. errorCode: $errorCode, errString: $errString" } }
    }
}

@BiometricScope
interface Authenticator {
    fun authenticate()
}

interface BiometricCallback {
    fun onAuthenticationFailed()
    fun onAuthenticationSuccessful()
    fun onAuthenticationError(errorCode: Int, errString: CharSequence)
}