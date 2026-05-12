package com.marketfiyat.core.data.local.dao

import androidx.room.*
import com.marketfiyat.core.data.local.entity.BarcodeCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BarcodeCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: BarcodeCacheEntity): Long

    @Update
    suspend fun updateCache(cache: BarcodeCacheEntity)

    @Delete
    suspend fun deleteCache(cache: BarcodeCacheEntity)

    @Query("SELECT * FROM barcode_cache WHERE barcode = :barcode LIMIT 1")
    suspend fun getCacheByBarcode(barcode: String): BarcodeCacheEntity?

    @Query("SELECT * FROM barcode_cache WHERE product_id = :productId LIMIT 1")
    suspend fun getCacheByProductId(productId: Long): BarcodeCacheEntity?

    @Query("SELECT * FROM barcode_cache ORDER BY cached_at DESC")
    fun getAllCache(): Flow<List<BarcodeCacheEntity>>

    @Query("DELETE FROM barcode_cache WHERE cached_at < :before")
    suspend fun deleteOldCache(before: Long)

    @Query("DELETE FROM barcode_cache WHERE product_id = :productId")
    suspend fun deleteCacheByProductId(productId: Long)
}
