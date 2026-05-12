package com.marketfiyat.feature.shoppinglist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketfiyat.core.domain.model.ShoppingList
import com.marketfiyat.core.domain.model.ShoppingListItem
import com.marketfiyat.core.domain.repository.ShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ShoppingListUiState(
    val isLoading: Boolean = true,
    val lists: List<ShoppingList> = emptyList(),
    val error: String? = null,
    val isCreatingList: Boolean = false,
    val newListName: String = ""
)

data class ShoppingListDetailUiState(
    val isLoading: Boolean = true,
    val list: ShoppingList? = null,
    val itemsByMarket: Map<String, List<ShoppingListItem>> = emptyMap(),
    val estimatedTotal: Double = 0.0,
    val error: String? = null,
    val newItemName: String = "",
    val isAddingItem: Boolean = false
)

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val shoppingListRepository: ShoppingListRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    init {
        loadLists()
    }

    private fun loadLists() {
        viewModelScope.launch {
            shoppingListRepository.getAllLists()
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    Timber.e(e)
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collectLatest { lists ->
                    _uiState.update { it.copy(isLoading = false, lists = lists) }
                }
        }
    }

    fun onNewListNameChange(name: String) {
        _uiState.update { it.copy(newListName = name) }
    }

    fun createList() {
        val name = _uiState.value.newListName.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            try {
                shoppingListRepository.createList(name)
                _uiState.update { it.copy(newListName = "", isCreatingList = false) }
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.update { it.copy(error = "Liste oluşturulamadı: ${e.message}") }
            }
        }
    }

    fun deleteList(listId: Long) {
        viewModelScope.launch {
            try {
                shoppingListRepository.deleteList(listId)
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.update { it.copy(error = "Liste silinemedi") }
            }
        }
    }

    fun toggleCreating() {
        _uiState.update { it.copy(isCreatingList = !it.isCreatingList, newListName = "") }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}

@HiltViewModel
class ShoppingListDetailViewModel @Inject constructor(
    private val shoppingListRepository: ShoppingListRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShoppingListDetailUiState())
    val uiState: StateFlow<ShoppingListDetailUiState> = _uiState.asStateFlow()

    fun loadList(listId: Long) {
        viewModelScope.launch {
            shoppingListRepository.getListWithItems(listId)
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    Timber.e(e)
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collectLatest { list ->
                    if (list != null) {
                        val byMarket = groupItemsByMarket(list.items)
                        val total = list.items.sumOf {
                            (it.estimatedPrice ?: 0.0) * it.quantity
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                list = list,
                                itemsByMarket = byMarket,
                                estimatedTotal = total
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
        }
    }

    private fun groupItemsByMarket(items: List<ShoppingListItem>): Map<String, List<ShoppingListItem>> {
        val result = mutableMapOf<String, MutableList<ShoppingListItem>>()
        items.forEach { item ->
            val market = item.bestMarket ?: "Belirsiz"
            result.getOrPut(market) { mutableListOf() }.add(item)
        }
        return result
    }

    fun onNewItemNameChange(name: String) {
        _uiState.update { it.copy(newItemName = name) }
    }

    fun addItem(listId: Long) {
        val name = _uiState.value.newItemName.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            try {
                val (bestMarket, estPrice) = shoppingListRepository.findBestMarketForItem(name)
                val item = ShoppingListItem(
                    listId = listId,
                    productName = name,
                    bestMarket = bestMarket,
                    estimatedPrice = estPrice
                )
                shoppingListRepository.addItemToList(item)
                _uiState.update { it.copy(newItemName = "", isAddingItem = false) }
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.update { it.copy(error = "Ürün eklenemedi") }
            }
        }
    }

    fun toggleItemChecked(itemId: Long, isChecked: Boolean) {
        viewModelScope.launch {
            shoppingListRepository.toggleItemChecked(itemId, isChecked)
        }
    }

    fun removeItem(itemId: Long) {
        viewModelScope.launch {
            shoppingListRepository.removeItemFromList(itemId)
        }
    }

    fun deleteCheckedItems(listId: Long) {
        viewModelScope.launch {
            shoppingListRepository.deleteCheckedItems(listId)
        }
    }

    fun toggleAddingItem() {
        _uiState.update { it.copy(isAddingItem = !it.isAddingItem, newItemName = "") }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
