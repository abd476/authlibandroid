package com.secureauthlib.core

import android.content.Context
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import com.secureauthlib.AuthResult
import com.secureauthlib.AuthType
import com.secureauthlib.models.AuthError
import com.secureauthlib.utils.AuthLogger
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Handler for pattern authentication
 * Note: This is a simplified pattern implementation using a PIN-like interface
 * For a full pattern lock UI, developers should provide a custom view
 */
internal class PatternHandler(
    private val context: Context,
    private val customPatternView: View? = null
) {

    /**
     * Authenticate using pattern
     * @param expectedPattern The expected pattern (stored as hash or encrypted)
     * @param title Dialog title
     * @param subtitle Dialog subtitle
     */
    suspend fun authenticate(
        expectedPattern: String,
        title: String = "Pattern Authentication",
        subtitle: String = "Enter your pattern"
    ): AuthResult = suspendCancellableCoroutine { continuation ->

        AuthLogger.debug("Starting pattern authentication")

        // If custom view is provided, use it
        if (customPatternView != null) {
            showCustomPatternDialog(customPatternView, expectedPattern, title, subtitle, continuation)
            return@suspendCancellableCoroutine
        }

        // Default implementation: use PIN-like input for pattern
        // In a real implementation, you'd use a PatternLockView library
        val input = EditText(context).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = "Enter pattern (e.g., 1-2-3-4-5)"
            setPadding(32, 16, 32, 16)
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 8)
            addView(input)
        }

        val dialog = AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog)
            .setTitle(title)
            .setMessage(subtitle)
            .setView(container)
            .setPositiveButton("OK") { _, _ ->
                val enteredPattern = input.text.toString()
                if (enteredPattern == expectedPattern) {
                    AuthLogger.info("Pattern authentication successful")
                    continuation.resume(AuthResult.Success(authType = AuthType.PATTERN))
                } else {
                    AuthLogger.warn("Pattern authentication failed")
                    continuation.resume(
                        AuthResult.Failure(
                            AuthError.INVALID_CREDENTIALS,
                            AuthType.PATTERN
                        )
                    )
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                AuthLogger.debug("Pattern authentication cancelled")
                continuation.resume(AuthResult.Cancelled)
            }
            .setOnCancelListener {
                continuation.resume(AuthResult.Cancelled)
            }
            .create()

        continuation.invokeOnCancellation {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showCustomPatternDialog(
        patternView: View,
        expectedPattern: String,
        title: String,
        subtitle: String,
        continuation: kotlin.coroutines.Continuation<AuthResult>
    ) {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 8)
            addView(patternView)
        }

        val dialog = AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog)
            .setTitle(title)
            .setMessage(subtitle)
            .setView(container)
            .setPositiveButton("OK", null)
            .setNegativeButton("Cancel") { _, _ ->
                continuation.resume(AuthResult.Cancelled)
            }
            .setOnCancelListener {
                continuation.resume(AuthResult.Cancelled)
            }
            .create()

        // Note: For custom pattern views, the developer should handle
        // pattern completion and call a callback
        // This is a simplified implementation
        dialog.show()
    }
}
