package com.secureauthlib.models

import org.junit.Test
import org.junit.Assert.*

/**
 * Tests for AuthError enum
 */
class AuthErrorTest {

    @Test
    fun testBiometricUnavailable() {
        val error = AuthError.BIOMETRIC_UNAVAILABLE
        assertEquals(1001, error.code)
        assertEquals("Biometric authentication is not available on this device", error.message)
    }

    @Test
    fun testBiometricNotEnrolled() {
        val error = AuthError.BIOMETRIC_NOT_ENROLLED
        assertEquals(1002, error.code)
        assertEquals("No biometric credentials are enrolled", error.message)
    }

    @Test
    fun testAuthenticationFailed() {
        val error = AuthError.AUTHENTICATION_FAILED
        assertEquals(2001, error.code)
        assertEquals("Authentication failed", error.message)
    }

    @Test
    fun testInvalidCredentials() {
        val error = AuthError.INVALID_CREDENTIALS
        assertEquals(2003, error.code)
        assertEquals("Invalid credentials provided", error.message)
    }

    @Test
    fun testMaxRetriesExceeded() {
        val error = AuthError.MAX_RETRIES_EXCEEDED
        assertEquals(2004, error.code)
        assertEquals("Maximum retry attempts exceeded", error.message)
    }

    @Test
    fun testSessionExpired() {
        val error = AuthError.SESSION_EXPIRED
        assertEquals(3001, error.code)
        assertEquals("Session has expired", error.message)
    }

    @Test
    fun testAllErrorCodesAreUnique() {
        val codes = AuthError.values().map { it.code }
        assertEquals(codes.size, codes.distinct().size)
    }
}
