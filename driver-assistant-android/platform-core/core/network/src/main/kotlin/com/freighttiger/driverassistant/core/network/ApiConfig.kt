package com.freighttiger.driverassistant.core.network

/**
 * Environment-specific API configuration. Supplied at build time (Gradle property / env var),
 * never hard-coded. An empty [baseUrl] means "not configured" — the app then refuses to talk to
 * a backend (or uses the clearly-labelled demo backend when demo mode is enabled).
 */
data class ApiConfig(
    val baseUrl: String,
    val environment: String,
    val clientVersion: String,
    /** Include the recogniser transcript as consent evidence (matches the proposed contract). */
    val sendTranscriptEvidence: Boolean = true,
    /** Only for local development against an emulator-hosted server. Never enable in release. */
    val allowCleartext: Boolean = false,
    val connectTimeoutSeconds: Long = 15,
    val readTimeoutSeconds: Long = 20,
) {
    val isConfigured: Boolean
        get() = baseUrl.isNotBlank() && (baseUrl.startsWith("https://") || (allowCleartext && baseUrl.startsWith("http://")))

    /** Retrofit requires a trailing slash. */
    val normalizedBaseUrl: String get() = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

    companion object {
        const val SCHEMA_VERSION = "1.0"
    }
}
