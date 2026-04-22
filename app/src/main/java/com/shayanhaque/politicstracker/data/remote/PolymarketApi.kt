package com.shayanhaque.politicstracker.data.remote

import com.shayanhaque.politicstracker.data.remote.dto.MarketDto
import com.shayanhaque.politicstracker.data.remote.dto.PricePointDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit contract for Polymarket's public Gamma API. The endpoints we use
 * do NOT require authentication, which keeps this project easy to run
 * without credentials. If a key ever becomes required, it should be added
 * as an OkHttp interceptor in [NetworkModule] — not here.
 */
interface PolymarketApi {

    @GET("markets")
    suspend fun listMarkets(
        @Query("active") active: Boolean = true,
        @Query("closed") closed: Boolean = false,
        @Query("limit") limit: Int = 100,
        @Query("order") order: String = "volume",
        @Query("ascending") ascending: Boolean = false,
    ): List<MarketDto>

    @GET("markets/{id}")
    suspend fun getMarket(@Path("id") id: String): MarketDto

    /** Returns recent trade prices. [interval] uses the API's own grammar: "1d", "1w", "1m", "max". */
    @GET("prices-history")
    suspend fun getPriceHistory(
        @Query("market") marketId: String,
        @Query("interval") interval: String = "1m",
    ): List<PricePointDto>
}
