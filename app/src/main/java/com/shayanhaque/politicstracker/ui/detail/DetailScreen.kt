package com.shayanhaque.politicstracker.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shayanhaque.politicstracker.R
import com.shayanhaque.politicstracker.ui.components.ErrorState
import com.shayanhaque.politicstracker.ui.components.HistoricalPriceChart
import com.shayanhaque.politicstracker.ui.components.LoadingState
import com.shayanhaque.politicstracker.ui.components.PriceChangeBadge
import com.shayanhaque.politicstracker.ui.components.StaleBanner
import com.shayanhaque.politicstracker.util.Formatters
import com.shayanhaque.politicstracker.viewmodel.DetailScreenData
import com.shayanhaque.politicstracker.viewmodel.DetailViewModel
import com.shayanhaque.politicstracker.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.observeAsState(UiState.Loading)
    val isFavorite by viewModel.isFavorite.observeAsState(false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Market") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) {
                                stringResource(R.string.action_unfavorite)
                            } else {
                                stringResource(R.string.action_favorite)
                            },
                        )
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.action_refresh))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        }
    ) { padding ->
        when (val state = uiState) {
            UiState.Loading -> LoadingState(Modifier.padding(padding))
            UiState.Empty -> LoadingState(Modifier.padding(padding))
            is UiState.Error -> ErrorState(
                message = state.message,
                onRetry = { viewModel.refresh() },
                modifier = Modifier.padding(padding),
            )
            is UiState.Success -> DetailBody(
                data = state.data,
                stale = state.stale,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun DetailBody(
    data: DetailScreenData,
    stale: Boolean,
    modifier: Modifier = Modifier,
) {
    val market = data.market
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        if (stale) {
            StaleBanner(message = stringResource(R.string.stale_cache))
            Spacer(Modifier.height(12.dp))
        }

        Text(
            text = market.category.displayName,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(Modifier.height(4.dp))
        Text(market.title, style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = Formatters.probability(market.probability),
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(Modifier.width(12.dp))
            PriceChangeBadge(dailyChange = market.dailyChange)
        }

        Spacer(Modifier.height(20.dp))
        Text(
            stringResource(R.string.detail_history),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(8.dp))
        HistoricalPriceChart(points = data.history)

        Spacer(Modifier.height(24.dp))
        DetailMetaGrid(
            volume = Formatters.volume(market.volumeUsd),
            closeDate = Formatters.closeDate(market.closeDateMillis),
            category = market.category.displayName,
        )

        if (market.rules.isNotBlank()) {
            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(R.string.detail_rules),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                market.rules,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun DetailMetaGrid(volume: String, closeDate: String, category: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        MetaRow(label = stringResource(R.string.detail_volume), value = volume)
        MetaRow(label = stringResource(R.string.detail_close_date), value = closeDate)
        MetaRow(label = stringResource(R.string.detail_category), value = category)
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
