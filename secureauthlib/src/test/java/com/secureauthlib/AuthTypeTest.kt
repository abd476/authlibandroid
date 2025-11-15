package com.secureauthlib

import org.junit.Test
import org.junit.Assert.*

/**
 * Tests for AuthType enum
 */
class AuthTypeTest {

    @Test
    fun testAuthTypeValues() {
        val types = AuthType.values()
        assertEquals(5, types.size)
        assertTrue(types.contains(AuthType.FINGERPRINT))
        assertTrue(types.contains(AuthType.FACE))
        assertTrue(types.contains(AuthType.PIN))
        assertTrue(types.contains(AuthType.PATTERN))
        assertTrue(types.contains(AuthType.PASSWORD))
    }

    @Test
    fun testAuthTypeValueOf() {
        assertEquals(AuthType.FINGERPRINT, AuthType.valueOf("FINGERPRINT"))
        assertEquals(AuthType.FACE, AuthType.valueOf("FACE"))
        assertEquals(AuthType.PIN, AuthType.valueOf("PIN"))
        assertEquals(AuthType.PATTERN, AuthType.valueOf("PATTERN"))
        assertEquals(AuthType.PASSWORD, AuthType.valueOf("PASSWORD"))
    }
}
