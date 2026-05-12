package com.marketfiyat.feature.addprice.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketfiyat.core.data.local.dao.BarcodeCacheDao
import com.marketfiyat.core.data.local.entity.BarcodeCacheEntity
import com.marketfiyat.core.domain.model.Market
import com.marketfiyat.core.domain.model.Product
import com.marketfiyat.core.domain.model.ProductPrice
import com.marketfiyat.core.domain.repository.MarketRepository
import com.marketfiyat.core.domain.repository.ProductPriceRepository
import com.marketfiyat.core.domain.repository.ProductRepository
import com.marketfiyat.core.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class AddPriceUiState(
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val productName: String = "",
    val brand: String = "",
    val marketName: String = "",
    val price: String = "",
    val discountedPrice: String = "",
    val quantity: String = "",
    val selectedUnit: UnitType = UnitType.GRAM,
    val purchaseDate: Long = System.currentTimeMillis(),
    val note: String = "",
    val existingProduct: Product? = null,
    val availableMarkets: List<String> = emptyList(),
    val unitPrices: UnitPrices? = null,
    val priceAnalysisMessage: String? = null,
    val errors: Map<String, String> = emptyMap(),
    val error: String? = null,
    // Real-time analysis
    val analysisMessage: String? = null,
    val isAboveAverage: Boolean = false
)

