package com.secureauthlib

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.secureauthlib.models.AuthError
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Helper class for handling biometric authentication (fingerprint and face)
 */
internal class BiometricAuthHelper(
    private val context: Context,
    private val activity: FragmentActivity
) {

    private val biometricManager = BiometricManager.from(context)

    /**
     * Check if biometric authentication is available
     */
    fun isBiometricAvailable(): Boolean {
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    /**
     * Authenticate using fingerprint
     */
    suspend fun authenticateFingerprint(
        title: String = "Fingerprint Authentication",
        subtitle: String = "Use your fingerprint to authenticate",
        negativeButtonText: String = "Cancel"
    ): AuthResult {
        // First check if fingerprint is specifically available
        if (!isFingerprintAvailable()) {
            return AuthResult.NotAvailable(AuthType.FINGERPRINT)
        }
        return authenticate(
            title = title,
            subtitle = subtitle,
            negativeButtonText = negativeButtonText,
            allowedAuthenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG
        )
    }

    /**
     * Authenticate using face recognition
     * Note: On devices with both fingerprint and face enrolled, Android may show fingerprint.
     * This is a limitation of the Android BiometricPrompt API.
     */
    suspend fun authenticateFace(
        title: String = "Face Authentication",
        subtitle: String = "Use your face to authenticate",
        negativeButtonText: String = "Cancel"
    ): AuthResult {
        // Check if face is available
        if (!isFaceAvailable()) {
            return AuthResult.NotAvailable(AuthType.FACE)
        }
        
        // If fingerprint is also available, BIOMETRIC_WEAK might still show fingerprint
        // We need to use BIOMETRIC_WEAK but exclude BIOMETRIC_STRONG
        // However, Android API doesn't support this directly
        // So we check: if only face is available (no fingerprint), use BIOMETRIC_WEAK
        // If both are available, we still use BIOMETRIC_WEAK but the system may show fingerprint
        val hasFingerprint = isFingerprintAvailable()
        
        // Use BIOMETRIC_WEAK which should prefer face, but may fall back to fingerprint
        // To force face-only, we'd need to check if fingerprint is NOT available
        val authenticators = if (hasFingerprint) {
            // Both available - use WEAK but it might show fingerprint (Android limitation)
            // We can't force face-only when both are enrolled
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        } else {
            // Only face available - use WEAK
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        }
        
        return authenticate(
            title = title,
            subtitle = subtitle,
            negativeButtonText = negativeButtonText,
            allowedAuthenticators = authenticators
        )
    }

    /**
     * Authenticate using biometrics with specified authenticator type
     */
    private suspend fun authenticate(
        title: String,
        subtitle: String,
        negativeButtonText: String,
        allowedAuthenticators: Int
    ): AuthResult = suspendCancellableCoroutine { continuation ->

        val canAuthenticate = when (biometricManager.canAuthenticate(allowedAuthenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }

        if (!canAuthenticate) {
            val authType = if (allowedAuthenticators == BiometricManager.Authenticators.BIOMETRIC_STRONG) {
                AuthType.FINGERPRINT
            } else {
                AuthType.FACE
            }
            continuation.resume(AuthResult.NotAvailable(authType))
            return@suspendCancellableCoroutine
        }

        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    val authType = if (allowedAuthenticators == BiometricManager.Authenticators.BIOMETRIC_STRONG) {
                        AuthType.FINGERPRINT
                    } else {
                        AuthType.FACE
                    }
                    continuation.resume(AuthResult.Success(authType = authType))
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            continuation.resume(AuthResult.Cancelled)
                        }
                        else -> {
                            val authType = if (allowedAuthenticators == BiometricManager.Authenticators.BIOMETRIC_STRONG) {
                                AuthType.FINGERPRINT
                            } else {
                                AuthType.FACE
                            }
                            continuation.resume(
                                AuthResult.Failure(
                                    AuthError.AUTHENTICATION_FAILED,
                                    authType
                                )
                            )
                        }
                    }
                }

                override fun onAuthenticationFailed() {
                    val authType = if (allowedAuthenticators == BiometricManager.Authenticators.BIOMETRIC_STRONG) {
                        AuthType.FINGERPRINT
                    } else {
                        AuthType.FACE
                    }
                    continuation.resume(
                        AuthResult.Failure(
                            AuthError.AUTHENTICATION_FAILED,
                            authType
                        )
                    )
                }
            }
        )

        // Build prompt info with specific authenticator requirements
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(allowedAuthenticators)
            // Don't allow device credential - force biometric only
            // This ensures we only use biometrics, not PIN/pattern
            .build()

        continuation.invokeOnCancellation {
            // Cancellation handled by the callback
        }

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Check if fingerprint is available
     */
    fun isFingerprintAvailable(): Boolean {
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    /**
     * Check if face authentication is available
     * This checks if BIOMETRIC_WEAK is available, which includes face unlock
     */
    fun isFaceAvailable(): Boolean {
        // Check if BIOMETRIC_WEAK is available (face unlock)
        // Note: This may return true even if fingerprint is also available
        // because BIOMETRIC_WEAK can include both on some devices
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }
    
    /**
     * Check if ONLY face is available (no fingerprint)
     * This helps determine if we can guarantee face-only authentication
     */
    fun isOnlyFaceAvailable(): Boolean {
        val faceAvailable = isFaceAvailable()
        val fingerprintAvailable = isFingerprintAvailable()
        return faceAvailable && !fingerprintAvailable
    }
}
