package com.secuirtyapp.authlibandroid

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.secureauthlib.AuthManager
import com.secureauthlib.AuthResult
import com.secureauthlib.AuthType
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var authManager: AuthManager
    private lateinit var btnFingerprint: MaterialButton
    private lateinit var btnFace: MaterialButton
    private lateinit var btnPin: MaterialButton
    private lateinit var btnPassword: MaterialButton
    private lateinit var btnFallback: MaterialButton
    private lateinit var btnSetupPin: MaterialButton
    private lateinit var btnSetupPassword: MaterialButton
    private lateinit var resultText: android.widget.TextView

    companion object {
        private const val PIN_KEY = "user_pin"
        private const val PASSWORD_KEY = "user_password"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        authManager = AuthManager.create(this, this)
        
        btnFingerprint = findViewById(R.id.btnFingerprint)
        btnFace = findViewById(R.id.btnFace)
        btnPin = findViewById(R.id.btnPin)
        btnPassword = findViewById(R.id.btnPassword)
        btnFallback = findViewById(R.id.btnFallback)
        btnSetupPin = findViewById(R.id.btnSetupPin)
        btnSetupPassword = findViewById(R.id.btnSetupPassword)
        resultText = findViewById(R.id.resultText)

        setupClickListeners()
        updateButtonStates()
    }

    private fun setupClickListeners() {
        btnFingerprint.setOnClickListener {
            authenticate(AuthType.FINGERPRINT)
        }

        btnFace.setOnClickListener {
            lifecycleScope.launch {
                // Check if face is available before attempting
                val isFaceAvailable = authManager.isAuthTypeAvailable(AuthType.FACE)
                if (!isFaceAvailable) {
                    showMessage("Face authentication is not available on this device")
                } else {
                    authenticate(AuthType.FACE)
                }
            }
        }

        btnPin.setOnClickListener {
            lifecycleScope.launch {
                val storedPin = authManager.getToken(PIN_KEY)
                if (storedPin == null) {
                    showMessage("Please setup PIN first")
                } else {
                    authenticate(AuthType.PIN, expectedValue = storedPin)
                }
            }
        }

        btnPassword.setOnClickListener {
            lifecycleScope.launch {
                val storedPassword = authManager.getToken(PASSWORD_KEY)
                if (storedPassword == null) {
                    showMessage("Please setup password first")
                } else {
                    authenticate(AuthType.PASSWORD, expectedValue = storedPassword)
                }
            }
        }

        btnFallback.setOnClickListener {
            lifecycleScope.launch {
                val storedPin = authManager.getToken(PIN_KEY)
                if (storedPin == null) {
                    showMessage("Please setup PIN first for fallback")
                } else {
                    authenticateWithFallback(storedPin)
                }
            }
        }

        btnSetupPin.setOnClickListener {
            showSetupPinDialog()
        }

        btnSetupPassword.setOnClickListener {
            showSetupPasswordDialog()
        }
    }

    private fun showSetupPinDialog() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            hint = "Enter PIN (4-8 digits)"
            setPadding(32, 16, 32, 16)
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 8)
            addView(input)
        }

        AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog)
            .setTitle("Setup PIN")
            .setMessage("Enter a PIN to use for authentication")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val pin = input.text.toString()
                if (pin.length in 4..8 && pin.all { it.isDigit() }) {
                    lifecycleScope.launch {
                        val success = authManager.saveToken(PIN_KEY, pin)
                        if (success) {
                            showMessage("PIN saved successfully")
                            updateButtonStates()
                        } else {
                            showMessage("Failed to save PIN")
                        }
                    }
                } else {
                    showMessage("PIN must be 4-8 digits")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSetupPasswordDialog() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = "Enter Password"
            setPadding(32, 16, 32, 16)
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 8)
            addView(input)
        }

        AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog)
            .setTitle("Setup Password")
            .setMessage("Enter a password to use for authentication")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val password = input.text.toString()
                if (password.length >= 4) {
                    lifecycleScope.launch {
                        val success = authManager.saveToken(PASSWORD_KEY, password)
                        if (success) {
                            showMessage("Password saved successfully")
                            updateButtonStates()
                        } else {
                            showMessage("Failed to save password")
                        }
                    }
                } else {
                    showMessage("Password must be at least 4 characters")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateButtonStates() {
        lifecycleScope.launch {
            val hasPin = authManager.getToken(PIN_KEY) != null
            val hasPassword = authManager.getToken(PASSWORD_KEY) != null

            btnPin.isEnabled = hasPin
            btnPassword.isEnabled = hasPassword
            btnFallback.isEnabled = hasPin

            if (!hasPin) {
                btnPin.text = "PIN Authentication (Setup Required)"
            } else {
                btnPin.text = "PIN Authentication"
            }

            if (!hasPassword) {
                btnPassword.text = "Password Authentication (Setup Required)"
            } else {
                btnPassword.text = "Password Authentication"
            }
        }
    }

    private fun authenticate(authType: AuthType, expectedValue: String? = null) {
        lifecycleScope.launch {
            resultText.text = "Authenticating..."
            val result = authManager.authenticate(authType, expectedValue)
            handleAuthResult(result, authType)
        }
    }

    private fun authenticateWithFallback(pin: String) {
        lifecycleScope.launch {
            resultText.text = "Trying biometric authentication..."
            val result = authManager.authenticateWithFallback(
                primaryAuthType = AuthType.FINGERPRINT,
                fallbackAuthType = AuthType.PIN,
                expectedValue = pin
            )
            handleAuthResult(result, AuthType.FINGERPRINT)
        }
    }

    private fun handleAuthResult(result: AuthResult, authType: AuthType) {
        val message = when (result) {
            is AuthResult.Success -> {
                "Authentication successful!"
            }
            is AuthResult.Failure -> {
                "Authentication failed: ${result.error}"
            }
            is AuthResult.Cancelled -> {
                "Authentication cancelled"
            }
            is AuthResult.NotAvailable -> {
                "${authType.name} authentication is not available on this device"
            }
        }

        resultText.text = message
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showMessage(message: String) {
        resultText.text = message
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}