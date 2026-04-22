package com.shayanhaque.politicstracker.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** History endpoint response envelope: `{ "history": [ { t, p }, ... ] }`. */
@JsonClass(generateAdapter = true)
data class PriceHistoryResponseDto(
    @Json(name = "history") val history: List<PricePointDto> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class PricePointDto(
    @Json(name = "t") val timestampSeconds: Long?,
    @Json(name = "p") val price: Double?,
)
