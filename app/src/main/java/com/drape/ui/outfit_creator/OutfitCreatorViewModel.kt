package com.drape.ui.outfit_creator

import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drape.data.model.ClothingItem
import com.drape.data.model.ItemCategory
import com.drape.data.model.Outfit
import com.drape.data.model.PlacedItem
import com.drape.data.repository.ClothesRepository
import com.drape.data.repository.OutfitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlacedItemState(
    val clothingItem: ClothingItem,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val offset: Offset = Offset.Zero,
    val zIndex: Int = 0
)

data class OutfitCreatorUiState(
    val availableClothes: List<ClothingItem> = emptyList(),
    val placedItems: Map<ItemCategory, PlacedItemState?> = emptyMap(),
    val selectedCategory: ItemCategory = ItemCategory.TOP,
    val isSelectionVisible: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    val canvasOffset: Offset = Offset.Zero
)

@HiltViewModel
class OutfitCreatorViewModel @Inject constructor(
    private val clothesRepository: ClothesRepository,
    private val outfitRepository: OutfitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OutfitCreatorUiState())
    val uiState: StateFlow<OutfitCreatorUiState> = _uiState.asStateFlow()

    init {
        loadClothes()
    }

    private fun loadClothes() {
        viewModelScope.launch {
            clothesRepository.getUserClothingItems().collect { items ->
                _uiState.update { it.copy(availableClothes = items) }
            }
        }
    }

    fun selectCategory(category: ItemCategory) {
        _uiState.update { it.copy(selectedCategory = category, isSelectionVisible = true) }
    }

    fun toggleSelectionVisibility(visible: Boolean) {
        _uiState.update { it.copy(isSelectionVisible = visible) }
    }

    fun selectItem(category: ItemCategory, item: ClothingItem?) {
        _uiState.update { state ->
            val newPlacedItems = state.placedItems.toMutableMap()
            if (item == null) {
                newPlacedItems.remove(category)
            } else {
                // Initialize with default transformation if it doesn't exist
                val existing = newPlacedItems[category]
                if (existing?.clothingItem?.id != item.id) {
                    newPlacedItems[category] = PlacedItemState(
                        clothingItem = item,
                        zIndex = getZIndexForCategory(category),
                        offset = getDefaultOffsetForCategory(category)
                    )
                }
            }
            state.copy(placedItems = newPlacedItems)
        }
    }

    fun updateTransform(category: ItemCategory, scale: Float? = null, rotation: Float? = null, offset: Offset? = null) {
        _uiState.update { state ->
            val itemState = state.placedItems[category] ?: return@update state
            val newPlacedItems = state.placedItems.toMutableMap()
            newPlacedItems[category] = itemState.copy(
                scale = scale ?: itemState.scale,
                rotation = rotation ?: itemState.rotation,
                offset = offset ?: itemState.offset
            )
            state.copy(placedItems = newPlacedItems)
        }
    }

    fun updateCanvasOffset(delta: Offset) {
        _uiState.update { it.copy(canvasOffset = it.canvasOffset + delta) }
    }

    fun saveOutfit(thumbnailUri: Uri? = null) {
        val currentState = _uiState.value
        if (currentState.placedItems.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Seleziona almeno un capo") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val outfitItems = currentState.placedItems.mapNotNull { (category, itemState) ->
                    itemState?.let {
                        PlacedItem(
                            itemId = it.clothingItem.id,
                            category = category,
                            posX = it.offset.x,
                            posY = it.offset.y,
                            scale = it.scale,
                            rotation = it.rotation,
                            zIndex = it.zIndex
                        )
                    }
                }

                val outfit = Outfit(
                    name = "Mio Outfit ${System.currentTimeMillis()}", // TODO: Allow user to set name
                    items = outfitItems
                )

                outfitRepository.saveOutfit(outfit, thumbnailUri)
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
            }
        }
    }

    private fun getZIndexForCategory(category: ItemCategory): Int {
        return when (category) {
            ItemCategory.SHOES -> 0
            ItemCategory.BOTTOM -> 1
            ItemCategory.TOP -> 2
            ItemCategory.ACCESSORIES -> 3
        }
    }

    private fun getDefaultOffsetForCategory(category: ItemCategory): Offset {
        // These values should ideally come from screen density or be normalized
        return when (category) {
            ItemCategory.TOP -> Offset(0f, -300f)
            ItemCategory.BOTTOM -> Offset(0f, 300f)
            ItemCategory.SHOES -> Offset(0f, 800f)
            ItemCategory.ACCESSORIES -> Offset(300f, -300f)
        }
    }
}
