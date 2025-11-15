# Consumer ProGuard rules for SecureAuthLib
# These rules are automatically applied to apps that use this library

# Keep all public API classes
-keep class com.secureauthlib.** { *; }
-keepclassmembers class com.secureauthlib.** { *; }

# Keep AuthManager and its public methods
-keep class com.secureauthlib.AuthManager { *; }
-keep class com.secureauthlib.AuthManager$* { *; }

# Keep AuthResult sealed class and all its subclasses
-keep class com.secureauthlib.AuthResult { *; }
-keep class com.secureauthlib.AuthResult$* { *; }

# Keep AuthType enum
-keep enum com.secureauthlib.AuthType { *; }

# AndroidX Security Crypto - EncryptedSharedPreferences
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# Keep MasterKey classes
-keep class androidx.security.crypto.MasterKey { *; }
-keep class androidx.security.crypto.MasterKey$* { *; }

# AndroidX Biometric
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# Keep BiometricPrompt and related classes
-keep class androidx.biometric.BiometricPrompt { *; }
-keep class androidx.biometric.BiometricPrompt$* { *; }
-keep class androidx.biometric.BiometricManager { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Kotlin metadata
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep Kotlin suspend functions
-keepclassmembers class * {
    *** suspend*(...);
}

# Keep Parcelable implementations
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep R class
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Don't warn about missing classes that are optional
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn kotlin.**
-dontwarn kotlinx.**
-dontwarn org.jetbrains.annotations.**