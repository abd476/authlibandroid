package com.secureauthlib

import android.content.Context
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Main authentication manager providing a unified API for all authentication types
 */
class AuthManager private constructor(
    private val context: Context,
    private val activity: FragmentActivity
) {

    private val tokenStorage = SecureTokenStorage(context)
    private val biometricHelper = BiometricAuthHelper(context, activity)
    private val pinPasswordHelper = PinPasswordAuthHelper(activity)

    companion object {
        /**
         * Create an instance of AuthManager
         * @param context Application context
         * @param activity FragmentActivity required for biometric authentication
         */
        fun create(context: Context, activity: FragmentActivity): AuthManager {
            return AuthManager(context.applicationContext, activity)
        }
    }

    /**
     * Check if a specific authentication type is available
     */
    suspend fun isAuthTypeAvailable(authType: AuthType): Boolean = withContext(Dispatchers.IO) {
        when (authType) {
            AuthType.FINGERPRINT -> biometricHelper.isFingerprintAvailable()
            AuthType.FACE -> biometricHelper.isFaceAvailable()
            AuthType.PIN, AuthType.PATTERN, AuthType.PASSWORD -> true
        }
    }

    /**
     * Authenticate using the specified authentication type
     * @param authType The type of authentication to use
     * @param expectedValue The expected PIN or password (only required for PIN/PASSWORD)
     * @param title Custom title for the authentication prompt
     * @param subtitle Custom subtitle for the authentication prompt
     * @param negativeButtonText Custom text for the negative button
     * @return AuthResult indicating success, failure, cancellation, or unavailability
     */
    suspend fun authenticate(
        authType: AuthType,
        expectedValue: String? = null,
        title: String? = null,
        subtitle: String? = null,
        negativeButtonText: String? = null
    ): AuthResult = withContext(Dispatchers.Main) {
        when (authType) {
            AuthType.FINGERPRINT -> {
                if (!biometricHelper.isFingerprintAvailable()) {
                    return@withContext AuthResult.NotAvailable(AuthType.FINGERPRINT)
                }
                biometricHelper.authenticateFingerprint(
                    title = title ?: "Fingerprint Authentication",
                    subtitle = subtitle ?: "Use your fingerprint to authenticate",
                    negativeButtonText = negativeButtonText ?: "Cancel"
                )
            }
            AuthType.FACE -> {
                if (!biometricHelper.isFaceAvailable()) {
                    return@withContext AuthResult.NotAvailable(AuthType.FACE)
                }
                biometricHelper.authenticateFace(
                    title = title ?: "Face Authentication",
                    subtitle = subtitle ?: "Use your face to authenticate",
                    negativeButtonText = negativeButtonText ?: "Cancel"
                )
            }
            AuthType.PIN -> {
                val pin = expectedValue ?: return@withContext AuthResult.Failure(
                    com.secureauthlib.models.AuthError.INVALID_CREDENTIALS,
                    AuthType.PIN
                )
                pinPasswordHelper.authenticateWithPin(
                    title = title ?: "Enter PIN",
                    message = subtitle ?: "Please enter your PIN",
                    expectedPin = pin
                )
            }
            AuthType.PATTERN -> {
                // Pattern authentication is handled by PatternHandler in AuthLibrary
                // This is a fallback for direct AuthManager usage
                val pattern = expectedValue ?: return@withContext AuthResult.Failure(
                    com.secureauthlib.models.AuthError.INVALID_CREDENTIALS,
                    AuthType.PATTERN
                )
                // For now, return not implemented - pattern should be used via AuthLibrary
                AuthResult.Failure(
                    com.secureauthlib.models.AuthError.AUTHENTICATION_FAILED,
                    AuthType.PATTERN
                )
            }
            AuthType.PASSWORD -> {
                val password = expectedValue ?: return@withContext AuthResult.Failure(
                    com.secureauthlib.models.AuthError.INVALID_CREDENTIALS,
                    AuthType.PASSWORD
                )
                pinPasswordHelper.authenticateWithPassword(
                    title = title ?: "Enter Password",
                    message = subtitle ?: "Please enter your password",
                    expectedPassword = password
                )
            }
        }
    }

    /**
     * Authenticate with automatic fallback
     * Tries the primary auth type, and if unavailable, falls back to secondary auth type
     * @param primaryAuthType Primary authentication type to try first
     * @param fallbackAuthType Fallback authentication type if primary is unavailable
     * @param expectedValue The expected PIN or password (required for PIN/PASSWORD)
     * @return AuthResult indicating success, failure, or cancellation
     */
    suspend fun authenticateWithFallback(
        primaryAuthType: AuthType,
        fallbackAuthType: AuthType,
        expectedValue: String? = null
    ): AuthResult {
        val primaryAvailable = isAuthTypeAvailable(primaryAuthType)
        
        return if (primaryAvailable) {
            authenticate(primaryAuthType, expectedValue)
        } else {
            val fallbackAvailable = isAuthTypeAvailable(fallbackAuthType)
            if (fallbackAvailable) {
                authenticate(fallbackAuthType, expectedValue)
            } else {
                AuthResult.NotAvailable(fallbackAuthType)
            }
        }
    }

    /**
     * Save an authentication token securely
     * @param key The key to store the token under
     * @param token The token to store
     * @return true if successful, false otherwise
     */
    suspend fun saveToken(key: String, token: String): Boolean {
        return tokenStorage.saveToken(key, token)
    }

    /**
     * Retrieve a stored authentication token
     * @param key The key the token was stored under
     * @return The token if found, null otherwise
     */
    suspend fun getToken(key: String): String? {
        return tokenStorage.getToken(key)
    }

    /**
     * Delete a stored authentication token
     * @param key The key of the token to delete
     * @return true if successful, false otherwise
     */
    suspend fun deleteToken(key: String): Boolean {
        return tokenStorage.deleteToken(key)
    }

    /**
     * Clear all stored authentication tokens
     * @return true if successful, false otherwise
     */
    suspend fun clearAllTokens(): Boolean {
        return tokenStorage.clearAllTokens()
    }

    /**
     * Save session token with timeout
     */
    suspend fun saveSessionToken(token: String, timeoutMs: Long = 0L): Boolean {
        return tokenStorage.saveSessionToken(token, timeoutMs)
    }

    /**
     * Get session token if valid
     */
    suspend fun getSessionToken(timeoutMs: Long = 0L): String? {
        return tokenStorage.getSessionToken(timeoutMs)
    }

    /**
     * Check if session is valid
     */
    suspend fun isSessionValid(inactivityTimeoutMs: Long = 0L): Boolean {
        return tokenStorage.isSessionValid(inactivityTimeoutMs)
    }

    /**
     * Invalidate session
     */
    suspend fun invalidateSession(): Boolean {
        return tokenStorage.invalidateSession()
    }

    /**
     * Update last activity timestamp
     */
    suspend fun updateLastActivity(): Boolean {
        return tokenStorage.updateLastActivity()
    }
}
