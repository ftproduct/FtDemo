package com.freighttiger.driverassistant.domain.voice

import kotlin.math.roundToInt

data class EtaEstimate(val minutes: Int, val approximate: Boolean)

/**
 * Parses spoken Hindi durations ("ek ghanta", "do ghante", "dedh ghanta", "aadha ghanta",
 * "sava do ghante", "45 minute", "do teen ghante") from normalised text.
 *
 * Returns null when no duration can be extracted reliably (e.g. "thodi der", "kal subah").
 * The caller must then ask the driver to clarify rather than guess.
 */
class EtaParser(private val maxMinutes: Int = 72 * 60) {

    fun parse(normalizedText: String): EtaEstimate? {
        val tokens = normalizedText.split(' ').filter { it.isNotBlank() }
        var total = 0.0
        var found = false
        var approximate = false

        for (idx in tokens.indices) {
            val unitMinutes = when (tokens[idx]) {
                "ghanta" -> 60
                "minute" -> 1
                else -> continue
            }
            var quantity: Double? = null
            var cursor = idx - 1

            val n1 = tokens.getOrNull(cursor)?.let(NumberWords::valueOf)
            if (n1 != null) {
                quantity = n1.toDouble()
                cursor--
                val n0 = tokens.getOrNull(cursor)?.let(NumberWords::valueOf)
                if (n0 != null && n0 < n1) {
                    // Range such as "do teen ghante": report the upper bound, flagged approximate.
                    approximate = true
                    cursor--
                }
            }
            when (tokens.getOrNull(cursor)) {
                "sava" -> quantity = (quantity ?: 1.0) + 0.25
                "paune" -> quantity = (quantity ?: 1.0) - 0.25
                "saadhe" -> quantity = quantity?.plus(0.5)
            }
            if (quantity == null) {
                quantity = when (tokens.getOrNull(idx - 1)) {
                    "aadha" -> 0.5
                    "dedh" -> 1.5
                    "dhai" -> 2.5
                    else -> null
                }
            }
            if (quantity == null) {
                if (unitMinutes == 60) {
                    // Bare "ghanta lagega" — about an hour.
                    quantity = 1.0
                    approximate = true
                } else {
                    continue
                }
            }
            total += quantity * unitMinutes
            found = true
        }

        if (!found) return null
        val minutes = total.roundToInt()
        if (minutes <= 0 || minutes > maxMinutes) return null
        return EtaEstimate(minutes, approximate)
    }
}

internal object NumberWords {
    private val words = mapOf(
        "ek" to 1, "do" to 2, "teen" to 3, "char" to 4, "paanch" to 5, "chhe" to 6, "saat" to 7,
        "aath" to 8, "nau" to 9, "das" to 10, "gyarah" to 11, "barah" to 12, "terah" to 13,
        "chaudah" to 14, "pandrah" to 15, "solah" to 16, "satrah" to 17, "atharah" to 18,
        "unnis" to 19, "bees" to 20, "pachchis" to 25, "pachis" to 25, "tees" to 30,
        "paintees" to 35, "chalis" to 40, "chalees" to 40, "paintalis" to 45, "pachas" to 50,
        "pachaas" to 50,
        "one" to 1, "two" to 2, "three" to 3, "four" to 4, "five" to 5, "ten" to 10,
        "fifteen" to 15, "twenty" to 20, "thirty" to 30, "forty" to 40, "fifty" to 50,
    )

    fun valueOf(token: String): Int? = token.toIntOrNull()?.takeIf { it in 0..10_000 } ?: words[token]
}
