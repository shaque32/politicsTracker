package com.shayanhaque.politicstracker.model

/**
 * Coarse category used for filter chips on the home screen. Keeping this as
 * an enum (instead of a free-form string) gives us compile-time safety when
 * mapping raw source strings and lets the UI exhaustively render chips.
 */
enum class MarketCategory(val displayName: String) {
    Presidential("Presidential"),
    Congress("Congress"),
    International("International"),
    Economy("Economy"),
    Other("Other");

    companion object {
        /** Best-effort mapping from free-form API tags to a category. */
        fun fromTags(tags: List<String>): MarketCategory {
            val lowered = tags.map { it.lowercase() }
            return when {
                lowered.any { it.contains("president") || it.contains("white house") } -> Presidential
                lowered.any { it.contains("senate") || it.contains("house") || it.contains("congress") } -> Congress
                lowered.any { it.contains("global") || it.contains("world") || it.contains("foreign") } -> International
                lowered.any { it.contains("econom") || it.contains("fed") || it.contains("cpi") } -> Economy
                else -> Other
            }
        }
    }
}