@HiltViewModel
class AddPriceViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val productPriceRepository: ProductPriceRepository,
    private val marketRepository: MarketRepository,
    private val barcodeCacheDao: BarcodeCacheDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddPriceUiState())
    val uiState: StateFlow<AddPriceUiState> = _uiState.asStateFlow()

    init {
        loadMarkets()
    }

    private fun loadMarkets() {
        viewModelScope.launch {
            marketRepository.getDistinctMarketNames()
                .catch { Timber.e(it) }
                .collect { markets ->
                    _uiState.update { it.copy(availableMarkets = markets) }
                }
        }
    }

    fun loadProduct(productId: Long) {
        viewModelScope.launch {
            val product = productRepository.getProductById(productId) ?: return@launch
            _uiState.update {
                it.copy(
                    existingProduct = product,
                    productName = product.name,
                    brand = product.brand
                )
            }
            // Load last price for this product
            val lastPrice = productPriceRepository.getLatestPrice(productId)
            lastPrice?.let { price ->
                _uiState.update {
                    it.copy(marketName = price.marketName)
                }
            }
        }
    }

    fun loadByBarcode(barcode: String) {
        viewModelScope.launch {
            val product = productRepository.getProductByBarcode(barcode)
            if (product != null) {
                _uiState.update {
                    it.copy(
                        existingProduct = product,
                        productName = product.name,
                        brand = product.brand
                    )
                }
            }
        }
    }

    fun onProductNameChange(name: String) {
        _uiState.update {
            it.copy(
                productName = name,
                errors = it.errors - "productName"
            )
        }
    }

    fun onBrandChange(brand: String) {
        _uiState.update { it.copy(brand = brand) }
    }

    fun onMarketNameChange(market: String) {
        _uiState.update {
            it.copy(
                marketName = market,
                errors = it.errors - "marketName"
            )
        }
        recalculateAndAnalyze()
    }

    fun onPriceChange(price: String) {
        _uiState.update {
            it.copy(
                price = price,
                errors = it.errors - "price"
            )
        }
        recalculateAndAnalyze()
    }

    fun onDiscountedPriceChange(price: String) {
        _uiState.update { it.copy(discountedPrice = price) }
        recalculateAndAnalyze()
    }

    fun onQuantityChange(qty: String) {
        _uiState.update {
            it.copy(
                quantity = qty,
                errors = it.errors - "quantity"
            )
        }
        recalculateAndAnalyze()
    }

    fun onUnitChange(unit: UnitType) {
        _uiState.update { it.copy(selectedUnit = unit) }
        recalculateAndAnalyze()
    }

    fun onDateChange(date: Long) {
        _uiState.update { it.copy(purchaseDate = date) }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    private fun recalculateAndAnalyze() {
        val state = _uiState.value
        val price = state.price.toDoubleOrNull() ?: return
        val qty = state.quantity.toDoubleOrNull() ?: return
        if (qty <= 0) return

        val unitPrices = UnitPriceCalculator.calculateUnitPrices(price, qty, state.selectedUnit)
        _uiState.update { it.copy(unitPrices = unitPrices) }

        // Real-time price analysis
        analyzeCurrentPrice(price)
    }

    private fun analyzeCurrentPrice(currentPrice: Double) {
        val product = _uiState.value.existingProduct ?: return
        viewModelScope.launch {
            try {
                val avgPrice = productPriceRepository.getAveragePrice(product.id) ?: return@launch
                val lastPrice = productPriceRepository.getLatestPrice(product.id)
                val minIn6Months = productPriceRepository.getAveragePriceSince(
                    product.id,
                    System.currentTimeMillis() - (180L * 24 * 60 * 60 * 1000)
                )

                val diffFromAvg = ((currentPrice - avgPrice) / avgPrice) * 100
                val message = when {
                    minIn6Months != null && currentPrice < minIn6Months ->
                        "🎉 Son 6 ayın en ucuz fiyatı!"
                    diffFromAvg > 10 ->
                        "⚠️ Geçmiş ortalamadan %.1f%% daha pahalı".format(diffFromAvg)
                    diffFromAvg < -10 ->
                        "✅ Geçmiş ortalamadan %.1f%% daha ucuz".format(-diffFromAvg)
                    lastPrice != null && currentPrice < lastPrice.effectivePrice ->
                        "📉 Son alış fiyatından daha uygun"
                    lastPrice != null && currentPrice > lastPrice.effectivePrice -> {
                        val change = ((currentPrice - lastPrice.effectivePrice) / lastPrice.effectivePrice) * 100
                        "📈 Son alışa göre %.1f%% zamlandı".format(change)
                    }
                    else -> null
                }
                _uiState.update {
                    it.copy(
                        analysisMessage = message,
                        isAboveAverage = diffFromAvg > 5
                    )
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun savePrice() {
        val state = _uiState.value
        val errors = validate(state)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(errors = errors) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val price = state.price.toDouble()
                val discountedPrice = state.discountedPrice.toDoubleOrNull()
                val qty = state.quantity.toDoubleOrNull() ?: 1.0
                val effectivePrice = discountedPrice ?: price

                val unitPrices = UnitPriceCalculator.calculateUnitPrices(
                    effectivePrice, qty, state.selectedUnit
                )

                // Get or create product
                val productId = if (state.existingProduct != null) {
                    state.existingProduct.id
                } else {
                    val newProduct = Product(
                        name = state.productName.trim(),
                        brand = state.brand.trim()
                    )
                    productRepository.insertProduct(newProduct)
                }

                // Get or create market
                marketRepository.getOrCreateMarket(state.marketName.trim())

                // Save price
                val productPrice = ProductPrice(
                    productId = productId,
                    marketName = state.marketName.trim(),
                    price = price,
                    discountedPrice = discountedPrice,
                    quantity = qty,
                    unit = state.selectedUnit,
                    unitPricePerKg = unitPrices.perKg,
                    unitPricePer100g = unitPrices.per100g,
                    unitPricePerLitre = unitPrices.perLitre,
                    unitPricePerPiece = unitPrices.perPiece,
                    effectivePrice = effectivePrice,
                    purchaseDate = state.purchaseDate,
                    note = state.note
                )
                productPriceRepository.insertPrice(productPrice)

                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                Timber.e(e, "Error saving price")
                _uiState.update {
                    it.copy(isLoading = false, error = "Kaydetme başarısız: ${e.message}")
                }
            }
        }
    }

    private fun validate(state: AddPriceUiState): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (state.productName.isBlank()) errors["productName"] = "Ürün adı boş olamaz"
        if (state.marketName.isBlank()) errors["marketName"] = "Market adı boş olamaz"
        if (state.price.isBlank()) {
            errors["price"] = "Fiyat boş olamaz"
        } else if (state.price.toDoubleOrNull() == null || state.price.toDouble() <= 0) {
            errors["price"] = "Geçerli bir fiyat girin"
        }
        if (state.quantity.isNotBlank() && (state.quantity.toDoubleOrNull() == null || state.quantity.toDouble() <= 0)) {
            errors["quantity"] = "Geçerli bir miktar girin"
        }
        return errors
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
