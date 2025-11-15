package com.secureauthlib.utils

import android.content.Context
import androidx.biometric.BiometricManager
import com.secureauthlib.AuthType
import com.secureauthlib.models.AuthError

/**
 * Utility class for device capability detection
 */
object DeviceUtils {

    /**
     * Check if biometric authentication is available
     */
    fun canUseBiometrics(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        val result = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Check if fingerprint is available
     */
    fun canUseFingerprint(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        val result = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Check if face recognition is available
     */
    fun canUseFace(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        val result = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Get the first available authentication method from a list
     */
    fun getFirstAvailableMethod(context: Context, methods: List<AuthType>): AuthType? {
        for (method in methods) {
            when (method) {
                AuthType.FINGERPRINT -> if (canUseFingerprint(context)) return method
                AuthType.FACE -> if (canUseFace(context)) return method
                AuthType.PIN, AuthType.PATTERN, AuthType.PASSWORD -> return method
            }
        }
        return null
    }

    /**
     * Get all available authentication methods
     */
    fun getAvailableMethods(context: Context, requestedMethods: List<AuthType>): List<AuthType> {
        return requestedMethods.filter { method ->
            when (method) {
                AuthType.FINGERPRINT -> canUseFingerprint(context)
                AuthType.FACE -> canUseFace(context)
                AuthType.PIN, AuthType.PATTERN, AuthType.PASSWORD -> true
            }
        }
    }

    /**
     * Get error code for unavailable biometric
     */
    fun getBiometricError(context: Context): AuthError {
        val biometricManager = BiometricManager.from(context)
        val result = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        
        return when (result) {
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> AuthError.BIOMETRIC_HARDWARE_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> AuthError.BIOMETRIC_NOT_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> AuthError.BIOMETRIC_UNAVAILABLE
            else -> AuthError.BIOMETRIC_UNAVAILABLE
        }
    }
}
