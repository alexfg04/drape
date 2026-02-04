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
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * UI state for the Home screen.
 * 
 * Contains the data needed to display the Home screen including recent outfits,
 * recent clothing items, and loading state.
 *
 * @property outfits List of recent outfits to display (last 4, newest first)
 * @property recentClothes List of recent clothing items to display (last 5, newest first)
 * @property isLoading Whether the data is currently being loaded
 */
data class HomeUiState(
    val outfits: List<Outfit> = emptyList(),
    val recentClothes: List<ClothingItem> = emptyList(),
    val isLoading: Boolean = true
)

/**
 * ViewModel for the Home screen.
 * 
 * Manages the data displayed on the Home screen including:
 * - Recent outfits (last 4 created, sorted by creation date)
 * - Recent clothing items (last 5 added, sorted by creation date)
 * 
 * The ViewModel combines flows from [OutfitRepository] and [ClothesRepository]
 * to provide real-time updates when the underlying data changes.
 *
 * @param outfitRepository Repository for accessing outfit data
 * @param clothesRepository Repository for accessing clothing items data
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val outfitRepository: OutfitRepository,
    private val clothesRepository: ClothesRepository
) : ViewModel() {

    /**
     * Combined UI state flow that automatically updates when data changes.
     * 
     * Collects the latest outfits and clothing items, sorts them by creation date
     * (newest first), and limits the results for display on the Home screen.
     * Uses [SharingStarted.WhileSubscribed] to keep the flow active while there are subscribers.
     */
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
