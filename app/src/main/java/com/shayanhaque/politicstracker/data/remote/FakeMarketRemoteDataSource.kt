package com.shayanhaque.politicstracker.data.remote

import com.shayanhaque.politicstracker.model.Market
import com.shayanhaque.politicstracker.model.MarketCategory
import com.shayanhaque.politicstracker.model.PricePoint
import com.shayanhaque.politicstracker.util.NetworkResult
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

/**
 * Offline, in-memory data source with realistic-looking sample data. Useful
 * in three situations:
 *  - The Polymarket API is down or rate-limited.
 *  - We're running unit tests and don't want any network.
 *  - A reviewer is evaluating the project without internet access.
 *
 * Switched on via [com.shayanhaque.politicstracker.di.AppContainer.useFakeData].
 */
class FakeMarketRemoteDataSource(
    private val artificialDelayMs: Long = 400L,
    private val seed: Long = 42L,
) : MarketRemoteDataSource {

    private val rng = Random(seed)

    private val sampleMarkets: List<Market> = listOf(
        Market(
            id = "pres-2028-gop-nominee",
            title = "Who will be the 2028 GOP presidential nominee?",
            description = "Market resolves to the Republican Party's official 2028 presidential nominee.",
            category = MarketCategory.Presidential,
            probability = 0.31,
            dailyChange = 0.024,
            volumeUsd = 4_820_000.0,
            closeDateMillis = daysFromNow(420),
            rules = "Resolves YES for the individual officially nominated by the Republican National Convention in 2028.",
            isActive = true,
            isTrending = true,
        ),
        Market(
            id = "senate-2026-majority",
            title = "Will Democrats hold the Senate majority after 2026?",
            description = "Resolves YES if Democrats (plus independents caucusing with them) hold 50+ seats on Jan 3, 2027.",
            category = MarketCategory.Congress,
            probability = 0.44,
            dailyChange = -0.018,
            volumeUsd = 2_130_000.0,
            closeDateMillis = daysFromNow(220),
            rules = "Majority is determined by seating on January 3, 2027. Tiebreaking via VP does not count.",
            isActive = true,
            isTrending = true,
        ),
        Market(
            id = "house-2026-control",
            title = "Which party controls the House after 2026?",
            description = "Resolves to the party with 218+ seats at the start of the 120th Congress.",
            category = MarketCategory.Congress,
            probability = 0.58,
            dailyChange = 0.011,
            volumeUsd = 1_560_000.0,
            closeDateMillis = daysFromNow(220),
            rules = "Market resolves based on officially seated members on the first day of the 120th Congress.",
            isActive = true,
            isTrending = false,
        ),
        Market(
            id = "fed-rate-cut-june",
            title = "Will the Fed cut rates at the June FOMC meeting?",
            description = "Resolves YES if the FOMC announces a rate cut at its scheduled June meeting.",
            category = MarketCategory.Economy,
            probability = 0.67,
            dailyChange = 0.082,
            volumeUsd = 980_000.0,
            closeDateMillis = daysFromNow(45),
            rules = "A 'cut' means the target range upper bound is lowered versus the prior meeting.",
            isActive = true,
            isTrending = true,
        ),
        Market(
            id = "uk-pm-endofyear",
            title = "Who will be UK Prime Minister on December 31?",
            description = "Market resolves to the individual serving as Prime Minister on Dec 31 at 23:59 GMT.",
            category = MarketCategory.International,
            probability = 0.71,
            dailyChange = -0.003,
            volumeUsd = 540_000.0,
            closeDateMillis = daysFromNow(250),
            rules = "Whoever is recognized by the Crown as PM at the resolution moment.",
            isActive = true,
            isTrending = false,
        ),
        Market(
            id = "eu-parliament-rightbloc",
            title = "Will the right bloc win a plurality in the next EU Parliament?",
            description = "Resolves YES if EPP + ECR + PfE collectively hold more seats than any other combination of blocs.",
            category = MarketCategory.International,
            probability = 0.38,
            dailyChange = 0.015,
            volumeUsd = 220_000.0,
            closeDateMillis = daysFromNow(610),
            rules = "Based on official seat allocation at the first plenary session.",
            isActive = true,
            isTrending = false,
        ),
        Market(
            id = "us-recession-eoy",
            title = "Will the US enter a recession before year-end?",
            description = "Resolves YES if NBER declares a recession with start date on or before Dec 31.",
            category = MarketCategory.Economy,
            probability = 0.22,
            dailyChange = -0.027,
            volumeUsd = 1_100_000.0,
            closeDateMillis = daysFromNow(180),
            rules = "Resolution source is the NBER Business Cycle Dating Committee.",
            isActive = true,
            isTrending = false,
        ),
        Market(
            id = "vp-pick-gop",
            title = "Who will be the 2028 GOP VP nominee?",
            description = "Resolves to the Republican VP nominee named at the 2028 RNC.",
            category = MarketCategory.Presidential,
            probability = 0.09,
            dailyChange = 0.004,
            volumeUsd = 310_000.0,
            closeDateMillis = daysFromNow(420),
            rules = "Based on the official VP nominee of the 2028 Republican National Convention.",
            isActive = true,
            isTrending = false,
        ),
    )

    override suspend fun fetchMarkets(): NetworkResult<List<Market>> {
        delay(artificialDelayMs)
        return NetworkResult.Success(sampleMarkets)
    }

    override suspend fun fetchMarket(id: String): NetworkResult<Market> {
        delay(artificialDelayMs / 2)
        val hit = sampleMarkets.firstOrNull { it.id == id }
            ?: return NetworkResult.Failure(NoSuchElementException("Unknown market $id"))
        return NetworkResult.Success(hit)
    }

    override suspend fun fetchPriceHistory(marketId: String): NetworkResult<List<PricePoint>> {
        delay(artificialDelayMs / 2)
        val base = sampleMarkets.firstOrNull { it.id == marketId }?.probability ?: 0.5
        val now = System.currentTimeMillis()
        val points = (0 until 60).map { i ->
            val t = now - (60 - i) * 24L * 60 * 60 * 1000
            val drift = sin(i / 7.0) * 0.08
            val jitter = (rng.nextDouble() - 0.5) * 0.05
            val p = (base + drift + jitter).coerceIn(0.02, 0.98)
            PricePoint(timestampMillis = t, probability = p)
        }
        return NetworkResult.Success(points)
    }

    private fun daysFromNow(days: Int): Long =
        System.currentTimeMillis() + days * 24L * 60 * 60 * 1000
}
