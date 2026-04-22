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
import com.shayanhaque.politicstracker.model.PricePoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class DetailScreenData(
    val market: Market,
    val history: List<PricePoint>,
    val isFavorite: Boolean,
)

class DetailViewModel(
    private val marketId: String,
    private val repository: MarketRepository,
) : ViewModel() {

    private val _uiState = MutableLiveData<UiState<DetailScreenData>>(UiState.Loading)
    val uiState: LiveData<UiState<DetailScreenData>> get() = _uiState

    private val _isRefreshing = MutableLiveData(false)
    val isRefreshing: LiveData<Boolean> get() = _isRefreshing

    val isFavorite: LiveData<Boolean> = repository.observeIsFavorite(marketId).asLiveData()

    init {
        combine(
            repository.observeMarkets().map { list -> list.firstOrNull { it.id == marketId } },
            repository.observePriceHistory(marketId),
            repository.observeIsFavorite(marketId),
        ) { market, history, isFav ->
            Triple(market, history, isFav)
        }
            .onEach { (market, history, isFav) ->
                if (market == null) {
                    // Could legitimately be missing until the first refresh lands.
                    if (_uiState.value !is UiState.Success) _uiState.postValue(UiState.Loading)
                } else {
                    _uiState.postValue(UiState.Success(DetailScreenData(market, history, isFav)))
                }
            }
            .launchIn(viewModelScope)

        refresh()
    }

    fun refresh() {
        if (_isRefreshing.value == true) return
        _isRefreshing.postValue(true)
        viewModelScope.launch {
            when (val outcome = repository.refreshMarketDetail(marketId)) {
                is RefreshOutcome.Success -> Unit
                is RefreshOutcome.Failure -> {
                    val current = _uiState.value
                    if (current is UiState.Success) {
                        _uiState.postValue(current.copy(stale = true))
                    } else {
                        _uiState.postValue(
                            UiState.Error(
                                outcome.cause.message?.takeIf { it.isNotBlank() }
                                    ?: "Couldn't load this market."
                            )
                        )
                    }
                }
            }
            _isRefreshing.postValue(false)
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch { repository.toggleFavorite(marketId) }
    }

    class Factory(
        private val marketId: String,
        private val repository: MarketRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DetailViewModel(marketId, repository) as T
    }
}
