package com.marketfiyat.core.data.local.entity

import com.marketfiyat.core.domain.model.*
import com.marketfiyat.core.util.UnitType

// ProductEntity <-> Product
fun ProductEntity.toDomain(): Product = Product(
    id = id,
    name = name,
    brand = brand,
    barcode = barcode,
    category = category,
    imageUrl = imageUrl,
    isFavorite = isFavorite,
    priceAlarmThreshold = priceAlarmThreshold,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    name = name,
    brand = brand,
    barcode = barcode,
    category = category,
    imageUrl = imageUrl,
    isFavorite = isFavorite,
    priceAlarmThreshold = priceAlarmThreshold,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// ProductPriceEntity <-> ProductPrice
fun ProductPriceEntity.toDomain(): ProductPrice = ProductPrice(
    id = id,
    productId = productId,
    marketId = marketId,
    marketName = marketName,
    price = price,
    discountedPrice = discountedPrice,
    quantity = quantity,
    unit = UnitType.fromString(unit),
    unitPricePerKg = unitPricePerKg,
    unitPricePer100g = unitPricePer100g,
    unitPricePerLitre = unitPricePerLitre,
    unitPricePerPiece = unitPricePerPiece,
    effectivePrice = effectivePrice,
    purchaseDate = purchaseDate,
    note = note,
    createdAt = createdAt
)

fun ProductPrice.toEntity(): ProductPriceEntity = ProductPriceEntity(
    id = id,
    productId = productId,
    marketId = marketId,
    marketName = marketName,
    price = price,
    discountedPrice = discountedPrice,
    quantity = quantity,
    unit = unit.name,
    unitPricePerKg = unitPricePerKg,
    unitPricePer100g = unitPricePer100g,
    unitPricePerLitre = unitPricePerLitre,
    unitPricePerPiece = unitPricePerPiece,
    effectivePrice = effectivePrice,
    purchaseDate = purchaseDate,
    note = note,
    createdAt = createdAt
)

// MarketEntity <-> Market
fun MarketEntity.toDomain(): Market = Market(
    id = id,
    name = name,
    logoUrl = logoUrl,
    colorHex = colorHex,
    isActive = isActive
)

fun Market.toEntity(): MarketEntity = MarketEntity(
    id = id,
    name = name,
    logoUrl = logoUrl,
    colorHex = colorHex,
    isActive = isActive
)

// ShoppingListEntity <-> ShoppingList
fun ShoppingListEntity.toDomain(items: List<ShoppingListItem> = emptyList()): ShoppingList = ShoppingList(
    id = id,
    name = name,
    isCompleted = isCompleted,
    estimatedTotal = estimatedTotal,
    items = items,
    createdAt = createdAt
)

fun ShoppingList.toEntity(): ShoppingListEntity = ShoppingListEntity(
    id = id,
    name = name,
    isCompleted = isCompleted,
    estimatedTotal = estimatedTotal,
    createdAt = createdAt
)

// ShoppingListItemEntity <-> ShoppingListItem
fun ShoppingListItemEntity.toDomain(): ShoppingListItem = ShoppingListItem(
    id = id,
    listId = listId,
    productId = productId,
    productName = productName,
    quantity = quantity,
    isChecked = isChecked,
    bestMarket = bestMarket,
    estimatedPrice = estimatedPrice,
    note = note
)

fun ShoppingListItem.toEntity(): ShoppingListItemEntity = ShoppingListItemEntity(
    id = id,
    listId = listId,
    productId = productId,
    productName = productName,
    quantity = quantity,
    isChecked = isChecked,
    bestMarket = bestMarket,
    estimatedPrice = estimatedPrice,
    note = note
)
