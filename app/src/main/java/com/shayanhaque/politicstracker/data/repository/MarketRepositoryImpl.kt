package com.shayanhaque.politicstracker.data.repository

import com.shayanhaque.politicstracker.data.local.MarketDao
import com.shayanhaque.politicstracker.data.local.entity.FavoriteEntity
import com.shayanhaque.politicstracker.data.local.entity.MarketEntity
import com.shayanhaque.politicstracker.data.local.entity.PricePointEntity
import com.shayanhaque.politicstracker.data.remote.MarketRemoteDataSource
import com.shayanhaque.politicstracker.model.Market
import com.shayanhaque.politicstracker.model.PricePoint
import com.shayanhaque.politicstracker.util.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Standard offline-first repository: Room is the single source of truth,
 * the network is a cache filler. Flows are served directly from Room so
 * every observer re-emits the instant a refresh lands.
 */
class MarketRepositoryImpl(
    private val remote: MarketRemoteDataSource,
    private val dao: MarketDao,
    private val clock: () -> Long = System::currentTimeMillis,
) : MarketRepository {

    override fun observeMarkets(): Flow<List<Market>> =
        dao.observeMarkets().map { rows -> rows.map(MarketEntity::toDomain) }.distinctUntilChanged()

    override fun observeFavorites(): Flow<List<Market>> =
        dao.observeFavoriteMarkets().map { rows -> rows.map(MarketEntity::toDomain) }.distinctUntilChanged()

    override fun observeIsFavorite(marketId: String): Flow<Boolean> =
        dao.observeIsFavorite(marketId).distinctUntilChanged()

    override fun observePriceHistory(marketId: String): Flow<List<PricePoint>> =
        dao.observePriceHistory(marketId)
            .map { rows -> rows.map(PricePointEntity::toDomain) }
            .distinctUntilChanged()

    override suspend fun latestCacheTimestamp(): Long? = dao.latestCacheTimestamp()

    override suspend fun refreshMarkets(): RefreshOutcome {
        return when (val result = remote.fetchMarkets()) {
            is NetworkResult.Success -> {
                val now = clock()
                dao.replaceMarkets(result.value.map { MarketEntity.fromDomain(it, now) })
                RefreshOutcome.Success
            }
            is NetworkResult.Failure -> RefreshOutcome.Failure(result.cause)
        }
    }

    override suspend fun refreshMarketDetail(marketId: String): RefreshOutcome {
        val marketResult = remote.fetchMarket(marketId)
        val historyResult = remote.fetchPriceHistory(marketId)

        if (marketResult is NetworkResult.Success) {
            dao.upsertMarket(MarketEntity.fromDomain(marketResult.value, clock()))
        }
        if (historyResult is NetworkResult.Success) {
            dao.replacePriceHistory(
                marketId,
                historyResult.value.map { PricePointEntity.fromDomain(marketId, it) },
            )
        }

        // Only report failure if we couldn't get the core market payload —
        // a missing history curve shouldn't block the detail screen.
        return when (marketResult) {
            is NetworkResult.Success -> RefreshOutcome.Success
            is NetworkResult.Failure -> RefreshOutcome.Failure(marketResult.cause)
        }
    }

    override suspend fun toggleFavorite(marketId: String) {
        val isFavorite = dao.observeIsFavorite(marketId).first()
        if (isFavorite) {
            dao.removeFavorite(marketId)
        } else {
            dao.addFavorite(FavoriteEntity(marketId = marketId, favoritedAtMillis = clock()))
        }
    }
}
