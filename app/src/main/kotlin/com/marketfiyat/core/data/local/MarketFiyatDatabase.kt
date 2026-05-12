package com.marketfiyat.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.marketfiyat.core.data.local.dao.*
import com.marketfiyat.core.data.local.entity.*

@Database(
    entities = [
        ProductEntity::class,
        ProductPriceEntity::class,
        MarketEntity::class,
        ShoppingListEntity::class,
        ShoppingListItemEntity::class,
        BarcodeCacheEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class MarketFiyatDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun productPriceDao(): ProductPriceDao
    abstract fun marketDao(): MarketDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun barcodeCacheDao(): BarcodeCacheDao

    companion object {
        const val DATABASE_NAME = "marketfiyat.db"

        val DEFAULT_MARKETS = listOf(
            MarketEntity(name = "A101", colorHex = "#E53935"),
            MarketEntity(name = "BİM", colorHex = "#F9A825"),
            MarketEntity(name = "ŞOK", colorHex = "#E65100"),
            MarketEntity(name = "Migros", colorHex = "#C62828"),
            MarketEntity(name = "CarrefourSA", colorHex = "#1565C0"),
            MarketEntity(name = "Hakmar", colorHex = "#2E7D32"),
            MarketEntity(name = "Metro", colorHex = "#F57F17"),
            MarketEntity(name = "Tarım Kredi", colorHex = "#558B2F"),
            MarketEntity(name = "Tazedirekt", colorHex = "#00695C"),
            MarketEntity(name = "Getir", colorHex = "#6A1B9A")
        )
    }
}
