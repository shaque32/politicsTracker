package com.shayanhaque.politicstracker.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shayanhaque.politicstracker.model.MarketCategory

/**
 * Horizontally scrolling chip row for category filters. The "Trending"
 * option is modeled here as a separate toggle rather than another category
 * so it can compose with a category filter (e.g. Trending + Presidential).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipRow(
    selectedCategory: MarketCategory?,
    trendingOnly: Boolean,
    onCategorySelect: (MarketCategory?) -> Unit,
    onTrendingToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(PaddingValues(horizontal = 16.dp)),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onCategorySelect(null) },
            label = { Text("All", style = MaterialTheme.typography.labelMedium) },
            colors = selectedChipColors(),
        )
        FilterChip(
            selected = trendingOnly,
            onClick = { onTrendingToggle(!trendingOnly) },
            label = { Text("Trending", style = MaterialTheme.typography.labelMedium) },
            colors = selectedChipColors(),
        )
        // Presidential, Congress, International, Economy
        MarketCategory.entries
            .filter { it != MarketCategory.Other }
            .forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = {
                        onCategorySelect(if (selectedCategory == cat) null else cat)
                    },
                    label = { Text(cat.displayName, style = MaterialTheme.typography.labelMedium) },
                    colors = selectedChipColors(),
                )
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun selectedChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
)
