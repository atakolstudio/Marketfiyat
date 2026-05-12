package com.marketfiyat.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "product_prices",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MarketEntity::class,
            parentColumns = ["id"],
            childColumns = ["market_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["product_id"]),
        Index(value = ["market_id"]),
        Index(value = ["purchase_date"]),
        Index(value = ["product_id", "market_id"]),
        Index(value = ["product_id", "purchase_date"]),
        Index(value = ["unit_price_per_kg"]),
        Index(value = ["unit_price_per_litre"])
    ]
)
data class ProductPriceEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "product_id")
    val productId: Long,

    @ColumnInfo(name = "market_id")
    val marketId: Long? = null,

    @ColumnInfo(name = "market_name")
    val marketName: String,

    @ColumnInfo(name = "price")
    val price: Double,

    @ColumnInfo(name = "discounted_price")
    val discountedPrice: Double? = null,

    @ColumnInfo(name = "quantity")
    val quantity: Double,

    @ColumnInfo(name = "unit")
    val unit: String, // GRAM, KILOGRAM, MILLILITRE, LITRE, PIECE

    // Auto-calculated unit prices
    @ColumnInfo(name = "unit_price_per_kg")
    val unitPricePerKg: Double? = null,

    @ColumnInfo(name = "unit_price_per_100g")
    val unitPricePer100g: Double? = null,

    @ColumnInfo(name = "unit_price_per_litre")
    val unitPricePerLitre: Double? = null,

    @ColumnInfo(name = "unit_price_per_piece")
    val unitPricePerPiece: Double? = null,

    @ColumnInfo(name = "effective_price")
    val effectivePrice: Double, // discountedPrice ?: price

    @ColumnInfo(name = "purchase_date")
    val purchaseDate: Long,

    @ColumnInfo(name = "note")
    val note: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
