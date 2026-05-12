package com.marketfiyat.feature.home.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marketfiyat.core.ui.components.*
import com.marketfiyat.core.util.formatPrice
import com.marketfiyat.feature.home.viewmodel.HomeViewModel
import com.marketfiyat.feature.home.viewmodel.RecentPriceItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddPrice: () -> Unit,
    onNavigateToArchive: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToShoppingList: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCompare: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Market Fiyat",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${uiState.totalProductCount} ürün takip ediliyor",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Ayarlar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddPrice,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Fiyat Ekle") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        },
        bottomBar = {
            HomeBottomBar(
                onNavigateToArchive = onNavigateToArchive,
                onNavigateToShoppingList = onNavigateToShoppingList,
                onNavigateToStatistics = onNavigateToStatistics
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LazyColumn(
                    contentPadding = paddingValues,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(6) { ShimmerProductCard() }
                }
            }
            uiState.error != null -> {
                ErrorStateView(
                    message = uiState.error!!,
                    onRetry = { viewModel.dismissError() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            uiState.recentPrices.isEmpty() -> {
                EmptyStateView(
                    icon = Icons.Default.ShoppingCart,
                    title = "Henüz ürün eklenmedi",
                    description = "İlk ürününüzü eklemek için + butonuna dokunun",
                    actionLabel = "Ürün Ekle",
                    onAction = onNavigateToAddPrice,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding() + 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Quick Action Cards
                    item {
                        QuickActionRow(
                            onNavigateToShoppingList = onNavigateToShoppingList,
                            onNavigateToStatistics = onNavigateToStatistics,
                            onNavigateToArchive = onNavigateToArchive
                        )
                    }

                    // Section header
                    item {
                        SectionHeader(title = "Son Eklenenler")
                    }

                    // Recent price items
                    items(
                        items = uiState.recentPrices,
                        key = { it.latestPrice.id }
                    ) { item ->
                        RecentPriceCard(
                            item = item,
                            onCompareClick = { onNavigateToCompare(item.product.id) }
                        )
                    }
                }
            }
        }
    }

    uiState.error?.let { errorMsg ->
        LaunchedEffect(errorMsg) {
            // Show snackbar
        }
    }
}

@Composable
private fun QuickActionRow(
    onNavigateToShoppingList: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToArchive: () -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            QuickActionCard(
                icon = Icons.Default.ShoppingCart,
                label = "Alışveriş\nListesi",
                onClick = onNavigateToShoppingList
            )
        }
        item {
            QuickActionCard(
                icon = Icons.Default.BarChart,
                label = "İstatistik",
                onClick = onNavigateToStatistics
            )
        }
        item {
            QuickActionCard(
                icon = Icons.Default.Archive,
                label = "Ürün\nArşivi",
                onClick = onNavigateToArchive
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(width = 100.dp, height = 80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun RecentPriceCard(
    item: RecentPriceItem,
    onCompareClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.product.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    if (item.product.brand.isNotBlank()) {
                        Text(
                            text = item.product.brand,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "📍 ${item.latestPrice.marketName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    PriceText(price = item.latestPrice.effectivePrice)
                    item.priceChangePercent?.let { change ->
                        if (kotlin.math.abs(change) > 0.5) {
                            PriceChangeBadge(changePercent = change)
                        }
                    }
                }
            }

            // Unit prices
            val unitPriceText = when {
                item.latestPrice.unitPricePerKg != null ->
                    "1 KG: ${item.latestPrice.unitPricePerKg.formatPrice()}"
                item.latestPrice.unitPricePerLitre != null ->
                    "1 L: ${item.latestPrice.unitPricePerLitre.formatPrice()}"
                else -> null
            }
            unitPriceText?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dateStr = SimpleDateFormat("dd MMM yyyy", Locale("tr")).format(
                    Date(item.latestPrice.purchaseDate)
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                TextButton(
                    onClick = onCompareClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.CompareArrows,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Karşılaştır", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun HomeBottomBar(
    onNavigateToArchive: () -> Unit,
    onNavigateToShoppingList: () -> Unit,
    onNavigateToStatistics: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Ana Sayfa") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onNavigateToArchive,
            icon = { Icon(Icons.Default.Archive, contentDescription = null) },
            label = { Text("Arşiv") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onNavigateToShoppingList,
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
            label = { Text("Alışveriş") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onNavigateToStatistics,
            icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
            label = { Text("İstatistik") }
        )
    }
}
