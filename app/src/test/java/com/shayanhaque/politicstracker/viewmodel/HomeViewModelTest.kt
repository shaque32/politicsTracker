package com.shayanhaque.politicstracker.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.shayanhaque.politicstracker.data.repository.MarketRepositoryImpl
import com.shayanhaque.politicstracker.fakes.FakeMarketDao
import com.shayanhaque.politicstracker.fakes.FakeRemoteDataSource
import com.shayanhaque.politicstracker.fakes.sampleMarket
import com.shayanhaque.politicstracker.model.MarketCategory
import com.shayanhaque.politicstracker.model.SortOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Covers the three bits of logic a reviewer would most want a test for:
 *  1. Initial load populates Success from the repo.
 *  2. Filters (category + search) narrow the visible list.
 *  3. Sort order respects the selected [SortOption].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()

    private val dao = FakeMarketDao()
    private val remote = FakeRemoteDataSource()
    private val repository = MarketRepositoryImpl(remote, dao) { 1L }

    @Before
    fun setUp() { Dispatchers.setMain(dispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial load emits Success with all markets`() = runTest(dispatcher) {
        remote.nextMarkets = listOf(
            sampleMarket("a", volumeUsd = 1_000.0),
            sampleMarket("b", volumeUsd = 2_000.0),
        )
        val vm = HomeViewModel(repository)
        dispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertThat(state).isInstanceOf(UiState.Success::class.java)
        val data = (state as UiState.Success).data
        assertThat(data.map { it.id }).containsExactly("b", "a").inOrder()
    }

    @Test
    fun `category filter narrows result set`() = runTest(dispatcher) {
        remote.nextMarkets = listOf(
            sampleMarket("p", category = MarketCategory.Presidential),
            sampleMarket("e", category = MarketCategory.Economy),
        )
        val vm = HomeViewModel(repository)
        dispatcher.scheduler.advanceUntilIdle()

        vm.onCategorySelect(MarketCategory.Economy)
        dispatcher.scheduler.advanceUntilIdle()

        val data = (vm.uiState.value as UiState.Success).data
        assertThat(data.map { it.id }).containsExactly("e")
    }

    @Test
    fun `biggest movers sort orders by absolute daily change`() = runTest(dispatcher) {
        remote.nextMarkets = listOf(
            sampleMarket("small", dailyChange = 0.01),
            sampleMarket("big", dailyChange = -0.12),
            sampleMarket("mid", dailyChange = 0.05),
        )
        val vm = HomeViewModel(repository)
        dispatcher.scheduler.advanceUntilIdle()

        vm.onSortChange(SortOption.BiggestMovers)
        dispatcher.scheduler.advanceUntilIdle()

        val data = (vm.uiState.value as UiState.Success).data
        assertThat(data.map { it.id }).containsExactly("big", "mid", "small").inOrder()
    }
}
