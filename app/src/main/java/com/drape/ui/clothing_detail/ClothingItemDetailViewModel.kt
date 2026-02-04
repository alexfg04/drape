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
 */
data class ClothingItemDetailUiState(
    val item: ClothingItem? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class ClothingItemDetailViewModel @Inject constructor(
    private val clothesRepository: ClothesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClothingItemDetailUiState())
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

    fun deleteItem(onSuccess: () -> Unit) {
        val itemId = _uiState.value.item?.id ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val success = clothesRepository.deleteClothingItem(itemId)
                if (success) {
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
