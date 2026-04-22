package com.shayanhaque.politicstracker.fakes

import com.shayanhaque.politicstracker.data.remote.MarketRemoteDataSource
import com.shayanhaque.politicstracker.model.Market
import com.shayanhaque.politicstracker.model.MarketCategory
import com.shayanhaque.politicstracker.model.PricePoint
import com.shayanhaque.politicstracker.util.NetworkResult

/**
 * Scriptable remote stub: tests drive the responses directly via
 * [nextMarkets]/[nextFailure], avoiding Retrofit in the JVM test run.
 */
class FakeRemoteDataSource : MarketRemoteDataSource {
    var nextMarkets: List<Market> = emptyList()
    var nextFailure: Throwable? = null
    var fetchCount: Int = 0

    override suspend fun fetchMarkets(): NetworkResult<List<Market>> {
        fetchCount += 1
        nextFailure?.let { return NetworkResult.Failure(it) }
        return NetworkResult.Success(nextMarkets)
    }

    override suspend fun fetchMarket(id: String): NetworkResult<Market> {
        nextFailure?.let { return NetworkResult.Failure(it) }
        val hit = nextMarkets.firstOrNull { it.id == id }
            ?: return NetworkResult.Failure(NoSuchElementException(id))
        return NetworkResult.Success(hit)
    }

    override suspend fun fetchPriceHistory(marketId: String): NetworkResult<List<PricePoint>> {
        nextFailure?.let { return NetworkResult.Failure(it) }
        return NetworkResult.Success(emptyList())
    }
}

fun sampleMarket(
    id: String = "m1",
    category: MarketCategory = MarketCategory.Presidential,
    probability: Double = 0.55,
    dailyChange: Double = 0.02,
    volumeUsd: Double? = 250_000.0,
    isTrending: Boolean = false,
): Market = Market(
    id = id,
    title = "Sample market $id",
    description = "Desc $id",
    category = category,
    probability = probability,
    dailyChange = dailyChange,
    volumeUsd = volumeUsd,
    closeDateMillis = null,
    rules = "rules",
    isActive = true,
    isTrending = isTrending,
)
