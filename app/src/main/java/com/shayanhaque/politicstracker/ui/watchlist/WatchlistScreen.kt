package com.shayanhaque.politicstracker.ui.watchlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shayanhaque.politicstracker.R
import com.shayanhaque.politicstracker.ui.components.EmptyState
import com.shayanhaque.politicstracker.ui.components.LoadingState
import com.shayanhaque.politicstracker.ui.components.MarketCard
import com.shayanhaque.politicstracker.viewmodel.UiState
import com.shayanhaque.politicstracker.viewmodel.WatchlistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    viewModel: WatchlistViewModel,
    onMarketClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.observeAsState(UiState.Loading)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tab_watchlist)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        modifier = modifier,
    ) { padding ->
        when (val s = state) {
            UiState.Loading -> LoadingState(Modifier.padding(padding))
            UiState.Empty -> EmptyState(
                title = stringResource(R.string.empty_watchlist),
                subtitle = "Tap the heart on any market to save it here.",
                modifier = Modifier.padding(padding),
            )
            is UiState.Error -> EmptyState(
                title = s.message,
                modifier = Modifier.padding(padding),
            )
            is UiState.Success -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items = s.data, key = { it.id }) { market ->
                    MarketCard(market = market, onClick = { onMarketClick(market.id) })
                }
            }
        }
    }
}
