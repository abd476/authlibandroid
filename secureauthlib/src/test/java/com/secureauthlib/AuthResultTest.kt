package com.secureauthlib

import com.secureauthlib.models.AuthError
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests for AuthResult sealed class
 */
class AuthResultTest {

    @Test
    fun testSuccessResult() {
        val result = AuthResult.Success(token = "test_token", authType = AuthType.FINGERPRINT)
        assertTrue(result is AuthResult.Success)
        assertEquals("test_token", (result as AuthResult.Success).token)
        assertEquals(AuthType.FINGERPRINT, (result as AuthResult.Success).authType)
    }

    @Test
    fun testSuccessResultWithoutToken() {
        val result = AuthResult.Success(authType = AuthType.PIN)
        assertTrue(result is AuthResult.Success)
        assertNull((result as AuthResult.Success).token)
        assertEquals(AuthType.PIN, (result as AuthResult.Success).authType)
    }

    @Test
    fun testFailureResultWithError() {
        val result = AuthResult.Failure(
            error = "Authentication failed",
            errorCode = AuthError.AUTHENTICATION_FAILED.code,
            authType = AuthType.FACE
        )
        assertTrue(result is AuthResult.Failure)
        assertEquals("Authentication failed", (result as AuthResult.Failure).error)
        assertEquals(AuthError.AUTHENTICATION_FAILED.code, (result as AuthResult.Failure).errorCode)
        assertEquals(AuthType.FACE, (result as AuthResult.Failure).authType)
    }

    @Test
    fun testFailureResultWithAuthError() {
        val result = AuthResult.Failure(AuthError.INVALID_CREDENTIALS, AuthType.PIN)
        assertTrue(result is AuthResult.Failure)
        assertEquals(AuthError.INVALID_CREDENTIALS.message, (result as AuthResult.Failure).error)
        assertEquals(AuthError.INVALID_CREDENTIALS.code, (result as AuthResult.Failure).errorCode)
        assertEquals(AuthType.PIN, (result as AuthResult.Failure).authType)
    }

    @Test
    fun testCancelledResult() {
        val result = AuthResult.Cancelled
        assertTrue(result is AuthResult.Cancelled)
    }

    @Test
    fun testNotAvailableResult() {
        val result = AuthResult.NotAvailable(AuthType.FINGERPRINT)
        assertTrue(result is AuthResult.NotAvailable)
        assertEquals(AuthType.FINGERPRINT, (result as AuthResult.NotAvailable).authType)
    }

    @Test
    fun testNotAvailableResultWithoutAuthType() {
        val result = AuthResult.NotAvailable(null)
        assertTrue(result is AuthResult.NotAvailable)
        assertNull((result as AuthResult.NotAvailable).authType)
    }
}
