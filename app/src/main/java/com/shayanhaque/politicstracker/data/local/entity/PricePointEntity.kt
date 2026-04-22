package com.shayanhaque.politicstracker.data.local.entity

import androidx.room.Entity
import com.shayanhaque.politicstracker.model.PricePoint

/**
 * Price-history samples are cached per (marketId, timestamp). This lets us
 * show the chart instantly on repeat visits while still appending newer
 * points on refresh.
 */
@Entity(
    tableName = "price_points",
    primaryKeys = ["marketId", "timestampMillis"],
)
data class PricePointEntity(
    val marketId: String,
    val timestampMillis: Long,
    val probability: Double,
) {
    fun toDomain(): PricePoint = PricePoint(timestampMillis, probability)

    companion object {
        fun fromDomain(marketId: String, point: PricePoint) =
            PricePointEntity(marketId, point.timestampMillis, point.probability)
    }
}
