package com.shayanhaque.politicstracker.viewmodel

/**
 * Canonical four-state wrapper used by every screen's ViewModel. Making this
 * sealed + generic keeps Compose rendering exhaustive and makes it obvious
 * what transitions are legal. `Success` carries the data; `Empty` is a
 * distinct state so we can show a friendlier "no results" UI when a query
 * legitimately matched zero markets versus the initial loading spinner.
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T, val stale: Boolean = false) : UiState<T>
    data object Empty : UiState<Nothing>
    data class Error(val message: String) : UiState<Nothing>
}
