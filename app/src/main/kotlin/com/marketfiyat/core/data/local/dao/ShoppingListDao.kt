package com.marketfiyat.core.data.local.dao

import androidx.room.*
import com.marketfiyat.core.data.local.entity.ShoppingListEntity
import com.marketfiyat.core.data.local.entity.ShoppingListItemEntity
import com.marketfiyat.core.data.local.relations.ShoppingListWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: ShoppingListEntity): Long

    @Update
    suspend fun updateList(list: ShoppingListEntity)

    @Delete
    suspend fun deleteList(list: ShoppingListEntity)

    @Query("DELETE FROM shopping_lists WHERE id = :id")
    suspend fun deleteListById(id: Long)

    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    suspend fun getListById(id: Long): ShoppingListEntity?

    @Query("SELECT * FROM shopping_lists ORDER BY created_at DESC")
    fun getAllLists(): Flow<List<ShoppingListEntity>>

    @Query("SELECT * FROM shopping_lists WHERE is_completed = 0 ORDER BY created_at DESC")
    fun getActiveLists(): Flow<List<ShoppingListEntity>>

    @Transaction
    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    fun getListWithItems(id: Long): Flow<ShoppingListWithItems?>

    @Transaction
    @Query("SELECT * FROM shopping_lists ORDER BY created_at DESC")
    fun getAllListsWithItems(): Flow<List<ShoppingListWithItems>>

    @Query("UPDATE shopping_lists SET is_completed = :isCompleted, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateListCompletedStatus(id: Long, isCompleted: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE shopping_lists SET estimated_total = :total, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateEstimatedTotal(id: Long, total: Double, updatedAt: Long = System.currentTimeMillis())

    // Items
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingListItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ShoppingListItemEntity>)

    @Update
    suspend fun updateItem(item: ShoppingListItemEntity)

    @Delete
    suspend fun deleteItem(item: ShoppingListItemEntity)

    @Query("DELETE FROM shopping_list_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("SELECT * FROM shopping_list_items WHERE list_id = :listId ORDER BY created_at ASC")
    fun getItemsByListId(listId: Long): Flow<List<ShoppingListItemEntity>>

    @Query("UPDATE shopping_list_items SET is_checked = :isChecked WHERE id = :id")
    suspend fun updateItemCheckedStatus(id: Long, isChecked: Boolean)

    @Query("UPDATE shopping_list_items SET best_market = :market, estimated_price = :price WHERE id = :id")
    suspend fun updateItemBestMarket(id: Long, market: String?, price: Double?)

    @Query("DELETE FROM shopping_list_items WHERE list_id = :listId AND is_checked = 1")
    suspend fun deleteCheckedItems(listId: Long)
}
