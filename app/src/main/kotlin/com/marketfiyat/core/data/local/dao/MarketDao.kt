package com.marketfiyat.core.data.local.dao

import androidx.room.*
import com.marketfiyat.core.data.local.entity.MarketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMarket(market: MarketEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMarkets(markets: List<MarketEntity>)

    @Update
    suspend fun updateMarket(market: MarketEntity)

    @Delete
    suspend fun deleteMarket(market: MarketEntity)

    @Query("SELECT * FROM markets WHERE id = :id")
    suspend fun getMarketById(id: Long): MarketEntity?

    @Query("SELECT * FROM markets WHERE name = :name LIMIT 1")
    suspend fun getMarketByName(name: String): MarketEntity?

    @Query("SELECT * FROM markets WHERE is_active = 1 ORDER BY name ASC")
    fun getAllActiveMarkets(): Flow<List<MarketEntity>>

    @Query("SELECT * FROM markets ORDER BY name ASC")
    fun getAllMarkets(): Flow<List<MarketEntity>>

    @Query("SELECT * FROM markets ORDER BY name ASC")
    suspend fun getAllMarketsSync(): List<MarketEntity>

    @Query("SELECT DISTINCT market_name FROM product_prices ORDER BY market_name ASC")
    fun getDistinctMarketNames(): Flow<List<String>>

    @Query("SELECT DISTINCT market_name FROM product_prices ORDER BY market_name ASC")
    suspend fun getDistinctMarketNamesSync(): List<String>

    @Query("UPDATE markets SET is_active = :isActive WHERE id = :id")
    suspend fun updateMarketStatus(id: Long, isActive: Boolean)
}
