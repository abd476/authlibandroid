package com.secureauthlib

import com.secureauthlib.models.AuthError

/**
 * Sealed class representing the result of an authentication attempt
 */
sealed class AuthResult {
    /**
     * Authentication was successful
     * @param token The authentication token (if session management is enabled)
     * @param authType The authentication method that succeeded
     */
    data class Success(
        val token: String? = null,
        val authType: AuthType? = null
    ) : AuthResult()

    /**
     * Authentication failed
     * @param error The error message
     * @param errorCode The specific error code from AuthError enum
     * @param authType The authentication method that failed
     */
    data class Failure(
        val error: String,
        val errorCode: Int = AuthError.UNKNOWN_ERROR.code,
        val authType: AuthType? = null
    ) : AuthResult() {
        constructor(error: AuthError, authType: AuthType? = null) : this(
            error.message,
            error.code,
            authType
        )
    }

    /**
     * Authentication was cancelled by the user
     */
    object Cancelled : AuthResult()

    /**
     * Authentication is not available (e.g., no biometric hardware)
     * @param authType The authentication method that is not available
     */
    data class NotAvailable(val authType: AuthType? = null) : AuthResult()
}
