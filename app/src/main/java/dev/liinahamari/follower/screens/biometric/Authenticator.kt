package dev.liinahamari.follower.screens.biometric

import dev.liinahamari.follower.di.modules.BiometricScope

@BiometricScope
interface Authenticator {
    fun authenticate()
}
