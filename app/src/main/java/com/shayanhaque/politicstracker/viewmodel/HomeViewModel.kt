package com.shayanhaque.politicstracker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.shayanhaque.politicstracker.data.repository.MarketRepository
import com.shayanhaque.politicstracker.data.repository.RefreshOutcome
import com.shayanhaque.politicstracker.model.Market
import com.shayanhaque.politicstracker.model.MarketCategory
import com.shayanhaque.politicstracker.model.SortOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Drives the home screen. State is exposed via [LiveData] as required; the
 * ViewModel internally uses Flow to combine repository data with the user's
 * query/filter/sort selections so the pipeline stays declarative.
 */
class HomeViewModel(
    private val repository: MarketRepository,
) : ViewModel() {

    private val _uiState = MutableLiveData<UiState<List<Market>>>(UiState.Loading)
    val uiState: LiveData<UiState<List<Market>>> get() = _uiState

    private val _isRefreshing = MutableLiveData(false)
    val isRefreshing: LiveData<Boolean> get() = _isRefreshing

    private val _query = MutableStateFlow("")
    private val _category = MutableStateFlow<MarketCategory?>(null)
    private val _trendingOnly = MutableStateFlow(false)
    private val _sort = MutableStateFlow(SortOption.MostActive)

    val query: LiveData<String> = _query.asLiveData()
    val category: LiveData<MarketCategory?> = _category.asLiveData()
    val trendingOnly: LiveData<Boolean> = _trendingOnly.asLiveData()
    val sort: LiveData<SortOption> = _sort.asLiveData()

    init {
        // Derive UI state from (cached markets × query × filters × sort).
        combine(
            repository.observeMarkets(),
            _query,
            _category,
            _trendingOnly,
            _sort,
        ) { markets, query, category, trendingOnly, sort ->
            applyFilters(markets, query, category, trendingOnly, sort)
        }
            .onEach { filtered ->
                _uiState.postValue(
                    when {
                        filtered.isEmpty() && _uiState.value is UiState.Loading -> UiState.Loading
                        filtered.isEmpty() -> UiState.Empty
                        else -> UiState.Success(filtered)
                    }
                )
            }
            .launchIn(viewModelScope)

        refresh(initial = true)
    }

    fun onQueryChange(new: String) { _query.value = new }
    fun onCategorySelect(category: MarketCategory?) { _category.value = category }
    fun onTrendingToggle(enabled: Boolean) { _trendingOnly.value = enabled }
    fun onSortChange(option: SortOption) { _sort.value = option }

    fun refresh(initial: Boolean = false) {
        if (_isRefreshing.value == true) return
        _isRefreshing.postValue(true)
        if (initial) _uiState.postValue(UiState.Loading)
        viewModelScope.launch {
            when (val outcome = repository.refreshMarkets()) {
                is RefreshOutcome.Success -> Unit // Room flow will re-emit.
                is RefreshOutcome.Failure -> {
                    // Keep showing cached data if we have any — only degrade to an
                    // error screen if the cache is genuinely empty.
                    val current = _uiState.value
                    if (current !is UiState.Success) {
                        _uiState.postValue(UiState.Error(outcome.cause.messageOrDefault()))
                    } else {
                        _uiState.postValue(current.copy(stale = true))
                    }
                }
            }
            _isRefreshing.postValue(false)
        }
    }

    private fun applyFilters(
        markets: List<Market>,
        query: String,
        category: MarketCategory?,
        trendingOnly: Boolean,
        sort: SortOption,
    ): List<Market> {
        val normalizedQuery = query.trim().lowercase()
        val filtered = markets.asSequence()
            .filter { it.isActive }
            .filter { category == null || it.category == category }
            .filter { !trendingOnly || it.isTrending }
            .filter { m ->
                normalizedQuery.isEmpty() ||
                    m.title.lowercase().contains(normalizedQuery) ||
                    m.description.lowercase().contains(normalizedQuery)
            }
            .toList()

        return when (sort) {
            SortOption.MostActive -> filtered.sortedByDescending { it.volumeUsd ?: 0.0 }
            SortOption.BiggestMovers -> filtered.sortedByDescending { abs(it.dailyChange) }
            SortOption.Newest -> filtered.sortedByDescending { it.closeDateMillis ?: 0L }
        }
    }

    class Factory(private val repository: MarketRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(repository) as T
    }
}

private fun Throwable.messageOrDefault(): String =
    message?.takeIf { it.isNotBlank() } ?: "Couldn't reach the markets service."
