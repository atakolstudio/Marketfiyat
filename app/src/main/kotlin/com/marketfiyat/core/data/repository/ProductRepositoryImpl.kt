package com.marketfiyat.core.data.repository

import com.marketfiyat.core.data.local.dao.ProductDao
import com.marketfiyat.core.data.local.entity.toDomain
import com.marketfiyat.core.data.local.entity.toEntity
import com.marketfiyat.core.domain.model.Product
import com.marketfiyat.core.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao
) : ProductRepository {

    override fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchProducts(query: String): Flow<List<Product>> {
        return productDao.searchProducts(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getFavoriteProducts(): Flow<List<Product>> {
        return productDao.getFavoriteProducts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getProductById(id: Long): Product? {
        return productDao.getProductById(id)?.toDomain()
    }

    override suspend fun getProductByBarcode(barcode: String): Product? {
        return productDao.getProductByBarcode(barcode)?.toDomain()
    }

    override suspend fun insertProduct(product: Product): Long {
        return try {
            productDao.insertProduct(product.toEntity())
        } catch (e: Exception) {
            Timber.e(e, "Error inserting product: ${product.name}")
            throw e
        }
    }

    override suspend fun updateProduct(product: Product) {
        try {
            productDao.updateProduct(
                product.toEntity().copy(updatedAt = System.currentTimeMillis())
            )
        } catch (e: Exception) {
            Timber.e(e, "Error updating product: ${product.id}")
            throw e
        }
    }

    override suspend fun deleteProduct(productId: Long) {
        try {
            productDao.deleteProductById(productId)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting product: $productId")
            throw e
        }
    }

    override suspend fun toggleFavorite(productId: Long, isFavorite: Boolean) {
        productDao.updateFavoriteStatus(productId, isFavorite)
    }

    override suspend fun setPriceAlarm(productId: Long, threshold: Double?) {
        productDao.updatePriceAlarm(productId, threshold)
    }

    override fun getProductCount(): Flow<Int> {
        return productDao.getProductCount()
    }
}
