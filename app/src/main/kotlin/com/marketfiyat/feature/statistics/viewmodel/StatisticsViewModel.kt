package com.marketfiyat.feature.statistics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketfiyat.core.domain.model.MarketSpendingStat
import com.marketfiyat.core.domain.model.MonthlySpending
import com.marketfiyat.core.domain.model.Product
import com.marketfiyat.core.domain.repository.ProductPriceRepository
import com.marketfiyat.core.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val totalSpending: Double = 0.0,
    val monthlySpending: List<MonthlySpending> = emptyList(),
    val marketSpending: List<MarketSpendingStat> = emptyList(),
    val mostPurchasedProducts: List<Product> = emptyList(),
    val totalSavings: Double = 0.0,
    val selectedPeriodMonths: Int = 6,
    val error: String? = null
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val productPriceRepository: ProductPriceRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics(6)
    }

    fun loadStatistics(months: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedPeriodMonths = months) }
            try {
                val cal = Calendar.getInstance()
                val endDate = cal.timeInMillis
                cal.add(Calendar.MONTH, -months)
                val startDate = cal.timeInMillis

                // Collect data concurrently
                val totalFlow = productPriceRepository.getTotalSpendingByDateRange(startDate, endDate)
                val marketFlow = productPriceRepository.getSpendingByMarket(startDate)

                combine(totalFlow, marketFlow) { total, markets ->
                    Pair(total, markets)
                }
                    .catch { e ->
                        Timber.e(e)
                        _uiState.update { it.copy(isLoading = false, error = e.message) }
                    }
                    .collectLatest { (total, markets) ->
                        val totalAmount = total ?: 0.0
                        val totalMarketSpend = markets.sumOf { it.second }
                        val marketStats = markets.map { (name, amount) ->
                            MarketSpendingStat(
                                marketName = name,
                                total = amount,
                                percentage = if (totalMarketSpend > 0)
                                    (amount / totalMarketSpend) * 100 else 0.0
                            )
                        }.sortedByDescending { it.total }

                        val monthlyData = buildMonthlyData(startDate, endDate, months)

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                totalSpending = totalAmount,
                                marketSpending = marketStats,
                                monthlySpending = monthlyData
                            )
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private suspend fun buildMonthlyData(
        startDate: Long,
        endDate: Long,
        months: Int
    ): List<MonthlySpending> {
        val result = mutableListOf<MonthlySpending>()
        val cal = Calendar.getInstance()
        cal.timeInMillis = startDate
        repeat(months) {
            val monthStart = cal.timeInMillis
            cal.add(Calendar.MONTH, 1)
            val monthEnd = cal.timeInMillis - 1
            val monthTotal = productPriceRepository
                .getTotalSpendingByDateRange(monthStart, monthEnd)
                .firstOrNull() ?: 0.0
            result.add(
                MonthlySpending(
                    year = cal.get(Calendar.YEAR),
                    month = cal.get(Calendar.MONTH) + 1,
                    total = monthTotal
                )
            )
        }
        return result
    }

    fun onPeriodChange(months: Int) {
        loadStatistics(months)
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
