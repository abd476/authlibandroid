package com.secureauthlib

import android.app.Activity
import android.content.Context
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Helper class for handling PIN and password authentication
 */
internal class PinPasswordAuthHelper(
    private val context: Context
) {
    
    private fun getActivityContext(): Activity? {
        return when (context) {
            is Activity -> context
            else -> null
        }
    }

    /**
     * Authenticate using PIN
     */
    suspend fun authenticateWithPin(
        title: String = "Enter PIN",
        message: String = "Please enter your PIN",
        expectedPin: String
    ): AuthResult = suspendCancellableCoroutine { continuation ->

        val activity = getActivityContext()
        if (activity == null) {
            continuation.resume(
                AuthResult.Failure(
                    com.secureauthlib.models.AuthError.AUTHENTICATION_FAILED,
                    AuthType.PIN
                )
            )
            return@suspendCancellableCoroutine
        }

        val input = EditText(activity).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            hint = "Enter PIN"
            setPadding(32, 16, 32, 16)
        }

        val container = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 8)
            addView(input)
        }

        val dialog = AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog)
            .setTitle(title)
            .setMessage(message)
            .setView(container)
            .setPositiveButton("OK") { _, _ ->
                val enteredPin = input.text.toString()
                if (enteredPin == expectedPin) {
                    continuation.resume(AuthResult.Success(authType = AuthType.PIN))
                } else {
                    continuation.resume(
                        AuthResult.Failure(
                            com.secureauthlib.models.AuthError.INVALID_CREDENTIALS,
                            AuthType.PIN
                        )
                    )
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
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

    /**
     * Authenticate using password
     */
    suspend fun authenticateWithPassword(
        title: String = "Enter Password",
        message: String = "Please enter your password",
        expectedPassword: String
    ): AuthResult = suspendCancellableCoroutine { continuation ->

        val activity = getActivityContext()
        if (activity == null) {
            continuation.resume(
                AuthResult.Failure(
                    com.secureauthlib.models.AuthError.AUTHENTICATION_FAILED,
                    AuthType.PASSWORD
                )
            )
            return@suspendCancellableCoroutine
        }

        val input = EditText(activity).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = "Enter Password"
            setPadding(32, 16, 32, 16)
        }

        val container = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 8)
            addView(input)
        }

        val dialog = AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog)
            .setTitle(title)
            .setMessage(message)
            .setView(container)
            .setPositiveButton("OK") { _, _ ->
                val enteredPassword = input.text.toString()
                if (enteredPassword == expectedPassword) {
                    continuation.resume(AuthResult.Success(authType = AuthType.PASSWORD))
                } else {
                    continuation.resume(
                        AuthResult.Failure(
                            com.secureauthlib.models.AuthError.INVALID_CREDENTIALS,
                            AuthType.PASSWORD
                        )
                    )
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
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
}
