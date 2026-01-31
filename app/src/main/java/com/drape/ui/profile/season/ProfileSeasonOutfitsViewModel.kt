package com.drape.ui.profile.season

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drape.data.model.Outfit
import com.drape.data.repository.ClothesRepository
import com.drape.data.repository.OutfitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SeasonOutfitsUiState(
    val isLoading: Boolean = true,
    val outfits: List<Outfit> = emptyList(),
    val season: String = ""
)

@HiltViewModel
class ProfileSeasonOutfitsViewModel @Inject constructor(
    private val outfitRepository: OutfitRepository,
    private val clothesRepository: ClothesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeasonOutfitsUiState())
    val uiState: StateFlow<SeasonOutfitsUiState> = _uiState.asStateFlow()

    fun loadMarkedSeason(season: String) {
        _uiState.value = _uiState.value.copy(season = season, isLoading = true)
        
        viewModelScope.launch {
            combine(
                outfitRepository.getUserOutfits(),
                clothesRepository.getUserClothingItems()
            ) { outfits, clothes ->
                val clothesMap = clothes.associateBy { it.id }
                
                outfits.filter { outfit ->
                    val seasonItemCount = outfit.items.count { placedItem ->
                        val clothingItem = clothesMap[placedItem.itemId]
                        val itemSeason = clothingItem?.season?.trim() ?: ""
                        
                        // Count item if it matches the specific season OR is applicable to all seasons
                        itemSeason.equals(season.trim(), ignoreCase = true) || 
                        itemSeason.equals("Tutte le stagioni", ignoreCase = true)
                    }
                    seasonItemCount >= 2
                }
            }.collect { filteredOutfits ->
                _uiState.value = _uiState.value.copy(
                    outfits = filteredOutfits,
                    isLoading = false
                )
            }
        }
    }
}
