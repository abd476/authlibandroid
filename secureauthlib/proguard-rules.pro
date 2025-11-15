# ProGuard rules for SecureAuthLib library module
# These rules apply when building the library itself

# Keep all public API classes
-keep class com.secureauthlib.** { *; }

# Keep AuthManager public API
-keep class com.secureauthlib.AuthManager {
    public *;
}

# Keep AuthResult and all its implementations
-keep class com.secureauthlib.AuthResult { *; }
-keep class com.secureauthlib.AuthResult$* { *; }

# Keep AuthType enum
-keep enum com.secureauthlib.AuthType { *; }

# AndroidX Security Crypto
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# AndroidX Biometric
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# Kotlin Coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Kotlin metadata
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}