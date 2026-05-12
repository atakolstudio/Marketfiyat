package com.marketfiyat.core.data.repository

import com.marketfiyat.core.data.local.dao.MarketDao
import com.marketfiyat.core.data.local.dao.ProductDao
import com.marketfiyat.core.data.local.dao.ProductPriceDao
import com.marketfiyat.core.data.local.dao.ShoppingListDao
import com.marketfiyat.core.data.local.entity.*
import com.marketfiyat.core.domain.repository.BackupRepository
import com.marketfiyat.core.util.Result
import com.marketfiyat.core.util.safeApiCall
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val productPriceDao: ProductPriceDao,
    private val marketDao: MarketDao,
    private val shoppingListDao: ShoppingListDao
) : BackupRepository {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun exportToJson(): Result<String> = safeApiCall {
        val prices = productPriceDao.getAllPricesSync()
        val productsRaw = mutableListOf<ProductWithPricesBackup>()

        val productIds = prices.map { it.productId }.distinct()
        productIds.forEach { productId ->
            val product = productDao.getProductById(productId) ?: return@forEach
            val productPrices = prices.filter { it.productId == productId }
            productsRaw.add(
                ProductWithPricesBackup(
                    name = product.name,
                    brand = product.brand,
                    barcode = product.barcode,
                    isFavorite = product.isFavorite,
                    prices = productPrices.map { price ->
                        PriceBackup(
                            marketName = price.marketName,
                            price = price.price,
                            discountedPrice = price.discountedPrice,
                            quantity = price.quantity,
                            unit = price.unit,
                            unitPricePerKg = price.unitPricePerKg,
                            unitPricePer100g = price.unitPricePer100g,
                            unitPricePerLitre = price.unitPricePerLitre,
                            unitPricePerPiece = price.unitPricePerPiece,
                            effectivePrice = price.effectivePrice,
                            purchaseDate = price.purchaseDate,
                            note = price.note
                        )
                    }
                )
            )
        }

        val backup = BackupData(
            exportDate = System.currentTimeMillis(),
            version = 1,
            products = productsRaw
        )
        json.encodeToString(backup)
    }

    override suspend fun importFromJson(json: String): Result<Unit> = safeApiCall {
        val backup = this.json.decodeFromString<BackupData>(json)
        // Import products and prices
        backup.products.forEach { productBackup ->
            val productId = productDao.insertProduct(
                ProductEntity(
                    name = productBackup.name,
                    brand = productBackup.brand,
                    barcode = productBackup.barcode,
                    isFavorite = productBackup.isFavorite
                )
            )
            productBackup.prices.forEach { price ->
                productPriceDao.insertPrice(
                    ProductPriceEntity(
                        productId = productId,
                        marketName = price.marketName,
                        price = price.price,
                        discountedPrice = price.discountedPrice,
                        quantity = price.quantity,
                        unit = price.unit,
                        unitPricePerKg = price.unitPricePerKg,
                        unitPricePer100g = price.unitPricePer100g,
                        unitPricePerLitre = price.unitPricePerLitre,
                        unitPricePerPiece = price.unitPricePerPiece,
                        effectivePrice = price.effectivePrice,
                        purchaseDate = price.purchaseDate,
                        note = price.note
                    )
                )
            }
        }
    }

    override suspend fun exportToCsv(): Result<String> = safeApiCall {
        val sb = StringBuilder()
        sb.appendLine("Ürün Adı,Marka,Market,Fiyat,İndirimli Fiyat,Miktar,Birim,KG Fiyatı,100g Fiyatı,Litre Fiyatı,Tarih,Not")
        val prices = productPriceDao.getAllPricesSync()
        prices.forEach { price ->
            val product = productDao.getProductById(price.productId)
            val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(Date(price.purchaseDate))
            sb.appendLine(
                "${product?.name ?: ""},${product?.brand ?: ""},${price.marketName}," +
                        "${price.price},${price.discountedPrice ?: ""}," +
                        "${price.quantity},${price.unit}," +
                        "${price.unitPricePerKg ?: ""},${price.unitPricePer100g ?: ""}," +
                        "${price.unitPricePerLitre ?: ""},$date,${price.note}"
            )
        }
        sb.toString()
    }
}

@Serializable
data class BackupData(
    val exportDate: Long,
    val version: Int,
    val products: List<ProductWithPricesBackup>
)

@Serializable
data class ProductWithPricesBackup(
    val name: String,
    val brand: String,
    val barcode: String?,
    val isFavorite: Boolean,
    val prices: List<PriceBackup>
)

@Serializable
data class PriceBackup(
    val marketName: String,
    val price: Double,
    val discountedPrice: Double?,
    val quantity: Double,
    val unit: String,
    val unitPricePerKg: Double?,
    val unitPricePer100g: Double?,
    val unitPricePerLitre: Double?,
    val unitPricePerPiece: Double?,
    val effectivePrice: Double,
    val purchaseDate: Long,
    val note: String
)
