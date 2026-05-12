package com.marketfiyat.feature.compare.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketfiyat.core.domain.model.MarketComparison
import com.marketfiyat.core.domain.model.PriceAnalysis
import com.marketfiyat.core.domain.model.PriceHistory
import com.marketfiyat.core.domain.model.PriceTrend
import com.marketfiyat.core.domain.model.Product
import com.marketfiyat.core.domain.model.ProductPrice
import com.marketfiyat.core.domain.repository.ProductPriceRepository
import com.marketfiyat.core.domain.repository.ProductRepository
import com.marketfiyat.core.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class MarketCompareUiState(
    val isLoading: Boolean = true,
    val product: Product? = null,
    val comparison: MarketComparison? = null,
    val priceAnalysis: PriceAnalysis? = null,
    val priceHistory: PriceHistory? = null,
    val chartData: List<Pair<Long, Double>> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class MarketCompareViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val productPriceRepository: ProductPriceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketCompareUiState())
    val uiState: StateFlow<MarketCompareUiState> = _uiState.asStateFlow()

    fun loadProductData(productId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val product = productRepository.getProductById(productId)
                val comparison = productPriceRepository.getMarketComparison(productId)
                val analysis = productPriceRepository.analyzePrices(productId)
                val prices = productPriceRepository.getPricesByProductIdSync(productId)
                val history = buildPriceHistory(prices)
                val since = System.currentTimeMillis() - (Constants.PRICE_ANALYSIS_WINDOW_DAYS * 24 * 60 * 60 * 1000)
                val chartData = productPriceRepository.getPricesForChart(productId, since)
                    .map { Pair(it.purchaseDate, it.effectivePrice) }
                    .sortedBy { it.first }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        product = product,
                        comparison = comparison,
                        priceAnalysis = analysis,
                        priceHistory = history,
                        chartData = chartData
                    )
                }
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun buildPriceHistory(prices: List<ProductPrice>): PriceHistory {
        val sorted = prices.sortedByDescending { it.purchaseDate }
        val latest = sorted.firstOrNull()
        val second = sorted.getOrNull(1)
        val changePercent = if (latest != null && second != null) {
            ((latest.effectivePrice - second.effectivePrice) / second.effectivePrice) * 100
        } else null
        val cheapestDate = prices.minByOrNull { it.effectivePrice }?.purchaseDate
        val mostExpensiveDate = prices.maxByOrNull { it.effectivePrice }?.purchaseDate
        val trend = when {
            changePercent == null -> PriceTrend.STABLE
            changePercent > 3 -> PriceTrend.INCREASING
            changePercent < -3 -> PriceTrend.DECREASING
            else -> PriceTrend.STABLE
        }
        return PriceHistory(
            prices = sorted,
            priceChangePercent = changePercent,
            cheapestDate = cheapestDate,
            mostExpensiveDate = mostExpensiveDate,
            trend = trend
        )
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
