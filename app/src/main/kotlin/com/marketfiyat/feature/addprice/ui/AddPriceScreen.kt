package com.marketfiyat.feature.addprice.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marketfiyat.core.ui.components.*
import com.marketfiyat.core.util.UnitType
import com.marketfiyat.core.util.formatPrice
import com.marketfiyat.feature.addprice.viewmodel.AddPriceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPriceScreen(
    productId: Long?,
    initialBarcode: String?,
    onNavigateBack: () -> Unit,
    onNavigateToBarcode: () -> Unit,
    onNavigateToOcr: () -> Unit,
    viewModel: AddPriceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    LaunchedEffect(productId) {
        productId?.let { viewModel.loadProduct(it) }
    }
    LaunchedEffect(initialBarcode) {
        initialBarcode?.let { viewModel.loadByBarcode(it) }
    }
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    Scaffold(
        topBar = {
            MarketFiyatTopBar(
                title = if (uiState.existingProduct != null) "Fiyat Güncelle" else "Yeni Fiyat Ekle",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = onNavigateToBarcode) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Barkod Tara")
                    }
                    IconButton(onClick = onNavigateToOcr) {
                        Icon(Icons.Default.DocumentScanner, contentDescription = "Fiş Oku")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ─── Ürün Bilgileri ───────────────────────────────────────
                Text(
                    "Ürün Bilgileri",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = uiState.productName,
                    onValueChange = viewModel::onProductNameChange,
                    label = { Text("Ürün Adı *") },
                    placeholder = { Text("örn: Kaşar Peyniri") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.errors.containsKey("productName"),
                    supportingText = uiState.errors["productName"]?.let { { Text(it) } },
                    leadingIcon = { Icon(Icons.Default.ShoppingBasket, null) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.brand,
                    onValueChange = viewModel::onBrandChange,
                    label = { Text("Marka") },
                    placeholder = { Text("örn: Pınar") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Label, null) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    singleLine = true
                )

                HorizontalDivider()

                // ─── Market Bilgisi ───────────────────────────────────────
                Text(
                    "Market Bilgisi",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                MarketDropdown(
                    value = uiState.marketName,
                    markets = uiState.availableMarkets,
                    onValueChange = viewModel::onMarketNameChange,
                    isError = uiState.errors.containsKey("marketName"),
                    errorMessage = uiState.errors["marketName"]
                )

                HorizontalDivider()

                // ─── Fiyat ve Miktar ──────────────────────────────────────
                Text(
                    "Fiyat ve Miktar",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.price,
                        onValueChange = viewModel::onPriceChange,
                        label = { Text("Fiyat (TL) *") },
                        modifier = Modifier.weight(1f),
                        isError = uiState.errors.containsKey("price"),
                        supportingText = uiState.errors["price"]?.let { { Text(it) } },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = uiState.discountedPrice,
                        onValueChange = viewModel::onDiscountedPriceChange,
                        label = { Text("İndirimli") },
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.Sell, null) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.quantity,
                        onValueChange = viewModel::onQuantityChange,
                        label = { Text("Miktar") },
                        modifier = Modifier.weight(1f),
                        isError = uiState.errors.containsKey("quantity"),
                        leadingIcon = { Icon(Icons.Default.Scale, null) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true
                    )
                    UnitSelector(
                        selectedUnit = uiState.selectedUnit,
                        onUnitSelected = viewModel::onUnitChange,
                        modifier = Modifier.weight(1f)
                    )
                }

                // ─── Otomatik Birim Fiyat Hesabı ─────────────────────────
                AnimatedVisibility(
                    visible = uiState.unitPrices != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    uiState.unitPrices?.let { prices ->
                        UnitPriceCard(prices = prices)
                    }
                }

                // ─── Real-time Analiz ─────────────────────────────────────
                AnimatedVisibility(
                    visible = uiState.analysisMessage != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    uiState.analysisMessage?.let { msg ->
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = if (uiState.isAboveAverage)
                                MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = if (uiState.isAboveAverage)
                                        MaterialTheme.colorScheme.onErrorContainer
                                    else MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (uiState.isAboveAverage)
                                        MaterialTheme.colorScheme.onErrorContainer
                                    else MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()

                // ─── Tarih ve Not ─────────────────────────────────────────
                Text(
                    "Ek Bilgiler",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                val dateStr = SimpleDateFormat("dd MMMM yyyy", Locale("tr"))
                    .format(Date(uiState.purchaseDate))
                OutlinedTextField(
                    value = dateStr,
                    onValueChange = {},
                    label = { Text("Tarih") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    leadingIcon = { Icon(Icons.Default.DateRange, null) },
                    trailingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = "Tarih Seç")
                    }
                )

                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = viewModel::onNoteChange,
                    label = { Text("Not") },
                    placeholder = { Text("Opsiyonel not ekleyin...") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Notes, null) },
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ─── Kaydet Butonu ────────────────────────────────────────
                Button(
                    onClick = viewModel::savePrice,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kaydet", style = MaterialTheme.typography.labelLarge)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    uiState.error?.let { msg ->
        LaunchedEffect(msg) {
            viewModel.dismissError()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarketDropdown(
    value: String,
    markets: List<String>,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    errorMessage: String?
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded && markets.isNotEmpty(),
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Market *") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable),
            isError = isError,
            supportingText = errorMessage?.let { { Text(it) } },
            leadingIcon = { Icon(Icons.Default.Store, null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            singleLine = true
        )
        if (markets.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                markets.filter { it.contains(value, ignoreCase = true) }.forEach { market ->
                    DropdownMenuItem(
                        text = { Text(market) },
                        onClick = {
                            onValueChange(market)
                            expanded = false
                        },
                        leadingIcon = { Icon(Icons.Default.Store, null, Modifier.size(18.dp)) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitSelector(
    selectedUnit: UnitType,
    onUnitSelected: (UnitType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedUnit.shortName,
            onValueChange = {},
            label = { Text("Birim") },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            UnitType.entries.forEach { unit ->
                DropdownMenuItem(
                    text = { Text("${unit.displayName} (${unit.shortName})") },
                    onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun UnitPriceCard(prices: com.marketfiyat.core.util.UnitPrices) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "Birim Fiyat Hesabı",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                prices.perKg?.let {
                    UnitPriceChip(label = "1 KG", value = it.formatPrice())
                }
                prices.per100g?.let {
                    UnitPriceChip(label = "100 GR", value = it.formatPrice())
                }
                prices.perLitre?.let {
                    UnitPriceChip(label = "1 LT", value = it.formatPrice())
                }
                prices.perPiece?.let {
                    UnitPriceChip(label = "Adet", value = it.formatPrice())
                }
            }
        }
    }
}

@Composable
private fun UnitPriceChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
