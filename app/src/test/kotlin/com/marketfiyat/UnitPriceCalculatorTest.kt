package com.marketfiyat.core.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UnitPriceCalculatorTest {

    @Test
    fun `calculateUnitPrices for gram converts correctly`() {
        // 700g at 189 TL → KG should be ~270 TL
        val prices = UnitPriceCalculator.calculateUnitPrices(189.0, 700.0, UnitType.GRAM)
        assertThat(prices.perKg).isWithin(0.01).of(270.0)
        assertThat(prices.per100g).isWithin(0.01).of(27.0)
        assertThat(prices.perLitre).isNull()
    }

    @Test
    fun `calculateUnitPrices for kilogram returns correct per kg`() {
        // 1.5 kg at 45 TL → KG should be 30 TL
        val prices = UnitPriceCalculator.calculateUnitPrices(45.0, 1.5, UnitType.KILOGRAM)
        assertThat(prices.perKg).isWithin(0.01).of(30.0)
        assertThat(prices.per100g).isWithin(0.01).of(3.0)
    }

    @Test
    fun `calculateUnitPrices for litre converts correctly`() {
        // 500ml at 25 TL → per litre should be 50 TL
        val prices = UnitPriceCalculator.calculateUnitPrices(25.0, 500.0, UnitType.MILLILITRE)
        assertThat(prices.perLitre).isWithin(0.01).of(50.0)
    }

    @Test
    fun `calculateUnitPrices for piece returns per piece`() {
        val prices = UnitPriceCalculator.calculateUnitPrices(30.0, 6.0, UnitType.PIECE)
        assertThat(prices.perPiece).isWithin(0.01).of(5.0)
    }

    @Test
    fun `getComparableUnitPrice returns kg price for weight units`() {
        val result = UnitPriceCalculator.getComparableUnitPrice(189.0, 700.0, UnitType.GRAM)
        assertThat(result).isWithin(0.01).of(270.0)
    }
}

class PriceFormatTest {

    @Test
    fun `formatPrice formats whole number without decimals`() {
        val result = 100.0.formatPrice()
        assertThat(result).isEqualTo("100 TL")
    }

    @Test
    fun `formatPrice formats decimal number with 2 places`() {
        val result = 45.99.formatPrice()
        assertThat(result).isEqualTo("45.99 TL")
    }

    @Test
    fun `formatPercent formats correctly`() {
        val result = 12.5.formatPercent()
        assertThat(result).isEqualTo("12.5%")
    }
}
