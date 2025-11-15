package com.secureauthlib.models

/**
 * Error codes for authentication failures
 */
enum class AuthError(val code: Int, val message: String) {
    BIOMETRIC_UNAVAILABLE(1001, "Biometric authentication is not available on this device"),
    BIOMETRIC_NOT_ENROLLED(1002, "No biometric credentials are enrolled"),
    BIOMETRIC_HARDWARE_UNAVAILABLE(1003, "Biometric hardware is not available"),
    AUTHENTICATION_FAILED(2001, "Authentication failed"),
    AUTHENTICATION_CANCELED(2002, "Authentication was canceled by the user"),
    INVALID_CREDENTIALS(2003, "Invalid credentials provided"),
    MAX_RETRIES_EXCEEDED(2004, "Maximum retry attempts exceeded"),
    COOLDOWN_ACTIVE(2005, "Please wait before trying again"),
    SESSION_EXPIRED(3001, "Session has expired"),
    TOKEN_INVALID(3002, "Invalid or missing token"),
    UNKNOWN_ERROR(9999, "An unknown error occurred")
}
