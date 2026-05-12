package com.marketfiyat.core.domain.repository

import com.marketfiyat.core.domain.model.*
import com.marketfiyat.core.util.Result
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getAllProducts(): Flow<List<Product>>
    fun searchProducts(query: String): Flow<List<Product>>
    fun getFavoriteProducts(): Flow<List<Product>>
    suspend fun getProductById(id: Long): Product?
    suspend fun getProductByBarcode(barcode: String): Product?
    suspend fun insertProduct(product: Product): Long
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(productId: Long)
    suspend fun toggleFavorite(productId: Long, isFavorite: Boolean)
    suspend fun setPriceAlarm(productId: Long, threshold: Double?)
    fun getProductCount(): Flow<Int>
}

interface ProductPriceRepository {
    fun getPricesByProductId(productId: Long): Flow<List<ProductPrice>>
    suspend fun getPricesByProductIdSync(productId: Long): List<ProductPrice>
    suspend fun getLatestPrice(productId: Long): ProductPrice?
    suspend fun getLatestPriceByMarket(productId: Long, marketName: String): ProductPrice?
    suspend fun insertPrice(price: ProductPrice): Long
    suspend fun updatePrice(price: ProductPrice)
    suspend fun deletePrice(priceId: Long)
    suspend fun getMinPrice(productId: Long): Double?
    suspend fun getMaxPrice(productId: Long): Double?
    suspend fun getAveragePrice(productId: Long): Double?
    suspend fun getAveragePriceSince(productId: Long, since: Long): Double?
    suspend fun getCheapestPrice(productId: Long): ProductPrice?
    suspend fun getMostExpensivePrice(productId: Long): ProductPrice?
    suspend fun getMarketsForProduct(productId: Long): List<String>
    fun getPricesByDateRange(productId: Long, startDate: Long, endDate: Long): Flow<List<ProductPrice>>
    fun getTotalSpendingByDateRange(startDate: Long, endDate: Long): Flow<Double?>
    fun getSpendingByMarket(startDate: Long): Flow<List<Pair<String, Double>>>
    suspend fun getPricesForChart(productId: Long, since: Long): List<ProductPrice>
    suspend fun getAllPricesForBackup(): List<ProductPrice>
    suspend fun analyzePrices(productId: Long): PriceAnalysis?
    suspend fun getMarketComparison(productId: Long): MarketComparison?
}

interface MarketRepository {
    fun getAllMarkets(): Flow<List<Market>>
    fun getActiveMarkets(): Flow<List<Market>>
    suspend fun getAllMarketsSync(): List<Market>
    fun getDistinctMarketNames(): Flow<List<String>>
    suspend fun getDistinctMarketNamesSync(): List<String>
    suspend fun insertMarket(market: Market): Long
    suspend fun getOrCreateMarket(name: String): Market
    suspend fun updateMarket(market: Market)
    suspend fun deleteMarket(marketId: Long)
}

interface ShoppingListRepository {
    fun getAllLists(): Flow<List<ShoppingList>>
    fun getActiveLists(): Flow<List<ShoppingList>>
    fun getListWithItems(listId: Long): Flow<ShoppingList?>
    suspend fun getListById(listId: Long): ShoppingList?
    suspend fun createList(name: String): Long
    suspend fun updateList(list: ShoppingList)
    suspend fun deleteList(listId: Long)
    suspend fun markListCompleted(listId: Long)
    suspend fun addItemToList(item: ShoppingListItem): Long
    suspend fun updateItem(item: ShoppingListItem)
    suspend fun removeItemFromList(itemId: Long)
    suspend fun toggleItemChecked(itemId: Long, isChecked: Boolean)
    suspend fun deleteCheckedItems(listId: Long)
    suspend fun findBestMarketForItem(productName: String): Pair<String?, Double?>
    suspend fun calculateOptimalShopping(listId: Long): Map<String, List<ShoppingListItem>>
}

interface BackupRepository {
    suspend fun exportToJson(): Result<String>
    suspend fun importFromJson(json: String): Result<Unit>
    suspend fun exportToCsv(): Result<String>
}
