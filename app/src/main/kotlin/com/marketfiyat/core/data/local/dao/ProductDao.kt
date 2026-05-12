package com.marketfiyat.core.data.local.dao

import androidx.room.*
import com.marketfiyat.core.data.local.entity.ProductEntity
import com.marketfiyat.core.data.local.relations.ProductWithPrices
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Long)

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductByIdFlow(id: Long): Flow<ProductEntity?>

    @Query("SELECT * FROM products ORDER BY updated_at DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE is_favorite = 1 ORDER BY name ASC")
    fun getFavoriteProducts(): Flow<List<ProductEntity>>

    @Query("UPDATE products SET is_favorite = :isFavorite, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE products SET price_alarm_threshold = :threshold, updated_at = :updatedAt WHERE id = :id")
    suspend fun updatePriceAlarm(id: Long, threshold: Double?, updatedAt: Long = System.currentTimeMillis())

    @Transaction
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductWithPrices(id: Long): ProductWithPrices?

    @Transaction
    @Query("SELECT * FROM products ORDER BY updated_at DESC")
    fun getAllProductsWithPrices(): Flow<List<ProductWithPrices>>

    @Transaction
    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchProductsWithPrices(query: String): Flow<List<ProductWithPrices>>

    @Query("SELECT COUNT(*) FROM products")
    fun getProductCount(): Flow<Int>

    @Query("""
        SELECT p.* FROM products p
        INNER JOIN product_prices pp ON p.id = pp.product_id
        WHERE p.price_alarm_threshold IS NOT NULL
        AND pp.effective_price <= p.price_alarm_threshold
    """)
    fun getProductsWithActiveAlarms(): Flow<List<ProductEntity>>

    // Sort queries
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProductsSortedByName(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY created_at DESC")
    fun getAllProductsSortedByNewest(): Flow<List<ProductEntity>>

    @Query("""
        SELECT p.* FROM products p
        LEFT JOIN product_prices pp ON p.id = pp.product_id
        GROUP BY p.id
        ORDER BY MIN(pp.effective_price) ASC
    """)
    fun getAllProductsSortedByCheapest(): Flow<List<ProductEntity>>
}
