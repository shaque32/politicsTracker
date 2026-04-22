package com.shayanhaque.politicstracker.data.repository

import com.shayanhaque.politicstracker.model.Market
import com.shayanhaque.politicstracker.model.PricePoint
import kotlinx.coroutines.flow.Flow

/**
 * Public contract for anyone who needs market data. Returning Flow for
 * observables lets Room be the single source of truth — the network just
 * refreshes the cache, and every observer is notified automatically.
 */
interface MarketRepository {

    /** Streams the cached market list. Emits empty list until the first load completes. */
    fun observeMarkets(): Flow<List<Market>>

    fun observeFavorites(): Flow<List<Market>>

    fun observeIsFavorite(marketId: String): Flow<Boolean>

    fun observePriceHistory(marketId: String): Flow<List<PricePoint>>

    /** Timestamp (epoch millis) of the most recent successful cache write, or null if never. */
    suspend fun latestCacheTimestamp(): Long?

    /** Hits the network and updates caches. Returns true on success, false on a network failure. */
    suspend fun refreshMarkets(): RefreshOutcome

    suspend fun refreshMarketDetail(marketId: String): RefreshOutcome

    suspend fun toggleFavorite(marketId: String)
}

sealed interface RefreshOutcome {
    data object Success : RefreshOutcome
    data class Failure(val cause: Throwable) : RefreshOutcome
}
