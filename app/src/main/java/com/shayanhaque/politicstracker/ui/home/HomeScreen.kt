package com.shayanhaque.politicstracker.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.shayanhaque.politicstracker.model.Market
import com.shayanhaque.politicstracker.ui.components.EmptyState
import com.shayanhaque.politicstracker.ui.components.ErrorState
import com.shayanhaque.politicstracker.ui.components.FilterChipRow
import com.shayanhaque.politicstracker.ui.components.LoadingState
import com.shayanhaque.politicstracker.ui.components.MarketCard
import com.shayanhaque.politicstracker.ui.components.SearchBar
import com.shayanhaque.politicstracker.ui.components.SortMenu
import com.shayanhaque.politicstracker.ui.components.StaleBanner
import com.shayanhaque.politicstracker.viewmodel.HomeViewModel
import com.shayanhaque.politicstracker.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onMarketClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.observeAsState(UiState.Loading)
    val query by viewModel.query.observeAsState("")
    val category by viewModel.category.observeAsState(null)
    val trending by viewModel.trendingOnly.observeAsState(false)
    val sort by viewModel.sort.observeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.action_refresh))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        modifier = modifier,
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            SearchBar(
                query = query,
                onQueryChange = viewModel::onQueryChange,
                placeholder = stringResource(R.string.search_hint),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )

            FilterChipRow(
                selectedCategory = category,
                trendingOnly = trending,
                onCategorySelect = viewModel::onCategorySelect,
                onTrendingToggle = viewModel::onTrendingToggle,
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                sort?.let {
                    SortMenu(selected = it, onSelect = viewModel::onSortChange)
                }
            }

            Spacer(Modifier.height(8.dp))

            HomeContent(
                state = uiState,
                onRetry = { viewModel.refresh(initial = true) },
                onMarketClick = onMarketClick,
            )
        }
    }
}

@Composable
private fun HomeContent(
    state: UiState<List<Market>>,
    onRetry: () -> Unit,
    onMarketClick: (String) -> Unit,
) {
    when (state) {
        UiState.Loading -> LoadingState(label = stringResource(R.string.loading))
        UiState.Empty -> EmptyState(
            title = stringResource(R.string.empty_markets),
            subtitle = "Try clearing filters or adjusting your search.",
        )
        is UiState.Error -> ErrorState(message = state.message, onRetry = onRetry)
        is UiState.Success -> {
            Column(Modifier.fillMaxSize()) {
                if (state.stale) {
                    StaleBanner(message = stringResource(R.string.stale_cache))
                }
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(items = state.data, key = { it.id }) { market ->
                        MarketCard(market = market, onClick = { onMarketClick(market.id) })
                    }
                }
            }
        }
    }
}
