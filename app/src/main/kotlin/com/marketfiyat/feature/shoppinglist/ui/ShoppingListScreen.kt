package com.marketfiyat.feature.shoppinglist.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marketfiyat.core.ui.components.*
import com.marketfiyat.core.util.formatPrice
import com.marketfiyat.feature.shoppinglist.viewmodel.ShoppingListDetailViewModel
import com.marketfiyat.feature.shoppinglist.viewmodel.ShoppingListViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            MarketFiyatTopBar(title = "Alışveriş Listeleri", onNavigateBack = onNavigateBack)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::toggleCreating) {
                Icon(Icons.Default.Add, contentDescription = "Yeni Liste")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // New list input
            AnimatedVisibility(
                visible = uiState.isCreatingList,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Yeni Liste", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.newListName,
                            onValueChange = viewModel::onNewListNameChange,
                            label = { Text("Liste Adı") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = viewModel::toggleCreating) { Text("İptal") }
                            Button(
                                onClick = viewModel::createList,
                                enabled = uiState.newListName.isNotBlank()
                            ) { Text("Oluştur") }
                        }
                    }
                }
            }

            when {
                uiState.isLoading -> {
                    LazyColumn {
                        items(4) { ShimmerProductCard() }
                    }
                }
                uiState.lists.isEmpty() -> {
                    EmptyStateView(
                        icon = Icons.Default.ShoppingCart,
                        title = "Alışveriş listesi yok",
                        description = "Yeni bir alışveriş listesi oluşturun",
                        actionLabel = "Liste Oluştur",
                        onAction = viewModel::toggleCreating
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.lists, key = { it.id }) { list ->
                            ShoppingListCard(
                                list = list,
                                onClick = { onNavigateToDetail(list.id) },
                                onDelete = { viewModel.deleteList(list.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShoppingListCard(
    list: com.marketfiyat.core.domain.model.ShoppingList,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale("tr")).format(Date(list.createdAt))
    val checkedCount = list.items.count { it.isChecked }
    val totalCount = list.items.size

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (list.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (list.isCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(list.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "$checkedCount/$totalCount ürün tamamlandı • $dateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (list.estimatedTotal > 0) {
                    Text(
                        "Tahmini: ${list.estimatedTotal.formatPrice()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
            }
        }
        if (totalCount > 0) {
            LinearProgressIndicator(
                progress = { if (totalCount > 0) checkedCount.toFloat() / totalCount else 0f },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListDetailScreen(
    listId: Long,
    onNavigateBack: () -> Unit,
    viewModel: ShoppingListDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(listId) {
        viewModel.loadList(listId)
    }

    Scaffold(
        topBar = {
            MarketFiyatTopBar(
                title = uiState.list?.name ?: "Alışveriş Listesi",
                onNavigateBack = onNavigateBack,
                actions = {
                    if (uiState.list != null) {
                        IconButton(onClick = { viewModel.deleteCheckedItems(listId) }) {
                            Icon(Icons.Default.DeleteSweep, "Tamamlananları Sil")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::toggleAddingItem) {
                Icon(Icons.Default.Add, contentDescription = "Ürün Ekle")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Estimated total banner
            if (uiState.estimatedTotal > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tahmini Toplam", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            uiState.estimatedTotal.formatPrice(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Add item input
            AnimatedVisibility(
                visible = uiState.isAddingItem,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        OutlinedTextField(
                            value = uiState.newItemName,
                            onValueChange = viewModel::onNewItemNameChange,
                            label = { Text("Ürün Adı") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { viewModel.addItem(listId) }) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                }
                            }
                        )
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = viewModel::toggleAddingItem) { Text("İptal") }
                            Button(onClick = { viewModel.addItem(listId) }, enabled = uiState.newItemName.isNotBlank()) {
                                Text("Ekle")
                            }
                        }
                    }
                }
            }

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.list?.items?.isEmpty() == true -> {
                    EmptyStateView(
                        icon = Icons.Default.AddShoppingCart,
                        title = "Liste boş",
                        description = "Alışveriş listenize ürün ekleyin",
                        actionLabel = "Ürün Ekle",
                        onAction = viewModel::toggleAddingItem
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Group by market
                        uiState.itemsByMarket.forEach { (market, items) ->
                            item(key = "header_$market") {
                                SectionHeader(
                                    title = "🏪 $market",
                                    action = {
                                        Text(
                                            "${items.sumOf { (it.estimatedPrice ?: 0.0) * it.quantity }.formatPrice()}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                )
                            }
                            items(items, key = { "item_${it.id}" }) { item ->
                                ShoppingListItemRow(
                                    item = item,
                                    onToggleChecked = {
                                        viewModel.toggleItemChecked(item.id, !item.isChecked)
                                    },
                                    onRemove = { viewModel.removeItem(item.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShoppingListItemRow(
    item: com.marketfiyat.core.domain.model.ShoppingListItem,
    onToggleChecked: () -> Unit,
    onRemove: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                item.productName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (item.isChecked) FontWeight.Normal else FontWeight.Medium,
                color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            if (item.estimatedPrice != null) {
                Text(
                    "~${item.estimatedPrice.formatPrice()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        leadingContent = {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { onToggleChecked() }
            )
        },
        trailingContent = {
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, contentDescription = "Kaldır", modifier = Modifier.size(16.dp))
            }
        }
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}
