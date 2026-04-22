package com.shayanhaque.politicstracker.ui.navigation

/**
 * Navigation "routes" are grouped here so we never leak raw strings into the
 * UI code. Keeping this as a sealed hierarchy also makes it trivial to add
 * new screens (or deep links) later without hunting through composables.
 */
sealed class Destination(val route: String) {
    data object Home : Destination("home")
    data object Watchlist : Destination("watchlist")
    data object Detail : Destination("detail/{marketId}") {
        const val ARG_MARKET_ID = "marketId"
        fun createRoute(marketId: String): String = "detail/$marketId"
    }
}
