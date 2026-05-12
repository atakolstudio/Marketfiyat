package com.marketfiyat.core.data.repository

import com.marketfiyat.core.data.local.dao.MarketDao
import com.marketfiyat.core.data.local.entity.toDomain
import com.marketfiyat.core.data.local.entity.toEntity
import com.marketfiyat.core.domain.model.Market
import com.marketfiyat.core.domain.repository.MarketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketRepositoryImpl @Inject constructor(
    private val marketDao: MarketDao
) : MarketRepository {

    override fun getAllMarkets(): Flow<List<Market>> =
        marketDao.getAllMarkets().map { it.map { entity -> entity.toDomain() } }

    override fun getActiveMarkets(): Flow<List<Market>> =
        marketDao.getAllActiveMarkets().map { it.map { entity -> entity.toDomain() } }

    override suspend fun getAllMarketsSync(): List<Market> =
        marketDao.getAllMarketsSync().map { it.toDomain() }

    override fun getDistinctMarketNames(): Flow<List<String>> =
        marketDao.getDistinctMarketNames()

    override suspend fun getDistinctMarketNamesSync(): List<String> =
        marketDao.getDistinctMarketNamesSync()

    override suspend fun insertMarket(market: Market): Long =
        marketDao.insertMarket(market.toEntity())

    override suspend fun getOrCreateMarket(name: String): Market {
        val existing = marketDao.getMarketByName(name)
        if (existing != null) return existing.toDomain()

        val newMarket = Market(name = name)
        val id = marketDao.insertMarket(newMarket.toEntity())
        return newMarket.copy(id = id)
    }

    override suspend fun updateMarket(market: Market) =
        marketDao.updateMarket(market.toEntity())

    override suspend fun deleteMarket(marketId: Long) {
        val market = marketDao.getMarketById(marketId)
        market?.let { marketDao.deleteMarket(it) }
    }
}
