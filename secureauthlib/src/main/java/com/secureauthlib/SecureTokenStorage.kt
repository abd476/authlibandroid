package com.secureauthlib

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.secureauthlib.utils.AuthLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Secure storage for authentication tokens using EncryptedSharedPreferences
 * Supports session management with timeouts
 */
internal class SecureTokenStorage(private val context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "secure_auth_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        private const val SESSION_TOKEN_KEY = "session_token"
        private const val SESSION_TIMESTAMP_KEY = "session_timestamp"
        private const val LAST_ACTIVITY_KEY = "last_activity"
    }

    suspend fun saveToken(key: String, token: String): Boolean = withContext(Dispatchers.IO) {
        try {
            sharedPreferences.edit().putString(key, token).apply()
            AuthLogger.debug("Token saved: $key")
            true
        } catch (e: Exception) {
            AuthLogger.error("Failed to save token: $key", e)
            false
        }
    }

    suspend fun getToken(key: String): String? = withContext(Dispatchers.IO) {
        try {
            sharedPreferences.getString(key, null)
        } catch (e: Exception) {
            AuthLogger.error("Failed to get token: $key", e)
            null
        }
    }

    suspend fun deleteToken(key: String): Boolean = withContext(Dispatchers.IO) {
        try {
            sharedPreferences.edit().remove(key).apply()
            AuthLogger.debug("Token deleted: $key")
            true
        } catch (e: Exception) {
            AuthLogger.error("Failed to delete token: $key", e)
            false
        }
    }

    suspend fun clearAllTokens(): Boolean = withContext(Dispatchers.IO) {
        try {
            sharedPreferences.edit().clear().apply()
            AuthLogger.debug("All tokens cleared")
            true
        } catch (e: Exception) {
            AuthLogger.error("Failed to clear tokens", e)
            false
        }
    }

    /**
     * Save session token with timestamp
     */
    suspend fun saveSessionToken(token: String, timeoutMs: Long = 0L): Boolean = withContext(Dispatchers.IO) {
        try {
            val timestamp = System.currentTimeMillis()
            sharedPreferences.edit()
                .putString(SESSION_TOKEN_KEY, token)
                .putLong(SESSION_TIMESTAMP_KEY, timestamp)
                .putLong(LAST_ACTIVITY_KEY, timestamp)
                .apply()
            AuthLogger.debug("Session token saved with timeout: ${timeoutMs}ms")
            true
        } catch (e: Exception) {
            AuthLogger.error("Failed to save session token", e)
            false
        }
    }

    /**
     * Get session token if valid (not expired)
     */
    suspend fun getSessionToken(timeoutMs: Long = 0L): String? = withContext(Dispatchers.IO) {
        try {
            val token = sharedPreferences.getString(SESSION_TOKEN_KEY, null) ?: return@withContext null
            val timestamp = sharedPreferences.getLong(SESSION_TIMESTAMP_KEY, 0L)
            
            if (timeoutMs > 0L) {
                val elapsed = System.currentTimeMillis() - timestamp
                if (elapsed > timeoutMs) {
                    AuthLogger.debug("Session token expired")
                    invalidateSession()
                    return@withContext null
                }
            }
            
            AuthLogger.debug("Session token retrieved")
            token
        } catch (e: Exception) {
            AuthLogger.error("Failed to get session token", e)
            null
        }
    }

    /**
     * Update last activity timestamp
     */
    suspend fun updateLastActivity(): Boolean = withContext(Dispatchers.IO) {
        try {
            sharedPreferences.edit().putLong(LAST_ACTIVITY_KEY, System.currentTimeMillis()).apply()
            true
        } catch (e: Exception) {
            AuthLogger.error("Failed to update last activity", e)
            false
        }
    }

    /**
     * Check if session is valid based on inactivity timeout
     */
    suspend fun isSessionValid(inactivityTimeoutMs: Long = 0L): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = sharedPreferences.getString(SESSION_TOKEN_KEY, null) ?: return@withContext false
            if (inactivityTimeoutMs <= 0L) return@withContext true
            
            val lastActivity = sharedPreferences.getLong(LAST_ACTIVITY_KEY, 0L)
            val elapsed = System.currentTimeMillis() - lastActivity
            val isValid = elapsed <= inactivityTimeoutMs
            
            if (!isValid) {
                AuthLogger.debug("Session invalidated due to inactivity")
                invalidateSession()
            }
            
            isValid
        } catch (e: Exception) {
            AuthLogger.error("Failed to check session validity", e)
            false
        }
    }

    /**
     * Invalidate session token
     */
    suspend fun invalidateSession(): Boolean = withContext(Dispatchers.IO) {
        try {
            sharedPreferences.edit()
                .remove(SESSION_TOKEN_KEY)
                .remove(SESSION_TIMESTAMP_KEY)
                .remove(LAST_ACTIVITY_KEY)
                .apply()
            AuthLogger.debug("Session invalidated")
            true
        } catch (e: Exception) {
            AuthLogger.error("Failed to invalidate session", e)
            false
        }
    }
}
