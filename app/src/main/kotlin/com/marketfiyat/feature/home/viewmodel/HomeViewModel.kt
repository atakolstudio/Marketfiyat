package com.marketfiyat.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketfiyat.core.domain.model.Product
import com.marketfiyat.core.domain.model.ProductPrice
import com.marketfiyat.core.domain.repository.ProductPriceRepository
import com.marketfiyat.core.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val recentPrices: List<RecentPriceItem> = emptyList(),
    val totalProductCount: Int = 0,
    val totalSavings: Double = 0.0,
    val error: String? = null
)

data class RecentPriceItem(
    val product: Product,
    val latestPrice: ProductPrice,
    val priceChangePercent: Double?,
    val isIncrease: Boolean
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val productPriceRepository: ProductPriceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            combine(
                productRepository.getAllProducts(),
                productRepository.getProductCount()
            ) { products, count ->
                Pair(products, count)
            }
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    Timber.e(e, "Error loading home data")
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collectLatest { (products, count) ->
                    val recentItems = buildRecentPriceItems(products)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            recentPrices = recentItems,
                            totalProductCount = count,
                            error = null
                        )
                    }
                }
        }
    }

    private suspend fun buildRecentPriceItems(products: List<Product>): List<RecentPriceItem> {
        return products
            .mapNotNull { product ->
                val prices = productPriceRepository.getPricesByProductIdSync(product.id)
                if (prices.isEmpty()) return@mapNotNull null
                val sorted = prices.sortedByDescending { it.purchaseDate }
                val latest = sorted.first()
                val previous = sorted.getOrNull(1)
                val changePercent = previous?.let {
                    ((latest.effectivePrice - it.effectivePrice) / it.effectivePrice) * 100
                }
                RecentPriceItem(
                    product = product,
                    latestPrice = latest,
                    priceChangePercent = changePercent,
                    isIncrease = (changePercent ?: 0.0) > 0
                )
            }
            .sortedByDescending { it.latestPrice.createdAt }
            .take(20)
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
