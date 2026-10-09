package com.freighttiger.driverassistant

import com.freighttiger.driverassistant.core.network.ApiConfig

/** Build-time environment configuration. Values come from Gradle properties / env vars, never source. */
data class AppConfig(
    /** When true the app talks ONLY to the in-process simulated backend and labels everything as demo. */
    val demoMode: Boolean,
    val api: ApiConfig,
    /** Verified support number, or null. The app never invents a number. */
    val supportPhoneNumber: String?,
    val versionName: String,
) {
    companion object {
        fun fromBuildConfig() = AppConfig(
            demoMode = BuildConfig.DEMO_MODE,
            api = ApiConfig(
                baseUrl = BuildConfig.API_BASE_URL,
                environment = BuildConfig.API_ENVIRONMENT,
                clientVersion = BuildConfig.VERSION_NAME,
                allowCleartext = BuildConfig.ALLOW_CLEARTEXT,
            ),
            supportPhoneNumber = BuildConfig.SUPPORT_PHONE_NUMBER.takeIf { it.isNotBlank() },
            versionName = BuildConfig.VERSION_NAME,
        )
    }
}
