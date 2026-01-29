package com.drape.ui.myOutfit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drape.data.model.Outfit
import com.drape.data.repository.OutfitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the saved outfits screen.
 */
data class SavedOutfitsUiState(
    val isLoading: Boolean = true,
    val outfits: List<Outfit> = emptyList(),
    val errorMessage: String? = null,
    val selectedOutfit: Outfit? = null,
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,
    val favoriteOutfitIds: Set<String> = emptySet()
)

/**
 * ViewModel for managing the saved outfits screen.
 */
@HiltViewModel
class SavedOutfitsViewModel @Inject constructor(
    private val outfitRepository: OutfitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedOutfitsUiState())
    val uiState: StateFlow<SavedOutfitsUiState> = _uiState.asStateFlow()

    private var outfitsLoadJob: Job? = null

    init {
        loadOutfits()
    }

    /**
     * Loads saved outfits from the repository.
     */
    private fun loadOutfits() {
        outfitsLoadJob?.cancel()
        outfitsLoadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            outfitRepository.getUserOutfits()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Errore nel caricamento degli outfit"
                    )
                }
                .collect { outfits ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        outfits = outfits,
                        errorMessage = null
                    )
                }
        }
    }

    /**
     * Refreshes the outfits list.
     */
    fun refresh() {
        loadOutfits()
    }

    /**
     * Deletes the specified outfit.
     */
    fun deleteOutfit(outfitId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, deleteSuccess = false)
            
            try {
                val success = outfitRepository.deleteOutfit(outfitId)
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    deleteSuccess = success,
                    selectedOutfit = if (success && _uiState.value.selectedOutfit?.id == outfitId) null else _uiState.value.selectedOutfit,
                    errorMessage = if (!success) "Impossibile eliminare l'outfit" else null
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
     * Toggles the favorite status of an outfit (Local mock for now).
     */
    fun toggleFavorite(outfit: Outfit) {
        val currentFavorites = _uiState.value.favoriteOutfitIds.toMutableSet()
        if (currentFavorites.contains(outfit.id)) {
            currentFavorites.remove(outfit.id)
        } else {
            currentFavorites.add(outfit.id)
        }
        _uiState.value = _uiState.value.copy(favoriteOutfitIds = currentFavorites)
    }

    /**
     * Selects an outfit for zooming.
     */
    fun selectOutfit(outfit: Outfit?) {
        _uiState.value = _uiState.value.copy(selectedOutfit = outfit)
    }

    /**
     * Clears error message.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
