package com.shayanhaque.politicstracker.data.remote

import com.google.common.truth.Truth.assertThat
import com.shayanhaque.politicstracker.data.remote.dto.MarketDto
import com.shayanhaque.politicstracker.data.remote.dto.MarketMapper
import com.shayanhaque.politicstracker.model.MarketCategory
import org.junit.Test

class MarketMapperTest {

    @Test
    fun `outcomePrices JSON string is parsed for probability`() {
        val dto = MarketDto(
            id = "1",
            slug = "slug",
            question = "Q?",
            description = null,
            outcomePrices = "[\"0.62\",\"0.38\"]",
            lastTradePrice = null,
            oneDayPriceChange = null,
            volume = null,
            liquidity = null,
            endDateIso = null,
            active = true,
            closed = false,
            featured = false,
            category = null,
            tags = listOf("senate race"),
        )

        val domain = MarketMapper.toDomain(dto)!!

        assertThat(domain.probability).isWithin(1e-9).of(0.62)
        assertThat(domain.category).isEqualTo(MarketCategory.Congress)
    }

    @Test
    fun `malformed dto returns null rather than throwing`() {
        val dto = MarketDto(
            id = null, slug = null, question = null, description = null,
            outcomePrices = null, lastTradePrice = null, oneDayPriceChange = null,
            volume = null, liquidity = null, endDateIso = null,
            active = null, closed = null, featured = null, category = null, tags = null,
        )

        assertThat(MarketMapper.toDomain(dto)).isNull()
    }
}
