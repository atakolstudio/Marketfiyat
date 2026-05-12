package com.marketfiyat.core.domain.model

import com.marketfiyat.core.util.UnitType

data class Product(
    val id: Long = 0,
    val name: String,
    val brand: String = "",
    val barcode: String? = null,
    val category: String = "",
    val imageUrl: String? = null,
    val isFavorite: Boolean = false,
    val priceAlarmThreshold: Double? = null,
    val latestPrice: ProductPrice? = null,
    val priceChangePercent: Double? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class ProductPrice(
    val id: Long = 0,
    val productId: Long,
    val marketId: Long? = null,
    val marketName: String,
    val price: Double,
    val discountedPrice: Double? = null,
    val quantity: Double,
    val unit: UnitType,
    val unitPricePerKg: Double? = null,
    val unitPricePer100g: Double? = null,
    val unitPricePerLitre: Double? = null,
    val unitPricePerPiece: Double? = null,
    val effectivePrice: Double,
    val purchaseDate: Long,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class Market(
    val id: Long = 0,
    val name: String,
    val logoUrl: String? = null,
    val colorHex: String? = null,
    val isActive: Boolean = true
)

data class ShoppingList(
    val id: Long = 0,
    val name: String,
    val isCompleted: Boolean = false,
    val estimatedTotal: Double = 0.0,
    val items: List<ShoppingListItem> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class ShoppingListItem(
    val id: Long = 0,
    val listId: Long,
    val productId: Long? = null,
    val productName: String,
    val quantity: Int = 1,
    val isChecked: Boolean = false,
    val bestMarket: String? = null,
    val estimatedPrice: Double? = null,
    val note: String = ""
)

data class MarketComparison(
    val productId: Long,
    val productName: String,
    val marketPrices: List<MarketPrice>,
    val cheapestMarket: String,
    val mostExpensiveMarket: String,
    val averagePrice: Double,
    val unitType: UnitType
)

data class MarketPrice(
    val marketName: String,
    val price: Double,
    val unitPrice: Double?,
    val lastPurchaseDate: Long,
    val isCheapest: Boolean = false
)

data class PriceAnalysis(
    val productId: Long,
    val currentPrice: Double,
    val averagePrice: Double,
    val minPrice: Double,
    val maxPrice: Double,
    val lastPrice: Double?,
    val priceChangePercent: Double?,
    val isLowestIn6Months: Boolean,
    val analysisMessage: String,
    val recommendation: PriceRecommendation
)

enum class PriceRecommendation {
    BUY_NOW,
    WAIT,
    NEUTRAL,
    GOOD_DEAL
}

data class PriceHistory(
    val prices: List<ProductPrice>,
    val priceChangePercent: Double?,
    val cheapestDate: Long?,
    val mostExpensiveDate: Long?,
    val trend: PriceTrend
)

enum class PriceTrend {
    INCREASING,
    DECREASING,
    STABLE
}

data class Statistics(
    val totalSpending: Double,
    val monthlySpending: List<MonthlySpending>,
    val marketSpending: List<MarketSpendingStat>,
    val mostPurchasedProducts: List<Product>,
    val mostIncreasedProducts: List<ProductWithIncrease>,
    val totalSavings: Double
)

data class MonthlySpending(
    val year: Int,
    val month: Int,
    val total: Double
)

data class MarketSpendingStat(
    val marketName: String,
    val total: Double,
    val percentage: Double
)

data class ProductWithIncrease(
    val product: Product,
    val increasePercent: Double
)
