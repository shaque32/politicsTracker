package com.shayanhaque.politicstracker.util

/**
 * Small Result-style type for the remote data source. Kept separate from
 * [com.shayanhaque.politicstracker.viewmodel.UiState] because the two
 * represent different concerns: one is an I/O outcome, the other is the
 * state a screen is currently rendering.
 */
sealed interface NetworkResult<out T> {
    data class Success<T>(val value: T) : NetworkResult<T>
    data class Failure(val cause: Throwable) : NetworkResult<Nothing>
}

inline fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> = when (this) {
    is NetworkResult.Success -> NetworkResult.Success(transform(value))
    is NetworkResult.Failure -> this
}
