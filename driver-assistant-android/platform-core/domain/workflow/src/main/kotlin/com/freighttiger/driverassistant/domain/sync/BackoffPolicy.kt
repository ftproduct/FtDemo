package com.freighttiger.driverassistant.domain.sync

import java.time.Duration
import kotlin.random.Random

/** Bounded exponential backoff with jitter. */
data class BackoffPolicy(
    val initialDelay: Duration = Duration.ofSeconds(5),
    val multiplier: Double = 2.0,
    val maxDelay: Duration = Duration.ofMinutes(15),
    /** Fraction of the delay used as +/- random jitter. */
    val jitter: Double = 0.2,
    val maxAttempts: Int = 8,
) {
    /** Delay before retry number [attempt] (1-based: the delay after the first failure is attempt 1). */
    fun delayFor(attempt: Int, random: Random = Random.Default): Duration {
        require(attempt >= 1)
        val base = initialDelay.toMillis() * Math.pow(multiplier, (attempt - 1).toDouble())
        val capped = base.coerceAtMost(maxDelay.toMillis().toDouble())
        val spread = capped * jitter
        val jittered = capped + (random.nextDouble() * 2 - 1) * spread
        return Duration.ofMillis(jittered.toLong().coerceIn(0, maxDelay.toMillis()))
    }
}
