@file:UseSerializers(InstantIsoSerializer::class)

package com.freighttiger.driverassistant.core.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.Instant

@Serializable
data class DriverProfile(
    val driverId: String,
    val displayName: String,
    /** Masked for display, e.g. "******3210". The full number is not stored by the app. */
    val maskedPhone: String,
    val preferredLanguage: String = "hi-IN",
)

@Serializable
data class DriverSession(
    val profile: DriverProfile,
    val authenticatedAt: Instant,
    /** True when produced by the demo OTP flow. */
    val simulated: Boolean,
)

/** Languages the assistant can speak/understand. Only Hindi is enabled in the MVP. */
enum class AssistantLanguage(val tag: String, val enabled: Boolean) {
    HINDI("hi-IN", true),
    ENGLISH_INDIA("en-IN", false),
    MARATHI("mr-IN", false),
    TAMIL("ta-IN", false),
    TELUGU("te-IN", false),
    BENGALI("bn-IN", false),
}
