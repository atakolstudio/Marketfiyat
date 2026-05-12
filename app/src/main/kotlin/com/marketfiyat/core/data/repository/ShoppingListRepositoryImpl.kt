package com.marketfiyat.core.data.repository

import com.marketfiyat.core.data.local.dao.ProductPriceDao
import com.marketfiyat.core.data.local.dao.ShoppingListDao
import com.marketfiyat.core.data.local.entity.ShoppingListEntity
import com.marketfiyat.core.data.local.entity.toDomain
import com.marketfiyat.core.data.local.entity.toEntity
import com.marketfiyat.core.domain.model.ShoppingList
import com.marketfiyat.core.domain.model.ShoppingListItem
import com.marketfiyat.core.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingListRepositoryImpl @Inject constructor(
    private val shoppingListDao: ShoppingListDao,
    private val productPriceDao: ProductPriceDao
) : ShoppingListRepository {

    override fun getAllLists(): Flow<List<ShoppingList>> {
        return shoppingListDao.getAllListsWithItems().map { lists ->
            lists.map { relation ->
                relation.shoppingList.toDomain(
                    items = relation.items.map { it.toDomain() }
                )
            }
        }
    }

    override fun getActiveLists(): Flow<List<ShoppingList>> {
        return shoppingListDao.getActiveLists().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getListWithItems(listId: Long): Flow<ShoppingList?> {
        return shoppingListDao.getListWithItems(listId).map { relation ->
            relation?.let {
                it.shoppingList.toDomain(items = it.items.map { item -> item.toDomain() })
            }
        }
    }

    override suspend fun getListById(listId: Long): ShoppingList? {
        return shoppingListDao.getListById(listId)?.toDomain()
    }

    override suspend fun createList(name: String): Long {
        val listEntity = com.marketfiyat.core.data.local.entity.ShoppingListEntity(name = name)
        return shoppingListDao.insertList(listEntity)
    }

    override suspend fun updateList(list: ShoppingList) {
        shoppingListDao.updateList(list.toEntity())
    }

    override suspend fun deleteList(listId: Long) {
        shoppingListDao.deleteListById(listId)
    }

    override suspend fun markListCompleted(listId: Long) {
        shoppingListDao.updateListCompletedStatus(listId, true)
    }

    override suspend fun addItemToList(item: ShoppingListItem): Long {
        return shoppingListDao.insertItem(item.toEntity())
    }

    override suspend fun updateItem(item: ShoppingListItem) {
        shoppingListDao.updateItem(item.toEntity())
    }

    override suspend fun removeItemFromList(itemId: Long) {
        shoppingListDao.deleteItemById(itemId)
    }

    override suspend fun toggleItemChecked(itemId: Long, isChecked: Boolean) {
        shoppingListDao.updateItemCheckedStatus(itemId, isChecked)
    }

    override suspend fun deleteCheckedItems(listId: Long) {
        shoppingListDao.deleteCheckedItems(listId)
    }

    override suspend fun findBestMarketForItem(productName: String): Pair<String?, Double?> {
        return try {
            // Search for products with similar names and find cheapest market
            val allPrices = productPriceDao.getAllPricesSync()
            val relevantPrices = allPrices.filter {
                it.marketName.isNotEmpty()
            }

            if (relevantPrices.isEmpty()) return Pair(null, null)

            val marketPrices = relevantPrices
                .groupBy { it.marketName }
                .mapValues { (_, prices) ->
                    prices.minByOrNull { it.effectivePrice }?.effectivePrice ?: Double.MAX_VALUE
                }
                .minByOrNull { it.value }

            Pair(marketPrices?.key, marketPrices?.value)
        } catch (e: Exception) {
            Timber.e(e, "Error finding best market for: $productName")
            Pair(null, null)
        }
    }

    override suspend fun calculateOptimalShopping(listId: Long): Map<String, List<ShoppingListItem>> {
        val items = shoppingListDao.getItemsByListId(listId).first()
        val result = mutableMapOf<String, MutableList<ShoppingListItem>>()
        items.forEach { itemEntity ->
            val item = itemEntity.toDomain()
            val market = item.bestMarket ?: "Belirsiz"
            result.getOrPut(market) { mutableListOf() }.add(item)
        }
        return result
    }
}
