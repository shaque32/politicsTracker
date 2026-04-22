package com.shayanhaque.politicstracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.shayanhaque.politicstracker.ui.theme.NegativeRed
import com.shayanhaque.politicstracker.ui.theme.NeutralGray
import com.shayanhaque.politicstracker.ui.theme.PositiveGreen
import com.shayanhaque.politicstracker.util.Formatters
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * Compact pill that color-codes a 24h price move: green up, red down,
 * gray for zero. Used in cards, the detail header, and anywhere else a
 * change needs to be rendered consistently.
 */
@Composable
fun PriceChangeBadge(
    dailyChange: Double,
    modifier: Modifier = Modifier,
) {
    val (bg, fg) = when {
        dailyChange > 0.0005 -> PositiveGreen.copy(alpha = 0.14f) to PositiveGreen
        dailyChange < -0.0005 -> NegativeRed.copy(alpha = 0.14f) to NegativeRed
        else -> NeutralGray.copy(alpha = 0.14f) to NeutralGray
    }
    Text(
        text = Formatters.dailyChange(dailyChange),
        color = fg,
        style = MaterialTheme.typography.labelMedium,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
