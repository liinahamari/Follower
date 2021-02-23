package com.example.follower.screens.biometric

import com.example.follower.di.modules.BiometricScope

@BiometricScope
interface Authenticator {
    fun authenticate()
}
