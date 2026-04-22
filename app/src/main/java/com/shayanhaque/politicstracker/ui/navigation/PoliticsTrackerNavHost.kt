package com.shayanhaque.politicstracker.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shayanhaque.politicstracker.R
import com.shayanhaque.politicstracker.di.AppContainer
import com.shayanhaque.politicstracker.ui.detail.DetailScreen
import com.shayanhaque.politicstracker.ui.home.HomeScreen
import com.shayanhaque.politicstracker.ui.watchlist.WatchlistScreen
import com.shayanhaque.politicstracker.viewmodel.DetailViewModel
import com.shayanhaque.politicstracker.viewmodel.HomeViewModel
import com.shayanhaque.politicstracker.viewmodel.WatchlistViewModel

@Composable
fun PoliticsTrackerNavHost(container: AppContainer) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute == Destination.Home.route ||
        currentRoute == Destination.Watchlist.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    BottomTab(
                        label = stringResource(R.string.tab_markets),
                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                        selected = currentRoute == Destination.Home.route,
                        onSelect = {
                            navController.navigate(Destination.Home.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                    BottomTab(
                        label = stringResource(R.string.tab_watchlist),
                        icon = { Icon(Icons.Default.FavoriteBorder, contentDescription = null) },
                        selected = currentRoute == Destination.Watchlist.route,
                        onSelect = {
                            navController.navigate(Destination.Watchlist.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Home.route,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            composable(Destination.Home.route) {
                val vm: HomeViewModel = viewModel(factory = HomeViewModel.Factory(container.repository))
                HomeScreen(
                    viewModel = vm,
                    onMarketClick = { id ->
                        navController.navigate(Destination.Detail.createRoute(id))
                    },
                )
            }

            composable(Destination.Watchlist.route) {
                val vm: WatchlistViewModel = viewModel(
                    factory = WatchlistViewModel.Factory(container.repository),
                )
                WatchlistScreen(
                    viewModel = vm,
                    onMarketClick = { id ->
                        navController.navigate(Destination.Detail.createRoute(id))
                    },
                )
            }

            composable(
                route = Destination.Detail.route,
                arguments = listOf(
                    navArgument(Destination.Detail.ARG_MARKET_ID) { type = NavType.StringType },
                ),
            ) { entry ->
                val id = entry.arguments?.getString(Destination.Detail.ARG_MARKET_ID).orEmpty()
                val vm: DetailViewModel = viewModel(
                    key = "detail-$id",
                    factory = DetailViewModel.Factory(id, container.repository),
                )
                DetailScreen(viewModel = vm, onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.BottomTab(
    label: String,
    icon: @Composable () -> Unit,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onSelect,
        icon = icon,
        label = { Text(label) },
    )
}
