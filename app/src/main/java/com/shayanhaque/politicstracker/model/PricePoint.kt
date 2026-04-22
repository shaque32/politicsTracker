package com.shayanhaque.politicstracker.model

/**
 * A single point on a market's historical price curve. Kept deliberately
 * minimal so the chart component stays trivial to drive from any source.
 */
data class PricePoint(
    val timestampMillis: Long,
    /** Probability in [0.0, 1.0] at this timestamp. */
    val probability: Double,
)
