package com.shayanhaque.politicstracker.data.remote

import com.shayanhaque.politicstracker.model.Market
import com.shayanhaque.politicstracker.model.PricePoint
import com.shayanhaque.politicstracker.util.NetworkResult

/**
 * Thin interface the repository talks to. Pulling this behind an interface
 * means we can swap Polymarket for another source (Kalshi, Manifold, …)
 * without touching ViewModels, and lets tests inject a fake implementation
 * without standing up Retrofit or OkHttp.
 */
interface MarketRemoteDataSource {
    suspend fun fetchMarkets(): NetworkResult<List<Market>>
    suspend fun fetchMarket(id: String): NetworkResult<Market>
    suspend fun fetchPriceHistory(marketId: String): NetworkResult<List<PricePoint>>
}
