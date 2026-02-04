package com.drape.ui.clothing_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drape.data.model.ClothingItem
import com.drape.data.repository.ClothesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the clothing item detail screen.
 * 
 * Contains all the data needed to display a clothing item's details
 * including the item itself, loading state, and any error messages.
 *
 * @property item The clothing item to display, null if not loaded or not found
 * @property isLoading Whether the item data is currently being loaded
 * @property errorMessage Error message to display if loading fails, null if no error
 */
data class ClothingItemDetailUiState(
    val item: ClothingItem? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/**
 * ViewModel for the clothing item detail screen.
 * 
 * Manages the loading and deletion of a specific clothing item.
 * The item ID is retrieved from navigation arguments via [SavedStateHandle].
 * 
 * Features:
 * - Loads clothing item details from [ClothesRepository]
 * - Verifies user ownership of the item
 * - Handles item deletion with error handling
 * - Provides loading and error states for UI feedback
 *
 * @param clothesRepository Repository for accessing and modifying clothing item data
 * @param savedStateHandle Handle for accessing navigation arguments, specifically "itemId"
 */
@HiltViewModel
class ClothingItemDetailViewModel @Inject constructor(
    private val clothesRepository: ClothesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClothingItemDetailUiState())
    
    /**
     * Public read-only state flow for observing UI state changes.
     * UI components should collect this flow to react to state updates.
     */
    val uiState: StateFlow<ClothingItemDetailUiState> = _uiState.asStateFlow()

    init {
        // Get clothing item ID from navigation arguments
        val itemId: String? = savedStateHandle["itemId"]
        if (itemId != null) {
            loadClothingItem(itemId)
        } else {
            _uiState.value = ClothingItemDetailUiState(
                isLoading = false,
                errorMessage = "ID capo non trovato"
            )
        }
    }

    /**
     * Loads the clothing item details from the repository.
     * 
     * Fetches the item by ID and verifies that it belongs to the current user.
     * Updates the UI state accordingly with the loaded item or an error message.
     *
     * @param itemId The unique identifier of the clothing item to load
     */
    private fun loadClothingItem(itemId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val item = clothesRepository.getClothingItem(itemId)
                if (item != null) {
                    _uiState.value = ClothingItemDetailUiState(
                        item = item,
                        isLoading = false
                    )
                } else {
                    _uiState.value = ClothingItemDetailUiState(
                        isLoading = false,
                        errorMessage = "Capo non trovato o non autorizzato"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = ClothingItemDetailUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "Errore durante il caricamento"
                )
            }
        }
    }

    /**
     * Deletes the currently loaded clothing item.
     * 
     * Initiates the deletion process and invokes the success callback
     * if deletion is successful. Updates the UI state with any errors
     * that occur during the process.
     *
     * @param onSuccess Callback invoked when deletion completes successfully
     */
    fun deleteItem(onSuccess: () -> Unit) {
        val itemId = _uiState.value.item?.id ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val success = clothesRepository.deleteClothingItem(itemId)
                if (success) {
                    // Reset loading state before calling onSuccess to ensure UI state is clean
                    // even if navigation or downstream logic delays
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Errore durante l'eliminazione"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Errore durante l'eliminazione"
                )
            }
        }
    }

    /**
     * Clears any error message currently displayed.
     * 
     * Call this after displaying an error to the user (e.g., after showing a Snackbar)
     * to reset the error state.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
