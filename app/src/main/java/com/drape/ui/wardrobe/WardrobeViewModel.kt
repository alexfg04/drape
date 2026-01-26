package com.drape.ui.wardrobe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drape.data.model.ClothingItem
import com.drape.data.repository.ClothesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the wardrobe screen.
 */
data class WardrobeUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val clothingItems: List<ClothingItem> = emptyList(),
    val selectedItem: ClothingItem? = null,
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false
)

/**
 * ViewModel for managing the wardrobe screen.
 * Fetches clothing items from the repository and handles delete operations.
 */
@HiltViewModel
class WardrobeViewModel @Inject constructor(
    private val clothesRepository: ClothesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WardrobeUiState())
    val uiState: StateFlow<WardrobeUiState> = _uiState.asStateFlow()

    private var clothingLoadJob: Job? = null

    init {
        loadClothingItems()
    }

    /**
     * Loads clothing items from the repository.
     * Subscribes to the Flow to receive real-time updates.
     */
    private fun loadClothingItems() {
        clothingLoadJob?.cancel()
        clothingLoadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            clothesRepository.getUserClothingItems()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Errore nel caricamento dei vestiti"
                    )
                }
                .collect { items ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        clothingItems = items,
                        errorMessage = null
                    )
                }
        }
    }

    /**
     * Refreshes the clothing items list.
     */
    fun refresh() {
        loadClothingItems()
    }

    /**
     * Selects a clothing item to show details.
     */
    fun selectItem(item: ClothingItem) {
        _uiState.value = _uiState.value.copy(selectedItem = item)
    }

    /**
     * Clears the selected item.
     */
    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedItem = null)
    }

    /**
     * Deletes the specified clothing item.
     */
    fun deleteClothingItem(clothingId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, deleteSuccess = false)
            
            try {
                val success = clothesRepository.deleteClothingItem(clothingId)
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    deleteSuccess = success,
                    selectedItem = null,
                    errorMessage = if (!success) "Impossibile eliminare il capo" else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    deleteSuccess = false,
                    errorMessage = e.localizedMessage ?: "Errore durante l'eliminazione"
                )
            }
        }
    }

    /**
     * Clears any error message.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Clears the delete success state.
     */
    fun clearDeleteSuccess() {
        _uiState.value = _uiState.value.copy(deleteSuccess = false)
    }
}
