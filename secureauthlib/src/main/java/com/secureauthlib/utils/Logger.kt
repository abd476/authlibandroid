package com.secureauthlib.utils

import android.util.Log

/**
 * Optional logger for debugging and analytics
 */
object AuthLogger {
    private const val TAG = "SecureAuthLib"
    private var isEnabled = false
    private var logLevel: LogLevel = LogLevel.INFO

    enum class LogLevel {
        VERBOSE, DEBUG, INFO, WARN, ERROR
    }

    /**
     * Enable or disable logging
     */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    /**
     * Set the log level
     */
    fun setLogLevel(level: LogLevel) {
        logLevel = level
    }

    fun verbose(message: String, throwable: Throwable? = null) {
        if (isEnabled && logLevel <= LogLevel.VERBOSE) {
            Log.v(TAG, message, throwable)
        }
    }

    fun debug(message: String, throwable: Throwable? = null) {
        if (isEnabled && logLevel <= LogLevel.DEBUG) {
            Log.d(TAG, message, throwable)
        }
    }

    fun info(message: String, throwable: Throwable? = null) {
        if (isEnabled && logLevel <= LogLevel.INFO) {
            Log.i(TAG, message, throwable)
        }
    }

    fun warn(message: String, throwable: Throwable? = null) {
        if (isEnabled && logLevel <= LogLevel.WARN) {
            Log.w(TAG, message, throwable)
        }
    }

    fun error(message: String, throwable: Throwable? = null) {
        if (isEnabled && logLevel <= LogLevel.ERROR) {
            Log.e(TAG, message, throwable)
        }
    }
}
