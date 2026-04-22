package com.shayanhaque.politicstracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shayanhaque.politicstracker.model.Market
import com.shayanhaque.politicstracker.model.MarketCategory

/**
 * Room-level representation of a market. We store the category as its enum
 * name (not the display string) so that renaming a display label doesn't
 * require a schema migration.
 */
@Entity(tableName = "markets")
data class MarketEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val probability: Double,
    val dailyChange: Double,
    val volumeUsd: Double?,
    val closeDateMillis: Long?,
    val rules: String,
    val isActive: Boolean,
    val isTrending: Boolean,
    val cachedAtMillis: Long,
) {
    fun toDomain(): Market = Market(
        id = id,
        title = title,
        description = description,
        category = runCatching { MarketCategory.valueOf(category) }.getOrDefault(MarketCategory.Other),
        probability = probability,
        dailyChange = dailyChange,
        volumeUsd = volumeUsd,
        closeDateMillis = closeDateMillis,
        rules = rules,
        isActive = isActive,
        isTrending = isTrending,
    )

    companion object {
        fun fromDomain(market: Market, cachedAtMillis: Long): MarketEntity = MarketEntity(
            id = market.id,
            title = market.title,
            description = market.description,
            category = market.category.name,
            probability = market.probability,
            dailyChange = market.dailyChange,
            volumeUsd = market.volumeUsd,
            closeDateMillis = market.closeDateMillis,
            rules = market.rules,
            isActive = market.isActive,
            isTrending = market.isTrending,
            cachedAtMillis = cachedAtMillis,
        )
    }
}
