package com.secureauthlib

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.secureauthlib.core.PatternHandler
import com.secureauthlib.models.AuthConfig
import com.secureauthlib.models.AuthError
import com.secureauthlib.utils.AuthLogger
import com.secureauthlib.utils.DeviceUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Callback interface for authentication results
 */
interface AuthCallback {
    fun onSuccess(result: AuthResult.Success)
    fun onFailure(result: AuthResult.Failure)
    fun onCancelled()
    fun onNotAvailable(result: AuthResult.NotAvailable)
}

/**
 * Unified authentication library entry point
 * Supports both callback-based and coroutine-based APIs
 */
class AuthLibrary private constructor(
    private val context: Context,
    private val activity: FragmentActivity
) {
    private val authManager = AuthManager.create(context, activity)
    private val patternHandler = PatternHandler(context)
    private var retryCount = 0
    private var lastFailureTime = 0L

    companion object {
        /**
         * Create an instance of AuthLibrary
         */
        fun create(context: Context, activity: FragmentActivity): AuthLibrary {
            return AuthLibrary(context.applicationContext, activity)
        }
    }

    /**
     * Authenticate with automatic fallback through multiple methods
     * Coroutine-based API
     */
    suspend fun authenticate(config: AuthConfig = AuthConfig()): AuthResult {
        AuthLogger.info("Starting authentication with methods: ${config.methods}")

        // Check for valid session first
        if (config.enableSessionToken && config.sessionTimeoutMs > 0L) {
            val sessionToken = authManager.getSessionToken(config.sessionTimeoutMs)
            if (sessionToken != null) {
                AuthLogger.info("Valid session token found, skipping authentication")
                return AuthResult.Success(token = sessionToken)
            }
        }

        // Check inactivity timeout
        if (config.inactivityTimeoutMs > 0L) {
            val isValid = authManager.isSessionValid(config.inactivityTimeoutMs)
            if (!isValid) {
                AuthLogger.warn("Session invalidated due to inactivity")
                return AuthResult.Failure(AuthError.SESSION_EXPIRED, null)
            }
        }

        // Check cooldown period
        if (retryCount >= config.maxRetries) {
            val elapsed = System.currentTimeMillis() - lastFailureTime
            if (elapsed < config.cooldownPeriodMs) {
                AuthLogger.warn("Cooldown period active")
                return AuthResult.Failure(AuthError.COOLDOWN_ACTIVE, null)
            } else {
                // Reset retry count after cooldown
                retryCount = 0
            }
        }

        // Chained authentication
        if (config.requireChainedAuth && config.chainedMethods.isNotEmpty()) {
            return authenticateChained(config)
        }

        // Get available methods
        val availableMethods = DeviceUtils.getAvailableMethods(context, config.methods)
        if (availableMethods.isEmpty()) {
            AuthLogger.error("No available authentication methods")
            return AuthResult.NotAvailable()
        }

        // Try each method in order until one succeeds
        for (method in availableMethods) {
            AuthLogger.debug("Attempting authentication with: $method")
            val result = authenticateMethod(method, config)
            
            when (result) {
                is AuthResult.Success -> {
                    retryCount = 0
                    // Save session token if enabled
                    if (config.enableSessionToken) {
                        val token = generateSessionToken()
                        authManager.saveSessionToken(token, config.sessionTimeoutMs)
                        return result.copy(token = token, authType = method)
                    }
                    return result.copy(authType = method)
                }
                is AuthResult.Cancelled -> {
                    return result
                }
                is AuthResult.Failure -> {
                    retryCount++
                    lastFailureTime = System.currentTimeMillis()
                    if (retryCount >= config.maxRetries) {
                        AuthLogger.warn("Max retries exceeded")
                        return AuthResult.Failure(AuthError.MAX_RETRIES_EXCEEDED, method)
                    }
                    // Continue to next method
                }
                is AuthResult.NotAvailable -> {
                    // Continue to next method
                }
            }
        }

        // All methods failed
        AuthLogger.error("All authentication methods failed")
        return AuthResult.Failure(AuthError.AUTHENTICATION_FAILED, null)
    }

    /**
     * Authenticate with callback-based API
     */
    fun authenticate(
        config: AuthConfig = AuthConfig(),
        callback: AuthCallback
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = authenticate(config)
            when (result) {
                is AuthResult.Success -> callback.onSuccess(result)
                is AuthResult.Failure -> callback.onFailure(result)
                is AuthResult.Cancelled -> callback.onCancelled()
                is AuthResult.NotAvailable -> callback.onNotAvailable(result)
            }
        }
    }

    /**
     * Authenticate a single method
     */
    private suspend fun authenticateMethod(
        method: AuthType,
        config: AuthConfig
    ): AuthResult = withContext(Dispatchers.Main) {
        val title = config.title ?: getDefaultTitle(method)
        val subtitle = config.subtitle ?: getDefaultSubtitle(method)
        val negativeButtonText = config.negativeButtonText

        when (method) {
            AuthType.FINGERPRINT -> {
                authManager.authenticate(
                    AuthType.FINGERPRINT,
                    title = title,
                    subtitle = subtitle,
                    negativeButtonText = negativeButtonText
                )
            }
            AuthType.FACE -> {
                authManager.authenticate(
                    AuthType.FACE,
                    title = title,
                    subtitle = subtitle,
                    negativeButtonText = negativeButtonText
                )
            }
            AuthType.PIN -> {
                val pin = getStoredCredential("pin")
                if (pin == null) {
                    AuthResult.Failure(AuthError.INVALID_CREDENTIALS, method)
                } else {
                    authManager.authenticate(
                        AuthType.PIN,
                        expectedValue = pin,
                        title = title,
                        subtitle = subtitle,
                        negativeButtonText = negativeButtonText
                    )
                }
            }
            AuthType.PATTERN -> {
                val pattern = getStoredCredential("pattern")
                if (pattern == null) {
                    AuthResult.Failure(AuthError.INVALID_CREDENTIALS, method)
                } else {
                    patternHandler.authenticate(pattern, title, subtitle)
                }
            }
            AuthType.PASSWORD -> {
                val password = getStoredCredential("password")
                if (password == null) {
                    AuthResult.Failure(AuthError.INVALID_CREDENTIALS, method)
                } else {
                    authManager.authenticate(
                        AuthType.PASSWORD,
                        expectedValue = password,
                        title = title,
                        subtitle = subtitle,
                        negativeButtonText = negativeButtonText
                    )
                }
            }
        }
    }

    /**
     * Chained authentication - require multiple methods
     */
    private suspend fun authenticateChained(config: AuthConfig): AuthResult {
        AuthLogger.info("Starting chained authentication")
        val results = mutableListOf<AuthResult.Success>()
        
        for (method in config.chainedMethods) {
            val result = authenticateMethod(method, config)
            when (result) {
                is AuthResult.Success -> results.add(result)
                else -> return result // Fail fast on any failure
            }
        }
        
        if (results.size == config.chainedMethods.size) {
            val token = if (config.enableSessionToken) generateSessionToken() else null
            if (token != null) {
                authManager.saveSessionToken(token, config.sessionTimeoutMs)
            }
            return AuthResult.Success(token = token, authType = config.chainedMethods.first())
        }
        
        return AuthResult.Failure(AuthError.AUTHENTICATION_FAILED, null)
    }

    private fun getDefaultTitle(method: AuthType): String {
        return when (method) {
            AuthType.FINGERPRINT -> "Fingerprint Authentication"
            AuthType.FACE -> "Face Authentication"
            AuthType.PIN -> "Enter PIN"
            AuthType.PATTERN -> "Pattern Authentication"
            AuthType.PASSWORD -> "Enter Password"
        }
    }

    private fun getDefaultSubtitle(method: AuthType): String {
        return when (method) {
            AuthType.FINGERPRINT -> "Use your fingerprint to authenticate"
            AuthType.FACE -> "Use your face to authenticate"
            AuthType.PIN -> "Please enter your PIN"
            AuthType.PATTERN -> "Draw your pattern"
            AuthType.PASSWORD -> "Please enter your password"
        }
    }

    private suspend fun getStoredCredential(type: String): String? {
        return authManager.getToken("user_$type")
    }

    private fun generateSessionToken(): String {
        return "session_${System.currentTimeMillis()}_${(0..1000).random()}"
    }

    /**
     * Invalidate current session
     */
    suspend fun logout() {
        authManager.invalidateSession()
        retryCount = 0
        lastFailureTime = 0L
    }

    /**
     * Update last activity timestamp
     */
    suspend fun updateActivity() {
        authManager.updateLastActivity()
    }
}
