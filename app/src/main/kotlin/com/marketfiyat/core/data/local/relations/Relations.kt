package com.marketfiyat.core.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.marketfiyat.core.data.local.entity.ProductEntity
import com.marketfiyat.core.data.local.entity.ProductPriceEntity
import com.marketfiyat.core.data.local.entity.ShoppingListEntity
import com.marketfiyat.core.data.local.entity.ShoppingListItemEntity

data class ProductWithPrices(
    @Embedded val product: ProductEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "product_id"
    )
    val prices: List<ProductPriceEntity>
)

data class ShoppingListWithItems(
    @Embedded val shoppingList: ShoppingListEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "list_id"
    )
    val items: List<ShoppingListItemEntity>
)
