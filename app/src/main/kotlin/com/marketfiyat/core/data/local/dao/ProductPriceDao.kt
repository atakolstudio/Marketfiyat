package com.marketfiyat.core.data.local.dao

import androidx.room.*
import com.marketfiyat.core.data.local.entity.ProductPriceEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface ProductPriceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrice(price: ProductPriceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrices(prices: List<ProductPriceEntity>)

    @Update
    suspend fun updatePrice(price: ProductPriceEntity)

    @Delete
    suspend fun deletePrice(price: ProductPriceEntity)

    @Query("DELETE FROM product_prices WHERE id = :id")
    suspend fun deletePriceById(id: Long)

    @Query("SELECT * FROM product_prices WHERE id = :id")
    suspend fun getPriceById(id: Long): ProductPriceEntity?

    @Query("SELECT * FROM product_prices WHERE product_id = :productId ORDER BY purchase_date DESC")
    fun getPricesByProductId(productId: Long): Flow<List<ProductPriceEntity>>

    @Query("SELECT * FROM product_prices WHERE product_id = :productId ORDER BY purchase_date DESC")
    suspend fun getPricesByProductIdSync(productId: Long): List<ProductPriceEntity>

    @Query("""
        SELECT * FROM product_prices 
        WHERE product_id = :productId 
        ORDER BY purchase_date DESC 
        LIMIT 1
    """)
    suspend fun getLatestPriceForProduct(productId: Long): ProductPriceEntity?

    @Query("""
        SELECT * FROM product_prices 
        WHERE product_id = :productId 
        ORDER BY purchase_date DESC 
        LIMIT 1
    """)
    fun getLatestPriceForProductFlow(productId: Long): Flow<ProductPriceEntity?>

    @Query("""
        SELECT * FROM product_prices
        WHERE product_id = :productId
        AND market_name = :marketName
        ORDER BY purchase_date DESC
        LIMIT 1
    """)
    suspend fun getLatestPriceByMarket(productId: Long, marketName: String): ProductPriceEntity?

    @Query("""
        SELECT * FROM product_prices
        WHERE product_id = :productId
        AND market_name = :marketName
        ORDER BY purchase_date DESC
    """)
    fun getPricesByProductAndMarket(productId: Long, marketName: String): Flow<List<ProductPriceEntity>>

    @Query("""
        SELECT MIN(effective_price) FROM product_prices
        WHERE product_id = :productId
    """)
    suspend fun getMinPrice(productId: Long): Double?

    @Query("""
        SELECT MAX(effective_price) FROM product_prices
        WHERE product_id = :productId
    """)
    suspend fun getMaxPrice(productId: Long): Double?

    @Query("""
        SELECT AVG(effective_price) FROM product_prices
        WHERE product_id = :productId
    """)
    suspend fun getAveragePrice(productId: Long): Double?

    @Query("""
        SELECT AVG(effective_price) FROM product_prices
        WHERE product_id = :productId
        AND purchase_date >= :since
    """)
    suspend fun getAveragePriceSince(productId: Long, since: Long): Double?

    @Query("""
        SELECT * FROM product_prices
        WHERE product_id = :productId
        ORDER BY effective_price ASC
        LIMIT 1
    """)
    suspend fun getCheapestPrice(productId: Long): ProductPriceEntity?

    @Query("""
        SELECT * FROM product_prices
        WHERE product_id = :productId
        ORDER BY effective_price DESC
        LIMIT 1
    """)
    suspend fun getMostExpensivePrice(productId: Long): ProductPriceEntity?

    @Query("""
        SELECT DISTINCT market_name FROM product_prices
        WHERE product_id = :productId
        ORDER BY market_name ASC
    """)
    suspend fun getMarketsForProduct(productId: Long): List<String>

    @Query("""
        SELECT * FROM product_prices
        WHERE product_id = :productId
        AND purchase_date >= :startDate
        AND purchase_date <= :endDate
        ORDER BY purchase_date DESC
    """)
    fun getPricesByDateRange(productId: Long, startDate: Long, endDate: Long): Flow<List<ProductPriceEntity>>

    // Statistics queries
    @Query("""
        SELECT SUM(effective_price) FROM product_prices
        WHERE purchase_date >= :startDate AND purchase_date <= :endDate
    """)
    fun getTotalSpendingByDateRange(startDate: Long, endDate: Long): Flow<Double?>

    @Query("""
        SELECT market_name, SUM(effective_price) as total
        FROM product_prices
        WHERE purchase_date >= :startDate
        GROUP BY market_name
        ORDER BY total DESC
    """)
    fun getSpendingByMarket(startDate: Long): Flow<List<MarketSpending>>

    @Query("""
        SELECT product_id, COUNT(*) as purchase_count
        FROM product_prices
        GROUP BY product_id
        ORDER BY purchase_count DESC
        LIMIT :limit
    """)
    fun getMostPurchasedProducts(limit: Int = 10): Flow<List<ProductPurchaseCount>>

    @Query("""
        SELECT * FROM product_prices
        WHERE purchase_date >= :startDate
        ORDER BY created_at DESC
    """)
    fun getRecentPrices(startDate: Long): Flow<List<ProductPriceEntity>>

    @Query("SELECT * FROM product_prices ORDER BY purchase_date DESC")
    suspend fun getAllPricesSync(): List<ProductPriceEntity>

    @Query("""
        SELECT * FROM product_prices
        WHERE purchase_date >= :since
        AND product_id = :productId
        ORDER BY purchase_date ASC
    """)
    suspend fun getPricesForChartSince(productId: Long, since: Long): List<ProductPriceEntity>
}

data class MarketSpending(
    @ColumnInfo(name = "market_name") val marketName: String,
    @ColumnInfo(name = "total") val total: Double
)

data class ProductPurchaseCount(
    @ColumnInfo(name = "product_id") val productId: Long,
    @ColumnInfo(name = "purchase_count") val purchaseCount: Int
)
