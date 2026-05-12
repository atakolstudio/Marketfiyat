package com.marketfiyat.feature.compare.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marketfiyat.core.domain.model.*
import com.marketfiyat.core.ui.components.*
import com.marketfiyat.core.util.formatPrice
import com.marketfiyat.core.util.formatPercent
import com.marketfiyat.feature.compare.viewmodel.MarketCompareViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MarketCompareScreen(
    productId: Long,
    onNavigateBack: () -> Unit,
    viewModel: MarketCompareViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(productId) {
        viewModel.loadProductData(productId)
    }

    Scaffold(
        topBar = {
            MarketFiyatTopBar(
                title = uiState.product?.name ?: "Market Karşılaştırması",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                ErrorStateView(
                    message = uiState.error!!,
                    onRetry = { viewModel.loadProductData(productId) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding() + 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Price Analysis Card
                    uiState.priceAnalysis?.let { analysis ->
                        item {
                            PriceAnalysisCard(analysis = analysis)
                        }
                    }

                    // Market Comparison
                    uiState.comparison?.let { comparison ->
                        item {
                            SectionHeader(title = "Market Karşılaştırması")
                        }
                        items(comparison.marketPrices) { marketPrice ->
                            MarketPriceRow(
                                marketPrice = marketPrice,
                                averagePrice = comparison.averagePrice
                            )
                        }
                        item {
                            MarketSummaryCard(comparison = comparison)
                        }
                    }

                    // Price History
                    uiState.priceHistory?.let { history ->
                        if (history.prices.isNotEmpty()) {
                            item {
                                SectionHeader(title = "Fiyat Geçmişi")
                            }
                            items(history.prices.take(10)) { price ->
                                PriceHistoryRow(price = price)
                            }
                        }
                    }

                    // Price Chart placeholder
                    if (uiState.chartData.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Fiyat Grafiği")
                        }
                        item {
                            PriceChartCard(chartData = uiState.chartData)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceAnalysisCard(analysis: PriceAnalysis) {
    val bgColor = when (analysis.recommendation) {
        PriceRecommendation.BUY_NOW, PriceRecommendation.GOOD_DEAL ->
            MaterialTheme.colorScheme.secondaryContainer
        PriceRecommendation.WAIT ->
            MaterialTheme.colorScheme.errorContainer
        PriceRecommendation.NEUTRAL ->
            MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when (analysis.recommendation) {
        PriceRecommendation.BUY_NOW, PriceRecommendation.GOOD_DEAL ->
            MaterialTheme.colorScheme.onSecondaryContainer
        PriceRecommendation.WAIT ->
            MaterialTheme.colorScheme.onErrorContainer
        PriceRecommendation.NEUTRAL ->
            MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = when (analysis.recommendation) {
                    PriceRecommendation.BUY_NOW -> Icons.Default.ThumbUp
                    PriceRecommendation.GOOD_DEAL -> Icons.Default.LocalOffer
                    PriceRecommendation.WAIT -> Icons.Default.Schedule
                    PriceRecommendation.NEUTRAL -> Icons.Default.Info
                }
                Icon(icon, contentDescription = null, tint = textColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = analysis.analysisMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "Güncel", value = analysis.currentPrice.formatPrice(), color = textColor)
                StatItem(label = "Ortalama", value = analysis.averagePrice.formatPrice(), color = textColor)
                StatItem(label = "En ucuz", value = analysis.minPrice.formatPrice(), color = textColor)
                StatItem(label = "En pahalı", value = analysis.maxPrice.formatPrice(), color = textColor)
            }
            analysis.priceChangePercent?.let { change ->
                Spacer(modifier = Modifier.height(8.dp))
                val changeText = if (change > 0) "+${change.formatPercent()} (son alışa göre)"
                else "${change.formatPercent()} (son alışa göre)"
                Text(changeText, style = MaterialTheme.typography.bodySmall, color = textColor)
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
        Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun MarketPriceRow(marketPrice: MarketPrice, averagePrice: Double) {
    val diffFromAvg = ((marketPrice.price - averagePrice) / averagePrice) * 100
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (marketPrice.isCheapest)
                MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (marketPrice.isCheapest) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = "En ucuz",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = marketPrice.marketName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (marketPrice.isCheapest) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = marketPrice.price.formatPrice(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                marketPrice.unitPrice?.let {
                    Text(
                        text = "1 KG: ${it.formatPrice()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                if (kotlin.math.abs(diffFromAvg) > 1) {
                    Text(
                        text = if (diffFromAvg < 0) "${diffFromAvg.formatPercent()} avg'dan ucuz"
                        else "+${diffFromAvg.formatPercent()} avg'dan pahalı",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (diffFromAvg < 0) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun MarketSummaryCard(comparison: MarketComparison) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Özet",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("En ucuz:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(comparison.cheapestMarket, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("En pahalı:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(comparison.mostExpensiveMarket, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Ortalama:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(comparison.averagePrice.formatPrice(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
private fun PriceHistoryRow(price: com.marketfiyat.core.domain.model.ProductPrice) {
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale("tr")).format(Date(price.purchaseDate))
    ListItem(
        headlineContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(price.marketName, style = MaterialTheme.typography.bodyMedium)
                PriceText(price = price.effectivePrice, style = MaterialTheme.typography.bodyMedium)
            }
        },
        supportingContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${price.quantity} ${price.unit.shortName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        modifier = Modifier.padding(horizontal = 8.dp)
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
private fun PriceChartCard(chartData: List<Pair<Long, Double>>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Fiyat Grafiği (Son 6 Ay)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Simple line chart representation as a linear progress bar row
            if (chartData.isNotEmpty()) {
                val minPrice = chartData.minOf { it.second }
                val maxPrice = chartData.maxOf { it.second }
                val range = maxPrice - minPrice
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    chartData.takeLast(12).forEach { (_, price) ->
                        val fraction = if (range > 0) ((price - minPrice) / range).toFloat() else 0.5f
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .fillMaxHeight(fraction.coerceIn(0.1f, 1f))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.primary
                            ) {}
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(minPrice.formatPrice(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    Text(maxPrice.formatPrice(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
