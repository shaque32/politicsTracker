package com.shayanhaque.politicstracker.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object Formatters {

    private val closeDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
    private val chartDateFormat = SimpleDateFormat("MMM d", Locale.US)

    fun probability(value: Double): String =
        "${(value.coerceIn(0.0, 1.0) * 100).toInt()}¢"

    fun dailyChange(value: Double): String {
        val pct = value * 100.0
        val sign = when {
            pct > 0 -> "+"
            pct < 0 -> "-"
            else -> ""
        }
        return "$sign${"%.1f".format(abs(pct))}%"
    }

    fun volume(usd: Double?): String {
        if (usd == null) return "—"
        return when {
            usd >= 1_000_000 -> "$${"%.1f".format(usd / 1_000_000)}M"
            usd >= 1_000 -> "$${"%.1f".format(usd / 1_000)}K"
            else -> NumberFormat.getCurrencyInstance(Locale.US).format(usd)
        }
    }

    fun closeDate(millis: Long?): String =
        millis?.let { closeDateFormat.format(Date(it)) } ?: "Ongoing"

    fun chartDate(millis: Long): String = chartDateFormat.format(Date(millis))
}
