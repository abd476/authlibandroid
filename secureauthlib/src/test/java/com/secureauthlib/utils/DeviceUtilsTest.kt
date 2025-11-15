package com.secureauthlib.utils

import android.content.Context
import com.secureauthlib.AuthType
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*

/**
 * Tests for DeviceUtils
 * Note: These are basic structure tests. Full device capability tests require Android instrumentation.
 */
class DeviceUtilsTest {

    @Test
    fun testGetFirstAvailableMethod() {
        // This test verifies the method structure
        // Actual device capability testing requires Android instrumentation
        val context = mock(Context::class.java)
        val methods = listOf(AuthType.PIN, AuthType.PASSWORD)
        
        // PIN and PASSWORD should always be available
        val result = DeviceUtils.getFirstAvailableMethod(context, methods)
        assertNotNull(result)
        assertTrue(result == AuthType.PIN || result == AuthType.PASSWORD)
    }

    @Test
    fun testGetAvailableMethods() {
        val context = mock(Context::class.java)
        val requestedMethods = listOf(AuthType.PIN, AuthType.PASSWORD, AuthType.PATTERN)
        
        val available = DeviceUtils.getAvailableMethods(context, requestedMethods)
        
        // PIN, PASSWORD, and PATTERN should always be available
        assertTrue(available.contains(AuthType.PIN))
        assertTrue(available.contains(AuthType.PASSWORD))
        assertTrue(available.contains(AuthType.PATTERN))
    }

    @Test
    fun testGetBiometricError() {
        val context = mock(Context::class.java)
        
        // Test that the method exists and returns an AuthError
        val error = DeviceUtils.getBiometricError(context)
        assertNotNull(error)
        assertTrue(error.code >= 1001 && error.code <= 1003)
    }
}
