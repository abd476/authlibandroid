package com.secureauthlib

/**
 * Test runner documentation
 * 
 * To run tests:
 * 
 * 1. Unit Tests (JVM):
 *    - Right-click on src/test/java folder
 *    - Select "Run 'Tests in secureauthlib'"
 *    - Or use: ./gradlew test
 * 
 * 2. Instrumentation Tests (Android):
 *    - Right-click on src/androidTest/java folder
 *    - Select "Run 'Tests in secureauthlib'"
 *    - Or use: ./gradlew connectedAndroidTest
 * 
 * Test Coverage:
 * - AuthType enum values and operations
 * - AuthResult all variants (Success, Failure, Cancelled, NotAvailable)
 * - AuthError all error codes and messages
 * - AuthConfig default values and builder pattern
 * - DeviceUtils method availability checks
 * 
 * Note: Full biometric testing requires:
 * - Device with fingerprint/face unlock enrolled
 * - Emulator with biometric support enabled
 * - Proper test environment setup
 */
