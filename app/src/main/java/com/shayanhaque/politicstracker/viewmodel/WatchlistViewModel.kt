package com.shayanhaque.politicstracker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shayanhaque.politicstracker.data.repository.MarketRepository
import com.shayanhaque.politicstracker.model.Market
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class WatchlistViewModel(
    private val repository: MarketRepository,
) : ViewModel() {

    private val _uiState = MutableLiveData<UiState<List<Market>>>(UiState.Loading)
    val uiState: LiveData<UiState<List<Market>>> get() = _uiState

    init {
        repository.observeFavorites()
            .onEach { favorites ->
                _uiState.postValue(
                    if (favorites.isEmpty()) UiState.Empty else UiState.Success(favorites)
                )
            }
            .launchIn(viewModelScope)
    }

    fun removeFavorite(marketId: String) {
        viewModelScope.launch { repository.toggleFavorite(marketId) }
    }

    class Factory(private val repository: MarketRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            WatchlistViewModel(repository) as T
    }
}
