package com.shayanhaque.politicstracker.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Network shape for a market from Polymarket's public Gamma API
 * (https://gamma-api.polymarket.com/markets). Only fields we actually
 * consume are declared; the rest of the payload is ignored by Moshi.
 *
 * We intentionally accept nullable primitives because the upstream is
 * inconsistent — a market can be missing volume, a close date, or even
 * an outcome price depending on its lifecycle state.
 */
@JsonClass(generateAdapter = true)
data class MarketDto(
    @Json(name = "id") val id: String?,
    @Json(name = "slug") val slug: String?,
    @Json(name = "question") val question: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "outcomePrices") val outcomePrices: String?,
    @Json(name = "lastTradePrice") val lastTradePrice: Double?,
    @Json(name = "oneDayPriceChange") val oneDayPriceChange: Double?,
    @Json(name = "volume") val volume: Double?,
    @Json(name = "liquidity") val liquidity: Double?,
    @Json(name = "endDateIso") val endDateIso: String?,
    @Json(name = "active") val active: Boolean?,
    @Json(name = "closed") val closed: Boolean?,
    @Json(name = "featured") val featured: Boolean?,
    @Json(name = "category") val category: String?,
    @Json(name = "tags") val tags: List<String>?,
)
