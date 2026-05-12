package com.marketfiyat.feature.archive.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marketfiyat.core.ui.components.*
import com.marketfiyat.core.util.formatPrice
import com.marketfiyat.feature.archive.viewmodel.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddPrice: (Long) -> Unit,
    onNavigateToCompare: (Long) -> Unit,
    viewModel: ArchiveViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSortDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            Column {
                MarketFiyatTopBar(
                    title = "Ürün Arşivi",
                    onNavigateBack = onNavigateBack,
                    actions = {
                        IconButton(onClick = { showSortDialog = true }) {
                            Icon(Icons.Default.Sort, "Sırala")
                        }
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(Icons.Default.FilterList, "Filtrele")
                        }
                    }
                )
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LazyColumn(contentPadding = paddingValues) {
                    items(8) { ShimmerProductCard() }
                }
            }
            uiState.filteredProducts.isEmpty() -> {
                EmptyStateView(
                    icon = Icons.Default.SearchOff,
                    title = "Ürün bulunamadı",
                    description = "Arama kriterlerinizi değiştirin veya yeni ürün ekleyin",
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding() + 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Active filters row
                    item {
                        ActiveFiltersRow(
                            uiState = uiState,
                            onClearMarket = { viewModel.onMarketFilterChange(null) },
                            onClearBrand = { viewModel.onBrandFilterChange(null) },
                            onClearFilter = { viewModel.onFilterTypeChange(FilterType.ALL) }
                        )
                    }

                    item {
                        Text(
                            "${uiState.filteredProducts.size} ürün",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    items(
                        items = uiState.filteredProducts,
                        key = { it.product.id }
                    ) { item ->
                        ArchiveProductCard(
                            item = item,
                            onAddPrice = { onNavigateToAddPrice(item.product.id) },
                            onCompare = { onNavigateToCompare(item.product.id) },
                            onToggleFavorite = {
                                viewModel.toggleFavorite(item.product.id, item.product.isFavorite)
                            },
                            onDelete = { showDeleteConfirm = item.product.id }
                        )
                    }
                }
            }
        }
    }

    // Sort Dialog
    if (showSortDialog) {
        SortDialog(
            currentSort = uiState.sortOrder,
            onSortSelected = {
                viewModel.onSortOrderChange(it)
                showSortDialog = false
            },
            onDismiss = { showSortDialog = false }
        )
    }

    // Filter Dialog
    if (showFilterDialog) {
        FilterDialog(
            currentFilter = uiState.filterType,
            markets = uiState.availableMarkets,
            brands = uiState.availableBrands,
            selectedMarket = uiState.selectedMarket,
            selectedBrand = uiState.selectedBrand,
            onFilterSelected = { viewModel.onFilterTypeChange(it) },
            onMarketSelected = { viewModel.onMarketFilterChange(it) },
            onBrandSelected = { viewModel.onBrandFilterChange(it) },
            onDismiss = { showFilterDialog = false }
        )
    }

    // Delete confirm dialog
    showDeleteConfirm?.let { productId ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Ürünü Sil") },
            text = { Text("Bu ürün ve tüm fiyat geçmişi silinecek. Emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProduct(productId)
                        showDeleteConfirm = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Sil") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("İptal") }
            }
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Ürün, marka veya market ara...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Ara") },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Temizle")
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.extraLarge
    )
}

@Composable
private fun ActiveFiltersRow(
    uiState: ArchiveUiState,
    onClearMarket: () -> Unit,
    onClearBrand: () -> Unit,
    onClearFilter: () -> Unit
) {
    val hasFilters = uiState.filterType != FilterType.ALL ||
            uiState.selectedMarket != null ||
            uiState.selectedBrand != null
    if (!hasFilters) return

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (uiState.filterType == FilterType.FAVORITES) {
            item {
                FilterChip(
                    selected = true,
                    onClick = onClearFilter,
                    label = { Text("Favoriler") },
                    trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) }
                )
            }
        }
        uiState.selectedMarket?.let { market ->
            item {
                FilterChip(
                    selected = true,
                    onClick = onClearMarket,
                    label = { Text(market) },
                    trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) }
                )
            }
        }
        uiState.selectedBrand?.let { brand ->
            item {
                FilterChip(
                    selected = true,
                    onClick = onClearBrand,
                    label = { Text(brand) },
                    trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) }
                )
            }
        }
    }
}

@Composable
private fun ArchiveProductCard(
    item: com.marketfiyat.feature.archive.viewmodel.ProductWithLatestPrice,
    onAddPrice: () -> Unit,
    onCompare: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.product.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (item.product.isFavorite) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    if (item.product.brand.isNotBlank()) {
                        Text(
                            item.product.brand,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    item.latestPrice?.let { price ->
                        Text(
                            "📍 ${price.marketName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    item.latestPrice?.let { price ->
                        PriceText(price = price.effectivePrice)
                        item.priceChangePercent?.let { change ->
                            if (kotlin.math.abs(change) > 0.5) {
                                PriceChangeBadge(changePercent = change)
                            }
                        }
                    }
                }
            }

            // Min / Max row
            if (item.allTimeMin != null && item.allTimeMax != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            "En ucuz: ${item.allTimeMin.formatPrice()}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            "En pahalı: ${item.allTimeMax.formatPrice()}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(4.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(36.dp)) {
                    Icon(
                        if (item.product.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favori",
                        tint = if (item.product.isFavorite) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onAddPrice, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Add, "Fiyat Ekle", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onCompare, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.CompareArrows, "Karşılaştır", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        "Sil",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SortDialog(
    currentSort: SortOrder,
    onSortSelected: (SortOrder) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sıralama") },
        text = {
            Column {
                val options = mapOf(
                    SortOrder.NEWEST to "En Yeni",
                    SortOrder.OLDEST to "En Eski",
                    SortOrder.NAME_ASC to "İsim (A-Z)",
                    SortOrder.NAME_DESC to "İsim (Z-A)",
                    SortOrder.PRICE_ASC to "Fiyat (Ucuzdan)",
                    SortOrder.PRICE_DESC to "Fiyat (Pahalıdan)",
                    SortOrder.CHANGE_DESC to "En Çok Zamlanlar"
                )
                options.forEach { (order, label) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentSort == order,
                            onClick = { onSortSelected(order) }
                        )
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tamam") }
        }
    )
}

@Composable
private fun FilterDialog(
    currentFilter: FilterType,
    markets: List<String>,
    brands: List<String>,
    selectedMarket: String?,
    selectedBrand: String?,
    onFilterSelected: (FilterType) -> Unit,
    onMarketSelected: (String?) -> Unit,
    onBrandSelected: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtrele") },
        text = {
            Column {
                Text("Kategori", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = currentFilter == FilterType.ALL, onClick = { onFilterSelected(FilterType.ALL) })
                    Text("Tümü")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = currentFilter == FilterType.FAVORITES, onClick = { onFilterSelected(FilterType.FAVORITES) })
                    Text("Favoriler")
                }
                if (markets.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Market", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(markets) { market ->
                            FilterChip(
                                selected = selectedMarket == market,
                                onClick = {
                                    onFilterSelected(FilterType.BY_MARKET)
                                    onMarketSelected(if (selectedMarket == market) null else market)
                                },
                                label = { Text(market, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tamam") }
        }
    )
}
