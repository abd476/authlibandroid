package com.secureauthlib.models

import com.secureauthlib.AuthType
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests for AuthConfig
 */
class AuthConfigTest {

    @Test
    fun testDefaultConfig() {
        val config = AuthConfig()
        assertEquals(3, config.methods.size)
        assertTrue(config.methods.contains(AuthType.FINGERPRINT))
        assertTrue(config.methods.contains(AuthType.FACE))
        assertTrue(config.methods.contains(AuthType.PIN))
        assertTrue(config.enableSessionToken)
        assertEquals(0L, config.sessionTimeoutMs)
        assertEquals(3, config.maxRetries)
        assertEquals(30000L, config.cooldownPeriodMs)
        assertFalse(config.requireChainedAuth)
    }

    @Test
    fun testConfigBuilder() {
        val config = authConfig {
            methods = listOf(AuthType.PIN, AuthType.PASSWORD)
            title = "Custom Title"
            subtitle = "Custom Subtitle"
            maxRetries = 5
            sessionTimeoutMs = 3600000L
            enableSessionToken = false
        }

        assertEquals(2, config.methods.size)
        assertEquals("Custom Title", config.title)
        assertEquals("Custom Subtitle", config.subtitle)
        assertEquals(5, config.maxRetries)
        assertEquals(3600000L, config.sessionTimeoutMs)
        assertFalse(config.enableSessionToken)
    }

    @Test
    fun testChainedAuthConfig() {
        val config = authConfig {
            requireChainedAuth = true
            chainedMethods = listOf(AuthType.FINGERPRINT, AuthType.PIN)
        }

        assertTrue(config.requireChainedAuth)
        assertEquals(2, config.chainedMethods.size)
        assertTrue(config.chainedMethods.contains(AuthType.FINGERPRINT))
        assertTrue(config.chainedMethods.contains(AuthType.PIN))
    }

    @Test
    fun testInactivityTimeout() {
        val config = authConfig {
            inactivityTimeoutMs = 1800000L // 30 minutes
        }

        assertEquals(1800000L, config.inactivityTimeoutMs)
    }
}
