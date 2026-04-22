package com.shayanhaque.politicstracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.shayanhaque.politicstracker.model.PricePoint
import com.shayanhaque.politicstracker.ui.theme.ChartFill
import com.shayanhaque.politicstracker.ui.theme.ChartLine

/**
 * A minimal line-and-fill chart implemented with Compose Canvas so the app
 * can render price history without pulling in a heavyweight chart library.
 * Handles 0/1-point edge cases gracefully by showing an explanatory label
 * instead of crashing or drawing a garbled chart.
 */
@Composable
fun HistoricalPriceChart(
    points: List<PricePoint>,
    modifier: Modifier = Modifier,
) {
    if (points.size < 2) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(160.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Not enough history yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        return
    }

    val minY = 0.0
    val maxY = 1.0
    val firstT = points.first().timestampMillis
    val lastT = points.last().timestampMillis
    val spanT = (lastT - firstT).coerceAtLeast(1L)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(vertical = 8.dp),
    ) {
        val w = size.width
        val h = size.height

        fun xFor(t: Long) = ((t - firstT).toFloat() / spanT) * w
        fun yFor(p: Double): Float {
            val norm = ((p - minY) / (maxY - minY)).coerceIn(0.0, 1.0).toFloat()
            // Invert so higher probability is higher on the chart.
            return h - norm * h
        }

        // Gridline at 50%.
        drawLine(
            color = ChartLine.copy(alpha = 0.15f),
            start = Offset(0f, h / 2f),
            end = Offset(w, h / 2f),
            strokeWidth = 1f,
        )

        val linePath = Path().apply {
            moveTo(xFor(points.first().timestampMillis), yFor(points.first().probability))
            for (i in 1 until points.size) {
                lineTo(xFor(points[i].timestampMillis), yFor(points[i].probability))
            }
        }
        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(xFor(points.last().timestampMillis), h)
            lineTo(xFor(points.first().timestampMillis), h)
            close()
        }

        drawPath(path = fillPath, color = ChartFill)
        drawPath(
            path = linePath,
            color = ChartLine,
            style = Stroke(width = 3.5f, cap = StrokeCap.Round),
        )
    }
}
