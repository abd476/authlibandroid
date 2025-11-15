# SecureAuthLib

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/abd476/authlibandroid)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![Min SDK](https://img.shields.io/badge/min%20sdk-24-orange.svg)]()

A modern, developer-friendly biometric authentication library for Android. Supports fingerprint, face, PIN, pattern, and password login with a simple, unified API. Features customizable UI, coroutine-friendly async handling, secure token storage, intelligent fallbacks, and session management.

## Version

Current Version: **1.0.0**

See [CHANGELOG.md](CHANGELOG.md) for version history.

## Features

- Multiple Authentication Types: Fingerprint, Face, PIN, and Password
- Unified API: Single interface for all authentication methods
- Coroutine Support: Built with Kotlin coroutines for async handling
- Secure Storage: Encrypted token storage using AndroidX Security Crypto
- Intelligent Fallbacks: Automatic fallback to alternative authentication methods
- Customizable UI: Customize authentication prompts and dialogs
- Easy Integration: Simple setup and usage

## Requirements

- Android API Level 24 (Android 7.0) or higher
- Kotlin coroutines support
- FragmentActivity for biometric authentication

## Installation

### Option 1: JitPack (Recommended)

1. Add JitPack repository to your project's `build.gradle.kts` (or `build.gradle`):
```kotlin
allprojects {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}
```

2. Add the dependency in your app's `build.gradle.kts`:
```kotlin
dependencies {
    implementation("com.github.YourUsername:authlibandroid:1.0.0")
    // Replace YourUsername with your GitHub username
}
```

### Option 2: Local Module

1. Include the library module in your `settings.gradle.kts`:
```kotlin
include(":secureauthlib")
```

2. Add the dependency in your app's `build.gradle.kts`:
```kotlin
dependencies {
    implementation(project(":secureauthlib"))
}
```

## Usage

### Basic Setup

Create an instance of `AuthManager` in your Activity or Fragment:

```kotlin
import com.secureauthlib.AuthManager
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {
    private lateinit var authManager: AuthManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authManager = AuthManager.create(this, this)
    }
}
```

### Fingerprint Authentication

```kotlin
import com.secureauthlib.AuthType
import com.secureauthlib.AuthResult
import kotlinx.coroutines.launch

lifecycleScope.launch {
    val result = authManager.authenticate(AuthType.FINGERPRINT)
    
    when (result) {
        is AuthResult.Success -> {
            // Authentication successful
        }
        is AuthResult.Failure -> {
            // Handle error: result.error
        }
        is AuthResult.Cancelled -> {
            // User cancelled
        }
        is AuthResult.NotAvailable -> {
            // Fingerprint not available on this device
        }
    }
}
```

### Face Authentication

```kotlin
lifecycleScope.launch {
    val result = authManager.authenticate(AuthType.FACE)
    // Handle result same as fingerprint
}
```

### PIN Authentication

```kotlin
lifecycleScope.launch {
    val result = authManager.authenticate(
        authType = AuthType.PIN,
        expectedValue = "1234" // The expected PIN
    )
    // Handle result
}
```

### Password Authentication

```kotlin
lifecycleScope.launch {
    val result = authManager.authenticate(
        authType = AuthType.PASSWORD,
        expectedValue = "your_password"
    )
    // Handle result
}
```

### Customizing Authentication Prompts

You can customize the title, subtitle, and button text for authentication prompts:

```kotlin
lifecycleScope.launch {
    val result = authManager.authenticate(
        authType = AuthType.FINGERPRINT,
        title = "Verify Identity",
        subtitle = "Place your finger on the sensor",
        negativeButtonText = "Cancel"
    )
}
```

### Authentication with Fallback

Automatically fallback to an alternative authentication method if the primary method is unavailable:

```kotlin
lifecycleScope.launch {
    val result = authManager.authenticateWithFallback(
        primaryAuthType = AuthType.FINGERPRINT,
        fallbackAuthType = AuthType.PIN,
        expectedValue = "1234"
    )
    // Will try fingerprint first, then PIN if fingerprint is unavailable
}
```

### Checking Authentication Availability

Check if a specific authentication type is available before attempting authentication:

```kotlin
lifecycleScope.launch {
    val isAvailable = authManager.isAuthTypeAvailable(AuthType.FINGERPRINT)
    if (isAvailable) {
        // Proceed with fingerprint authentication
    } else {
        // Use alternative authentication method
    }
}
```

### Secure Token Storage

Store and retrieve authentication tokens securely:

```kotlin
// Save a token
lifecycleScope.launch {
    val success = authManager.saveToken("user_token", "your_secure_token")
    if (success) {
        // Token saved successfully
    }
}

// Retrieve a token
lifecycleScope.launch {
    val token = authManager.getToken("user_token")
    // Use the token
}

// Delete a token
lifecycleScope.launch {
    authManager.deleteToken("user_token")
}

// Clear all tokens
lifecycleScope.launch {
    authManager.clearAllTokens()
}
```

## API Reference

### AuthManager

Main class for handling authentication operations.

#### Methods

- `create(context: Context, activity: FragmentActivity): AuthManager` - Create an instance of AuthManager
- `authenticate(authType: AuthType, expectedValue: String? = null, ...): AuthResult` - Authenticate using the specified method
- `authenticateWithFallback(primaryAuthType: AuthType, fallbackAuthType: AuthType, expectedValue: String? = null): AuthResult` - Authenticate with automatic fallback
- `isAuthTypeAvailable(authType: AuthType): Boolean` - Check if authentication type is available
- `saveToken(key: String, token: String): Boolean` - Save a token securely
- `getToken(key: String): String?` - Retrieve a stored token
- `deleteToken(key: String): Boolean` - Delete a stored token
- `clearAllTokens(): Boolean` - Clear all stored tokens

### AuthType

Enum representing available authentication types:
- `FINGERPRINT` - Fingerprint authentication
- `FACE` - Face recognition authentication
- `PIN` - PIN code authentication
- `PASSWORD` - Password authentication

### AuthResult

Sealed class representing authentication results:
- `Success(token: String?)` - Authentication successful
- `Failure(error: String, errorCode: Int)` - Authentication failed
- `Cancelled` - Authentication cancelled by user
- `NotAvailable` - Authentication type not available

## Best Practices

1. Always check authentication availability before attempting authentication
2. Provide fallback options for users when biometric authentication is unavailable
3. Store sensitive tokens securely using the provided token storage methods
4. Handle all authentication result cases appropriately
5. Use coroutines properly with lifecycle-aware scopes

## Demo App

The project includes a demo app that demonstrates all authentication methods. Run the app to see:
- Fingerprint authentication
- Face authentication
- PIN authentication
- Password authentication
- Biometric with PIN fallback

## License

This library is provided as-is for use in your projects.

## Contributing

Contributions are welcome. Please ensure your code follows Android best practices and includes appropriate tests.
