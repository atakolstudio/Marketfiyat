package com.marketfiyat.feature.archive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketfiyat.core.domain.model.Product
import com.marketfiyat.core.domain.model.ProductPrice
import com.marketfiyat.core.domain.repository.ProductPriceRepository
import com.marketfiyat.core.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

enum class SortOrder {
    NEWEST, OLDEST, NAME_ASC, NAME_DESC, PRICE_ASC, PRICE_DESC, CHANGE_DESC
}

enum class FilterType {
    ALL, FAVORITES, BY_MARKET, BY_BRAND
}

data class ArchiveUiState(
    val isLoading: Boolean = true,
    val products: List<ProductWithLatestPrice> = emptyList(),
    val filteredProducts: List<ProductWithLatestPrice> = emptyList(),
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.NEWEST,
    val filterType: FilterType = FilterType.ALL,
    val selectedMarket: String? = null,
    val selectedBrand: String? = null,
    val availableMarkets: List<String> = emptyList(),
    val availableBrands: List<String> = emptyList(),
    val error: String? = null
)

data class ProductWithLatestPrice(
    val product: Product,
    val latestPrice: ProductPrice?,
    val priceChangePercent: Double?,
    val allTimeMin: Double?,
    val allTimeMax: Double?
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val productPriceRepository: ProductPriceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState: StateFlow<ArchiveUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")
    private val sortOrder = MutableStateFlow(SortOrder.NEWEST)
    private val filterType = MutableStateFlow(FilterType.ALL)

    init {
        loadProducts()
        observeSearchAndFilters()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            productRepository.getAllProducts()
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    Timber.e(e)
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collectLatest { products ->
                    val enriched = products.map { product ->
                        buildProductWithLatestPrice(product)
                    }
                    val markets = enriched.mapNotNull { it.latestPrice?.marketName }.distinct().sorted()
                    val brands = products.map { it.brand }.filter { it.isNotBlank() }.distinct().sorted()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            products = enriched,
                            availableMarkets = markets,
                            availableBrands = brands
                        )
                    }
                    applyFilters()
                }
        }
    }

    private suspend fun buildProductWithLatestPrice(product: Product): ProductWithLatestPrice {
        val prices = productPriceRepository.getPricesByProductIdSync(product.id)
        val sortedPrices = prices.sortedByDescending { it.purchaseDate }
        val latest = sortedPrices.firstOrNull()
        val previous = sortedPrices.getOrNull(1)
        val changePercent = if (latest != null && previous != null) {
            ((latest.effectivePrice - previous.effectivePrice) / previous.effectivePrice) * 100
        } else null
        val minPrice = prices.minOfOrNull { it.effectivePrice }
        val maxPrice = prices.maxOfOrNull { it.effectivePrice }
        return ProductWithLatestPrice(
            product = product,
            latestPrice = latest,
            priceChangePercent = changePercent,
            allTimeMin = minPrice,
            allTimeMax = maxPrice
        )
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchAndFilters() {
        viewModelScope.launch {
            combine(searchQuery.debounce(300), sortOrder, filterType) { q, sort, filter ->
                Triple(q, sort, filter)
            }.collect { applyFilters() }
        }
    }

    private fun applyFilters() {
        val state = _uiState.value
        var filtered = state.products

        // Search
        val query = searchQuery.value.trim()
        if (query.isNotEmpty()) {
            filtered = filtered.filter {
                it.product.name.contains(query, ignoreCase = true) ||
                        it.product.brand.contains(query, ignoreCase = true) ||
                        it.latestPrice?.marketName?.contains(query, ignoreCase = true) == true
            }
        }

        // Filter
        when (filterType.value) {
            FilterType.FAVORITES -> filtered = filtered.filter { it.product.isFavorite }
            FilterType.BY_MARKET -> state.selectedMarket?.let { market ->
                filtered = filtered.filter { it.latestPrice?.marketName == market }
            }
            FilterType.BY_BRAND -> state.selectedBrand?.let { brand ->
                filtered = filtered.filter { it.product.brand == brand }
            }
            FilterType.ALL -> Unit
        }

        // Sort
        filtered = when (sortOrder.value) {
            SortOrder.NEWEST -> filtered.sortedByDescending { it.latestPrice?.purchaseDate ?: 0L }
            SortOrder.OLDEST -> filtered.sortedBy { it.latestPrice?.purchaseDate ?: Long.MAX_VALUE }
            SortOrder.NAME_ASC -> filtered.sortedBy { it.product.name }
            SortOrder.NAME_DESC -> filtered.sortedByDescending { it.product.name }
            SortOrder.PRICE_ASC -> filtered.sortedBy { it.latestPrice?.effectivePrice ?: Double.MAX_VALUE }
            SortOrder.PRICE_DESC -> filtered.sortedByDescending { it.latestPrice?.effectivePrice ?: 0.0 }
            SortOrder.CHANGE_DESC -> filtered.sortedByDescending { it.priceChangePercent ?: Double.MIN_VALUE }
        }

        _uiState.update { it.copy(filteredProducts = filtered) }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onSortOrderChange(order: SortOrder) {
        sortOrder.value = order
        _uiState.update { it.copy(sortOrder = order) }
    }

    fun onFilterTypeChange(filter: FilterType) {
        filterType.value = filter
        _uiState.update { it.copy(filterType = filter) }
        applyFilters()
    }

    fun onMarketFilterChange(market: String?) {
        _uiState.update { it.copy(selectedMarket = market) }
        applyFilters()
    }

    fun onBrandFilterChange(brand: String?) {
        _uiState.update { it.copy(selectedBrand = brand) }
        applyFilters()
    }

    fun deleteProduct(productId: Long) {
        viewModelScope.launch {
            try {
                productRepository.deleteProduct(productId)
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.update { it.copy(error = "Silme başarısız: ${e.message}") }
            }
        }
    }

    fun toggleFavorite(productId: Long, current: Boolean) {
        viewModelScope.launch {
            productRepository.toggleFavorite(productId, !current)
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
