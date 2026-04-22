package com.shayanhaque.politicstracker.data.remote

import com.shayanhaque.politicstracker.data.remote.dto.MarketMapper
import com.shayanhaque.politicstracker.model.Market
import com.shayanhaque.politicstracker.model.PricePoint
import com.shayanhaque.politicstracker.util.NetworkResult
import kotlinx.coroutines.CancellationException

/**
 * Real-network implementation backed by Polymarket. Errors (including
 * cancellation-preserving rethrow) are caught here so the repository always
 * gets a well-typed [NetworkResult] and never has to guard against raw
 * exceptions.
 */
class PolymarketRemoteDataSource(
    private val api: PolymarketApi,
) : MarketRemoteDataSource {

    override suspend fun fetchMarkets(): NetworkResult<List<Market>> = safeCall {
        api.listMarkets().mapNotNull(MarketMapper::toDomain)
    }

    override suspend fun fetchMarket(id: String): NetworkResult<Market> = safeCall {
        MarketMapper.toDomain(api.getMarket(id))
            ?: error("Market $id returned an unusable payload")
    }

    override suspend fun fetchPriceHistory(marketId: String): NetworkResult<List<PricePoint>> = safeCall {
        api.getPriceHistory(marketId).mapNotNull(MarketMapper::toDomain)
    }

    private inline fun <T> safeCall(block: () -> T): NetworkResult<T> = try {
        NetworkResult.Success(block())
    } catch (ce: CancellationException) {
        // Never swallow cancellation — it would turn a user navigating away
        // into a phantom failure state.
        throw ce
    } catch (t: Throwable) {
        NetworkResult.Failure(t)
    }
}
