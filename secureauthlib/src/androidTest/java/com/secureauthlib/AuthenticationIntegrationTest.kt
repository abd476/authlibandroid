package com.secureauthlib

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.secureauthlib.models.AuthConfig
import com.secureauthlib.models.AuthError
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.fragment.app.FragmentActivity
import org.junit.Assert.*

/**
 * Integration tests for authentication
 * These tests require a device or emulator with biometric capabilities
 */
@RunWith(AndroidJUnit4::class)
class AuthenticationIntegrationTest {

    private lateinit var context: android.content.Context
    private lateinit var activity: FragmentActivity

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        // Note: In real tests, you'd need to get the actual Activity
        // For now, these are structure tests
    }

    @Test
    fun testAuthTypeEnum() {
        val types = AuthType.values()
        assertTrue(types.isNotEmpty())
        assertTrue(types.contains(AuthType.FINGERPRINT))
        assertTrue(types.contains(AuthType.FACE))
        assertTrue(types.contains(AuthType.PIN))
        assertTrue(types.contains(AuthType.PATTERN))
        assertTrue(types.contains(AuthType.PASSWORD))
    }

    @Test
    fun testAuthConfigCreation() {
        val config = AuthConfig(
            methods = listOf(AuthType.PIN, AuthType.PASSWORD),
            title = "Test Auth",
            maxRetries = 3
        )
        
        assertEquals(2, config.methods.size)
        assertEquals("Test Auth", config.title)
        assertEquals(3, config.maxRetries)
    }

    @Test
    fun testAuthConfigBuilder() {
        val config = authConfig {
            methods = listOf(AuthType.FINGERPRINT, AuthType.FACE)
            title = "Biometric Auth"
            maxRetries = 5
            sessionTimeoutMs = 3600000L
        }
        
        assertEquals(2, config.methods.size)
        assertEquals("Biometric Auth", config.title)
        assertEquals(5, config.maxRetries)
        assertEquals(3600000L, config.sessionTimeoutMs)
    }

    @Test
    fun testAuthErrorCodes() {
        assertEquals(1001, AuthError.BIOMETRIC_UNAVAILABLE.code)
        assertEquals(1002, AuthError.BIOMETRIC_NOT_ENROLLED.code)
        assertEquals(2001, AuthError.AUTHENTICATION_FAILED.code)
        assertEquals(2003, AuthError.INVALID_CREDENTIALS.code)
        assertEquals(2004, AuthError.MAX_RETRIES_EXCEEDED.code)
        assertEquals(3001, AuthError.SESSION_EXPIRED.code)
    }

    @Test
    fun testAuthResultSuccess() {
        val result = AuthResult.Success(
            token = "test_token_123",
            authType = AuthType.PIN
        )
        
        assertTrue(result is AuthResult.Success)
        assertEquals("test_token_123", (result as AuthResult.Success).token)
        assertEquals(AuthType.PIN, (result as AuthResult.Success).authType)
    }

    @Test
    fun testAuthResultFailure() {
        val result = AuthResult.Failure(
            AuthError.INVALID_CREDENTIALS,
            AuthType.PASSWORD
        )
        
        assertTrue(result is AuthResult.Failure)
        assertEquals(AuthError.INVALID_CREDENTIALS.code, (result as AuthResult.Failure).errorCode)
        assertEquals(AuthType.PASSWORD, (result as AuthResult.Failure).authType)
    }

    @Test
    fun testAuthResultCancelled() {
        val result = AuthResult.Cancelled
        assertTrue(result is AuthResult.Cancelled)
    }

    @Test
    fun testAuthResultNotAvailable() {
        val result = AuthResult.NotAvailable(AuthType.FACE)
        assertTrue(result is AuthResult.NotAvailable)
        assertEquals(AuthType.FACE, (result as AuthResult.NotAvailable).authType)
    }
}
