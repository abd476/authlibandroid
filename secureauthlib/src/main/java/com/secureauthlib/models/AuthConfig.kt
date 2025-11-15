package com.secureauthlib.models

import com.secureauthlib.AuthType

/**
 * Configuration for authentication
 */
data class AuthConfig(
    /**
     * List of authentication methods to try in order
     */
    val methods: List<AuthType> = listOf(AuthType.FINGERPRINT, AuthType.FACE, AuthType.PIN),
    
    /**
     * Title for authentication prompt
     */
    val title: String? = null,
    
    /**
     * Subtitle for authentication prompt
     */
    val subtitle: String? = null,
    
    /**
     * Negative button text
     */
    val negativeButtonText: String = "Cancel",
    
    /**
     * Enable session token storage
     */
    val enableSessionToken: Boolean = true,
    
    /**
     * Session timeout in milliseconds (0 = no timeout)
     */
    val sessionTimeoutMs: Long = 0L,
    
    /**
     * Maximum retry attempts before failing
     */
    val maxRetries: Int = 3,
    
    /**
     * Cooldown period in milliseconds after max retries
     */
    val cooldownPeriodMs: Long = 30000L,
    
    /**
     * Require multiple authentication methods (chained authentication)
     */
    val requireChainedAuth: Boolean = false,
    
    /**
     * List of methods required for chained authentication
     */
    val chainedMethods: List<AuthType> = emptyList(),
    
    /**
     * Inactivity timeout in milliseconds (0 = no timeout)
     */
    val inactivityTimeoutMs: Long = 0L
)

/**
 * Builder for AuthConfig
 */
class AuthConfigBuilder {
    private var methods: List<AuthType> = listOf(AuthType.FINGERPRINT, AuthType.FACE, AuthType.PIN)
    private var title: String? = null
    private var subtitle: String? = null
    private var negativeButtonText: String = "Cancel"
    private var enableSessionToken: Boolean = true
    private var sessionTimeoutMs: Long = 0L
    private var maxRetries: Int = 3
    private var cooldownPeriodMs: Long = 30000L
    private var requireChainedAuth: Boolean = false
    private var chainedMethods: List<AuthType> = emptyList()
    private var inactivityTimeoutMs: Long = 0L

    fun methods(methods: List<AuthType>) = apply { this.methods = methods }
    fun title(title: String) = apply { this.title = title }
    fun subtitle(subtitle: String) = apply { this.subtitle = subtitle }
    fun negativeButtonText(text: String) = apply { this.negativeButtonText = text }
    fun enableSessionToken(enable: Boolean) = apply { this.enableSessionToken = enable }
    fun sessionTimeoutMs(timeout: Long) = apply { this.sessionTimeoutMs = timeout }
    fun maxRetries(retries: Int) = apply { this.maxRetries = retries }
    fun cooldownPeriodMs(period: Long) = apply { this.cooldownPeriodMs = period }
    fun requireChainedAuth(require: Boolean) = apply { this.requireChainedAuth = require }
    fun chainedMethods(methods: List<AuthType>) = apply { this.chainedMethods = methods }
    fun inactivityTimeoutMs(timeout: Long) = apply { this.inactivityTimeoutMs = timeout }

    fun build() = AuthConfig(
        methods = methods,
        title = title,
        subtitle = subtitle,
        negativeButtonText = negativeButtonText,
        enableSessionToken = enableSessionToken,
        sessionTimeoutMs = sessionTimeoutMs,
        maxRetries = maxRetries,
        cooldownPeriodMs = cooldownPeriodMs,
        requireChainedAuth = requireChainedAuth,
        chainedMethods = chainedMethods,
        inactivityTimeoutMs = inactivityTimeoutMs
    )
}

fun authConfig(block: AuthConfigBuilder.() -> Unit): AuthConfig {
    return AuthConfigBuilder().apply(block).build()
}
