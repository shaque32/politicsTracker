package com.shayanhaque.politicstracker.fakes

import com.shayanhaque.politicstracker.data.local.MarketDao
import com.shayanhaque.politicstracker.data.local.entity.FavoriteEntity
import com.shayanhaque.politicstracker.data.local.entity.MarketEntity
import com.shayanhaque.politicstracker.data.local.entity.PricePointEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory stand-in for [MarketDao] so repository tests don't need a real
 * Room instance. We emulate Room's reactive Flow semantics by backing each
 * query with a [MutableStateFlow].
 */
class FakeMarketDao : MarketDao {

    private val markets = MutableStateFlow<List<MarketEntity>>(emptyList())
    private val favorites = MutableStateFlow<Set<String>>(emptySet())
    private val history = MutableStateFlow<Map<String, List<PricePointEntity>>>(emptyMap())

    override fun observeMarkets(): Flow<List<MarketEntity>> = markets

    override suspend fun getMarket(id: String): MarketEntity? =
        markets.value.firstOrNull { it.id == id }

    override suspend fun upsertMarkets(markets: List<MarketEntity>) {
        val merged = (this.markets.value + markets).associateBy { it.id }.values.toList()
        this.markets.value = merged
    }

    override suspend fun upsertMarket(market: MarketEntity) {
        val merged = (markets.value + market).associateBy { it.id }.values.toList()
        markets.value = merged
    }

    override suspend fun latestCacheTimestamp(): Long? =
        markets.value.maxOfOrNull { it.cachedAtMillis }

    override suspend fun clearMarkets() { markets.value = emptyList() }

    override fun observeFavoriteMarkets(): Flow<List<MarketEntity>> =
        markets.map { list ->
            val favs = favorites.value
            list.filter { it.id in favs }
        }

    override fun observeIsFavorite(id: String): Flow<Boolean> =
        favorites.map { it.contains(id) }

    override suspend fun addFavorite(favorite: FavoriteEntity) {
        favorites.value = favorites.value + favorite.marketId
    }

    override suspend fun removeFavorite(id: String) {
        favorites.value = favorites.value - id
    }

    override fun observePriceHistory(id: String): Flow<List<PricePointEntity>> =
        history.map { it[id].orEmpty() }

    override suspend fun upsertPriceHistory(points: List<PricePointEntity>) {
        val grouped = points.groupBy { it.marketId }
        history.value = history.value.toMutableMap().apply {
            grouped.forEach { (id, list) ->
                val existing = getOrDefault(id, emptyList())
                    .associateBy { it.timestampMillis }
                    .toMutableMap()
                list.forEach { existing[it.timestampMillis] = it }
                put(id, existing.values.sortedBy { it.timestampMillis })
            }
        }
    }

    override suspend fun clearPriceHistory(id: String) {
        history.value = history.value - id
    }
}
