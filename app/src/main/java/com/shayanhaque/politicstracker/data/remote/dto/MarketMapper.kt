package com.shayanhaque.politicstracker.data.remote.dto

import com.shayanhaque.politicstracker.model.Market
import com.shayanhaque.politicstracker.model.MarketCategory
import com.shayanhaque.politicstracker.model.PricePoint
import java.time.Instant
import java.time.format.DateTimeParseException

/**
 * Translates Polymarket DTOs to our clean domain models. Keeping this in a
 * dedicated file means the rest of the codebase never has to know about the
 * upstream quirks (outcomePrices arriving as a stringified JSON array,
 * timestamps in seconds rather than millis, etc.).
 */
object MarketMapper {

    fun toDomain(dto: MarketDto): Market? {
        val id = dto.id ?: dto.slug ?: return null
        val title = dto.question?.takeIf { it.isNotBlank() } ?: return null

        val probability = dto.lastTradePrice
            ?: parseFirstOutcomePrice(dto.outcomePrices)
            ?: return null

        val tags = buildList {
            dto.category?.let { add(it) }
            dto.tags?.let { addAll(it) }
        }

        return Market(
            id = id,
            title = title,
            description = (dto.description ?: "").take(280),
            category = MarketCategory.fromTags(tags),
            probability = probability.coerceIn(0.0, 1.0),
            dailyChange = dto.oneDayPriceChange ?: 0.0,
            volumeUsd = dto.volume ?: dto.liquidity,
            closeDateMillis = dto.endDateIso?.let(::parseIsoToMillis),
            rules = dto.description.orEmpty(),
            isActive = dto.active == true && dto.closed != true,
            isTrending = dto.featured == true,
        )
    }

    fun toDomain(dto: PricePointDto): PricePoint? {
        val ts = dto.timestampSeconds ?: return null
        val p = dto.price ?: return null
        return PricePoint(timestampMillis = ts * 1_000L, probability = p.coerceIn(0.0, 1.0))
    }

    /** Upstream returns `outcomePrices` as a JSON-encoded string e.g. "[\"0.62\",\"0.38\"]". */
    private fun parseFirstOutcomePrice(raw: String?): Double? {
        if (raw.isNullOrBlank()) return null
        return raw.trim().trim('[', ']')
            .split(',')
            .firstOrNull()
            ?.trim()
            ?.trim('"')
            ?.toDoubleOrNull()
    }

    private fun parseIsoToMillis(iso: String): Long? = try {
        Instant.parse(iso).toEpochMilli()
    } catch (_: DateTimeParseException) {
        null
    }
}
