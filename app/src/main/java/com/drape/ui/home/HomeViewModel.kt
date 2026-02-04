package com.drape.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drape.data.model.ClothingItem
import com.drape.data.model.Outfit
import com.drape.data.repository.ClothesRepository
import com.drape.data.repository.OutfitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * UI state for the Home screen.
 */
data class HomeUiState(
    val outfits: List<Outfit> = emptyList(),
    val recentClothes: List<ClothingItem> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val outfitRepository: OutfitRepository,
    private val clothesRepository: ClothesRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        outfitRepository.getUserOutfits(),
        clothesRepository.getUserClothingItems()
    ) { outfits, clothes ->
        // Get last 4 outfits sorted by creation date (newest first)
        val recentOutfits = outfits
            .sortedByDescending { it.createdAt?.toDate()?.time ?: 0 }
            .take(4)
        
        // Get last 5 clothing items sorted by creation date (newest first)
        val recentClothing = clothes
            .sortedByDescending { it.createdAt?.toDate()?.time ?: 0 }
            .take(5)
        
        HomeUiState(
            outfits = recentOutfits,
            recentClothes = recentClothing,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )
}
