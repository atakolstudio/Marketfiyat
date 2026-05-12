package com.marketfiyat.core.data.repository

import com.marketfiyat.core.data.local.dao.ProductPriceDao
import com.marketfiyat.core.data.local.entity.toDomain
import com.marketfiyat.core.data.local.entity.toEntity
import com.marketfiyat.core.domain.model.*
import com.marketfiyat.core.domain.repository.ProductPriceRepository
import com.marketfiyat.core.util.Constants
import com.marketfiyat.core.util.UnitType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductPriceRepositoryImpl @Inject constructor(
    private val productPriceDao: ProductPriceDao
) : ProductPriceRepository {

    override fun getPricesByProductId(productId: Long): Flow<List<ProductPrice>> {
        return productPriceDao.getPricesByProductId(productId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getPricesByProductIdSync(productId: Long): List<ProductPrice> {
        return productPriceDao.getPricesByProductIdSync(productId).map { it.toDomain() }
    }

    override suspend fun getLatestPrice(productId: Long): ProductPrice? {
        return productPriceDao.getLatestPriceForProduct(productId)?.toDomain()
    }

    override suspend fun getLatestPriceByMarket(productId: Long, marketName: String): ProductPrice? {
        return productPriceDao.getLatestPriceByMarket(productId, marketName)?.toDomain()
    }

    override suspend fun insertPrice(price: ProductPrice): Long {
        return try {
            productPriceDao.insertPrice(price.toEntity())
        } catch (e: Exception) {
            Timber.e(e, "Error inserting price for product: ${price.productId}")
            throw e
        }
    }

    override suspend fun updatePrice(price: ProductPrice) {
        productPriceDao.updatePrice(price.toEntity())
    }

    override suspend fun deletePrice(priceId: Long) {
        productPriceDao.deletePriceById(priceId)
    }

    override suspend fun getMinPrice(productId: Long): Double? {
        return productPriceDao.getMinPrice(productId)
    }

    override suspend fun getMaxPrice(productId: Long): Double? {
        return productPriceDao.getMaxPrice(productId)
    }

    override suspend fun getAveragePrice(productId: Long): Double? {
        return productPriceDao.getAveragePrice(productId)
    }

    override suspend fun getAveragePriceSince(productId: Long, since: Long): Double? {
        return productPriceDao.getAveragePriceSince(productId, since)
    }

    override suspend fun getCheapestPrice(productId: Long): ProductPrice? {
        return productPriceDao.getCheapestPrice(productId)?.toDomain()
    }

    override suspend fun getMostExpensivePrice(productId: Long): ProductPrice? {
        return productPriceDao.getMostExpensivePrice(productId)?.toDomain()
    }

    override suspend fun getMarketsForProduct(productId: Long): List<String> {
        return productPriceDao.getMarketsForProduct(productId)
    }

    override fun getPricesByDateRange(
        productId: Long,
        startDate: Long,
        endDate: Long
    ): Flow<List<ProductPrice>> {
        return productPriceDao.getPricesByDateRange(productId, startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTotalSpendingByDateRange(startDate: Long, endDate: Long): Flow<Double?> {
        return productPriceDao.getTotalSpendingByDateRange(startDate, endDate)
    }

    override fun getSpendingByMarket(startDate: Long): Flow<List<Pair<String, Double>>> {
        return productPriceDao.getSpendingByMarket(startDate).map { list ->
            list.map { Pair(it.marketName, it.total) }
        }
    }

    override suspend fun getPricesForChart(productId: Long, since: Long): List<ProductPrice> {
        return productPriceDao.getPricesForChartSince(productId, since).map { it.toDomain() }
    }

    override suspend fun getAllPricesForBackup(): List<ProductPrice> {
        return productPriceDao.getAllPricesSync().map { it.toDomain() }
    }

    override suspend fun analyzePrices(productId: Long): PriceAnalysis? {
        val prices = getPricesByProductIdSync(productId)
        if (prices.size < Constants.MIN_PRICES_FOR_ANALYSIS) return null

        val sortedPrices = prices.sortedByDescending { it.purchaseDate }
        val currentPrice = sortedPrices.first().effectivePrice
        val lastPrice = sortedPrices.getOrNull(1)?.effectivePrice

        val avgPrice = getAveragePrice(productId) ?: return null
        val minPrice = getMinPrice(productId) ?: return null
        val maxPrice = getMaxPrice(productId) ?: return null

        val sixMonthsAgo = System.currentTimeMillis() - (Constants.PRICE_ANALYSIS_WINDOW_DAYS * 24 * 60 * 60 * 1000)
        val minIn6Months = prices
            .filter { it.purchaseDate >= sixMonthsAgo }
            .minOfOrNull { it.effectivePrice }

        val isLowestIn6Months = minIn6Months != null && currentPrice <= minIn6Months

        val priceChangePercent = lastPrice?.let {
            ((currentPrice - it) / it) * 100
        }

        val diffFromAvg = ((currentPrice - avgPrice) / avgPrice) * 100

        val analysisMessage = when {
            isLowestIn6Months -> "Son 6 ayın en ucuz fiyatı! 🎉"
            priceChangePercent != null && priceChangePercent > Constants.SIGNIFICANT_PRICE_CHANGE_PERCENT ->
                "Son alışa göre %.1f%% zamlandı 📈".format(priceChangePercent)
            priceChangePercent != null && priceChangePercent < -Constants.SIGNIFICANT_PRICE_CHANGE_PERCENT ->
                "Son alışa göre %.1f%% ucuzladı 📉".format(-priceChangePercent)
            diffFromAvg > Constants.SIGNIFICANT_PRICE_CHANGE_PERCENT ->
                "Geçmiş ortalamadan %.1f%% daha pahalı ⚠️".format(diffFromAvg)
            diffFromAvg < -Constants.SIGNIFICANT_PRICE_CHANGE_PERCENT ->
                "Geçmiş ortalamadan %.1f%% daha ucuz ✅".format(-diffFromAvg)
            else -> "Fiyat ortalamalara yakın"
        }

        val recommendation = when {
            isLowestIn6Months -> PriceRecommendation.BUY_NOW
            currentPrice <= minPrice * 1.05 -> PriceRecommendation.GOOD_DEAL
            currentPrice >= maxPrice * 0.95 -> PriceRecommendation.WAIT
            diffFromAvg > 10 -> PriceRecommendation.WAIT
            else -> PriceRecommendation.NEUTRAL
        }

        return PriceAnalysis(
            productId = productId,
            currentPrice = currentPrice,
            averagePrice = avgPrice,
            minPrice = minPrice,
            maxPrice = maxPrice,
            lastPrice = lastPrice,
            priceChangePercent = priceChangePercent,
            isLowestIn6Months = isLowestIn6Months,
            analysisMessage = analysisMessage,
            recommendation = recommendation
        )
    }

    override suspend fun getMarketComparison(productId: Long): MarketComparison? {
        val prices = getPricesByProductIdSync(productId)
        if (prices.isEmpty()) return null

        val marketGroups = prices.groupBy { it.marketName }
        val marketPrices = marketGroups.map { (market, marketPriceList) ->
            val latestPrice = marketPriceList.maxByOrNull { it.purchaseDate }!!
            val unitPrice = latestPrice.unitPricePerKg
                ?: latestPrice.unitPricePerLitre
                ?: latestPrice.unitPricePerPiece
            MarketPrice(
                marketName = market,
                price = latestPrice.effectivePrice,
                unitPrice = unitPrice,
                lastPurchaseDate = latestPrice.purchaseDate
            )
        }.sortedBy { it.price }

        if (marketPrices.isEmpty()) return null

        val cheapest = marketPrices.first()
        val mostExpensive = marketPrices.last()
        val avgPrice = marketPrices.map { it.price }.average()
        val productName = prices.first().let { it.productId.toString() }
        val unitType = prices.first().unit

        val updatedMarketPrices = marketPrices.mapIndexed { index, mp ->
            mp.copy(isCheapest = index == 0)
        }

        return MarketComparison(
            productId = productId,
            productName = productName,
            marketPrices = updatedMarketPrices,
            cheapestMarket = cheapest.marketName,
            mostExpensiveMarket = mostExpensive.marketName,
            averagePrice = avgPrice,
            unitType = unitType
        )
    }
}
