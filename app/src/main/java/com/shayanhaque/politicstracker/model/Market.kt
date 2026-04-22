package com.shayanhaque.politicstracker.model

/**
 * Domain model for a single politics prediction market (e.g. "Will the GOP
 * win the 2026 Senate majority?"). Intentionally decoupled from any network
 * DTO or Room entity — mapping happens at the data-layer boundary so that
 * ViewModels and UI code are never coupled to a specific backend shape.
 */
data class Market(
    val id: String,
    val title: String,
    val description: String,
    val category: MarketCategory,
    /** Current implied probability as a fraction in [0.0, 1.0]. */
    val probability: Double,
    /** 24h change in probability (e.g. +0.042 means +4.2 percentage points). */
    val dailyChange: Double,
    /** USD volume, or null if the source doesn't provide it. */
    val volumeUsd: Double?,
    /** UTC millis when this market closes; null if open-ended. */
    val closeDateMillis: Long?,
    /** Full resolution rules / market description body. */
    val rules: String,
    /** True if the market is currently accepting trades. */
    val isActive: Boolean,
    /** Whether this market is flagged as trending by the source. */
    val isTrending: Boolean,
) {
    val probabilityPercent: Int get() = (probability * 100).toInt().coerceIn(0, 100)
    val dailyChangePercent: Double get() = dailyChange * 100.0
}
