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

/**
 * Represents the state of a single clothing item placed in the outfit creator's canvas.
 *
 * @property clothingItem The original [ClothingItem] data.
 * @property scale The current scale factor of the item.
 * @property rotation The current rotation degree of the item.
 * @property offset The (x, y) coordinates of the item relative to the canvas center.
 * @property zIndex The stacking order of the item.
 */
data class PlacedItemState(
    val clothingItem: ClothingItem,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val offset: Offset = Offset.Zero,
    val zIndex: Int = 0
)

/**
 * UI State for the Outfit Creator screen.
 *
 * @property availableClothes List of items available to be added to the outfit.
 * @property placedItems A map of categories to their placed item states.
 * @property selectedCategory The currently active category for editing.
 * @property isSelectionVisible Flag for showing/hiding the item selection menu.
 * @property isSaving Flag indicating an outfit save operation is in progress.
 * @property saveSuccess Flag indicating an outfit was saved successfully.
 * @property errorResId Resource ID for a localized error message.
 * @property errorMessage A generic error message string.
 * @property canvasOffset The global scroll/pan offset of the editor canvas.
 */
data class OutfitCreatorUiState(
    val availableClothes: List<ClothingItem> = emptyList(),
    val placedItems: Map<ItemCategory, PlacedItemState?> = emptyMap(),
    val selectedCategory: ItemCategory = ItemCategory.TOP,
    val isSelectionVisible: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorResId: Int? = null,
    val errorMessage: String? = null,
    val canvasOffset: Offset = Offset.Zero,
    val currentOutfitId: String? = null
)

/**
 * ViewModel for the Outfit Creator screen.
 * Manages the placement, transformation, and saving of an outfit.
 */
@HiltViewModel
class OutfitCreatorViewModel @Inject constructor(
    private val clothesRepository: ClothesRepository,
    private val outfitRepository: OutfitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OutfitCreatorUiState())
    val uiState: StateFlow<OutfitCreatorUiState> = _uiState.asStateFlow()

    private var currentOutfitId: String? = null

    init {
        loadClothes()
    }

    /**
     * Loads the available clothing items from the repository.
     */
    private fun loadClothes() {
        viewModelScope.launch {
            clothesRepository.getUserClothingItems().collect { items ->
                _uiState.update { it.copy(availableClothes = items) }
            }
        }
    }

    /**
     * Loads an existing outfit for editing.
     *
     * @param outfitId The ID of the outfit to load.
     */
    fun loadOutfit(outfitId: String) {
        viewModelScope.launch {
            try {
                // If we are already editing this outfit, don't reload to avoid overwriting changes
                if (currentOutfitId == outfitId) return@launch

                currentOutfitId = outfitId
                _uiState.update { it.copy(currentOutfitId = outfitId) }

                val outfit = outfitRepository.getOutfit(outfitId)
                if (outfit != null) {
                    val newPlacedItems = mutableMapOf<ItemCategory, PlacedItemState?>()
                    
                    outfit.items.forEach { placedItem ->
                        val clothingItem = clothesRepository.getClothingItem(placedItem.itemId)
                        if (clothingItem != null) {
                            newPlacedItems[placedItem.category] = PlacedItemState(
                                clothingItem = clothingItem,
                                scale = placedItem.scale,
                                rotation = placedItem.rotation,
                                offset = Offset(placedItem.posX, placedItem.posY),
                                zIndex = placedItem.zIndex ?: getZIndexForCategory(placedItem.category)
                            )
                        }
                    }
                    
                    _uiState.update { 
                        it.copy(placedItems = newPlacedItems) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to load outfit: ${e.message}") }
            }
        }
    }

    /**
     * Updates the currently selected category for editing.
     *
     * @param category The [ItemCategory] to select.
     */
    fun selectCategory(category: ItemCategory) {
        _uiState.update { it.copy(selectedCategory = category, isSelectionVisible = true) }
    }

    /**
     * Toggles the visibility of the item selection gallery.
     *
     * @param visible True to show the gallery, false to hide it.
     */
    fun toggleSelectionVisibility(visible: Boolean) {
        _uiState.update { it.copy(isSelectionVisible = visible) }
    }

    /**
     * Places or removes a clothing item for a specific category.
     *
     * @param category The category slot to update.
     * @param item The [ClothingItem] to place, or null to remove it.
     */
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

    /**
     * Updates the visual transformation of a placed item.
     *
     * @param category The category of the item to update.
     * @param scale The new scale value.
     * @param rotation The new rotation degree.
     * @param offset The new (x, y) offset.
     */
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

    /**
     * Adjusts the overall canvas pan/scroll offset.
     *
     * @param delta The change in offset to apply.
     */
    fun updateCanvasOffset(delta: Offset) {
        _uiState.update { it.copy(canvasOffset = it.canvasOffset + delta) }
    }

    /**
     * Resets the transformation of a specific item to its category-specific defaults.
     *
     * @param category The category of the item to reset.
     */
    fun resetTransform(category: ItemCategory) {
        _uiState.update { state ->
            val itemState = state.placedItems[category] ?: return@update state
            val newPlacedItems = state.placedItems.toMutableMap()
            newPlacedItems[category] = itemState.copy(
                scale = 1f,
                rotation = 0f,
                offset = getDefaultOffsetForCategory(category)
            )
            state.copy(placedItems = newPlacedItems)
        }
    }

    /**
     * Resets the save success flag.
     */
    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    /**
     * Clears any active error states in the UI.
     */
    fun clearError() {
        _uiState.update { it.copy(errorResId = null, errorMessage = null) }
    }

    /**
     * Saves the current outfit to the remote repository.
     *
     * @param thumbnailUri The URI of the captured outfit thumbnail image.
     */
    fun saveOutfit(thumbnailUri: Uri? = null) {
        val currentState = _uiState.value
        if (currentState.placedItems.isEmpty()) {
            _uiState.update { it.copy(errorResId = com.drape.R.string.outfit_creator_error_empty) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorResId = null, errorMessage = null, saveSuccess = false) }
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
                    items = outfitItems,
                    id = currentOutfitId ?: "" // Preserve ID if editing, otherwise empty for new
                )

                outfitRepository.saveOutfit(outfit, thumbnailUri)
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
            }
        }
    }

    /**
     * Calculates the Z-index (layer) for a category to ensure proper overlapping.
     *
     * @param category The [ItemCategory].
     * @return The integer Z-index value.
     */
    private fun getZIndexForCategory(category: ItemCategory): Int {
        return when (category) {
            ItemCategory.SHOES -> 0
            ItemCategory.BOTTOM -> 1
            ItemCategory.TOP -> 2
            ItemCategory.ACCESSORIES -> 3
        }
    }

    /**
     * Returns the default (starting) position offset for a specific category on the canvas.
     *
     * @param category The [ItemCategory].
     * @return The default [Offset].
     */
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
