package com.shayanhaque.politicstracker.data.repository

import com.google.common.truth.Truth.assertThat
import com.shayanhaque.politicstracker.fakes.FakeMarketDao
import com.shayanhaque.politicstracker.fakes.FakeRemoteDataSource
import com.shayanhaque.politicstracker.fakes.sampleMarket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class MarketRepositoryImplTest {

    private val dao = FakeMarketDao()
    private val remote = FakeRemoteDataSource()
    private val repository = MarketRepositoryImpl(remote = remote, dao = dao, clock = { 1_000L })

    @Test
    fun `refreshMarkets replaces cache on success`() = runTest {
        remote.nextMarkets = listOf(sampleMarket("a"), sampleMarket("b"))

        val outcome = repository.refreshMarkets()

        assertThat(outcome).isEqualTo(RefreshOutcome.Success)
        val cached = repository.observeMarkets().first()
        assertThat(cached.map { it.id }).containsExactly("a", "b")
    }

    @Test
    fun `refreshMarkets returns Failure and does not clear cache on network error`() = runTest {
        remote.nextMarkets = listOf(sampleMarket("a"))
        repository.refreshMarkets()

        remote.nextFailure = IOException("offline")
        val outcome = repository.refreshMarkets()

        assertThat(outcome).isInstanceOf(RefreshOutcome.Failure::class.java)
        val cached = repository.observeMarkets().first()
        assertThat(cached.map { it.id }).containsExactly("a")
    }

    @Test
    fun `toggleFavorite adds then removes the market from favorites`() = runTest {
        remote.nextMarkets = listOf(sampleMarket("a"))
        repository.refreshMarkets()

        repository.toggleFavorite("a")
        assertThat(repository.observeFavorites().first().map { it.id }).containsExactly("a")

        repository.toggleFavorite("a")
        assertThat(repository.observeFavorites().first()).isEmpty()
    }

    @Test
    fun `latestCacheTimestamp reflects clock at refresh time`() = runTest {
        remote.nextMarkets = listOf(sampleMarket("a"))
        repository.refreshMarkets()
        assertThat(repository.latestCacheTimestamp()).isEqualTo(1_000L)
    }
}
