package com.drape.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drape.data.model.ClothingItem
import com.drape.data.model.Outfit
import com.drape.data.model.PlannedDay
import com.drape.data.repository.ClothesRepository
import com.drape.data.repository.OutfitRepository
import com.drape.data.repository.PlannedDaysRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Data class representing outfit statistics
 */
data class OutfitStats(
    val totalOutfits: Int,
    val usedOutfits: Int,
    val unusedOutfits: Int,
    val usagePercentage: Float
) {
    companion object {
        val EMPTY = OutfitStats(0, 0, 0, 0f)
    }
}

/**
 * Data class representing clothing statistics
 */
data class ClothingStats(
    val totalClothes: Int,
    val usedClothes: Int,
    val unusedClothes: Int,
    val usagePercentage: Float,
    val byCategory: Map<String, Int>,
    val byColor: Map<String, Int>
) {
    companion object {
        val EMPTY = ClothingStats(0, 0, 0, 0f, emptyMap(), emptyMap())
    }
}

/**
 * Data class representing monthly usage statistics for charts
 */
data class MonthlyUsageStats(
    val month: String,
    val outfitCount: Int,
    val clothesCount: Int
)

/**
 * Data class representing top used outfits
 */
data class TopUsedOutfit(
    val outfitId: String,
    val outfitName: String,
    val usageCount: Int,
    val thumbnailUrl: String?
)

/**
 * Combined statistics state
 */
data class StatisticsUiState(
    val outfitStats: OutfitStats = OutfitStats.EMPTY,
    val clothingStats: ClothingStats = ClothingStats.EMPTY,
    val monthlyStats: List<MonthlyUsageStats> = emptyList(),
    val topUsedOutfits: List<TopUsedOutfit> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val outfitRepository: OutfitRepository,
    private val clothesRepository: ClothesRepository,
    private val plannedDaysRepository: PlannedDaysRepository
) : ViewModel() {

    private val outfitsFlow: Flow<List<Outfit>> = outfitRepository.getUserOutfits()
    private val clothesFlow: Flow<List<ClothingItem>> = clothesRepository.getUserClothingItems()
    private val plannedDaysFlow: Flow<List<PlannedDay>> = plannedDaysRepository.getAllPlannedDays()

    val uiState: StateFlow<StatisticsUiState> = combine(
        outfitsFlow,
        clothesFlow,
        plannedDaysFlow
    ) { outfits, clothes, plannedDays ->
        val outfitStats = calculateOutfitStats(outfits, plannedDays)
        val clothingStats = calculateClothingStats(clothes, outfits, plannedDays)
        val monthlyStats = calculateMonthlyStats(plannedDays)
        val topUsedOutfits = calculateTopUsedOutfits(outfits, plannedDays)

        StatisticsUiState(
            outfitStats = outfitStats,
            clothingStats = clothingStats,
            monthlyStats = monthlyStats,
            topUsedOutfits = topUsedOutfits,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatisticsUiState(isLoading = true)
    )

    private fun calculateOutfitStats(
        outfits: List<Outfit>,
        plannedDays: List<PlannedDay>
    ): OutfitStats {
        val totalOutfits = outfits.size
        
        // Get all outfit IDs that have been used in planner
        val usedOutfitIds = plannedDays
            .flatMap { it.items }
            .map { it.outfitId }
            .toSet()
        
        val usedOutfits = usedOutfitIds.size
        val unusedOutfits = totalOutfits - usedOutfits
        val usagePercentage = if (totalOutfits > 0) {
            (usedOutfits.toFloat() / totalOutfits) * 100
        } else 0f

        return OutfitStats(
            totalOutfits = totalOutfits,
            usedOutfits = usedOutfits,
            unusedOutfits = unusedOutfits,
            usagePercentage = usagePercentage
        )
    }

    private fun calculateClothingStats(
        clothes: List<ClothingItem>,
        outfits: List<Outfit>,
        plannedDays: List<PlannedDay>
    ): ClothingStats {
        val totalClothes = clothes.size
        
        // Get outfit IDs that have been used
        val usedOutfitIds = plannedDays
            .flatMap { it.items }
            .map { it.outfitId }
            .toSet()
        
        // Get clothing IDs used in planned outfits
        val usedClothingIds = outfits
            .filter { it.id in usedOutfitIds }
            .flatMap { it.items }
            .map { it.itemId }
            .toSet()
        
        val usedClothes = usedClothingIds.size
        val unusedClothes = totalClothes - usedClothes
        val usagePercentage = if (totalClothes > 0) {
            (usedClothes.toFloat() / totalClothes) * 100
        } else 0f

        // Group by category
        val byCategory = clothes.groupingBy { it.category }.eachCount()
        
        // Group by color
        val byColor = clothes.groupingBy { it.color }.eachCount()

        return ClothingStats(
            totalClothes = totalClothes,
            usedClothes = usedClothes,
            unusedClothes = unusedClothes,
            usagePercentage = usagePercentage,
            byCategory = byCategory,
            byColor = byColor
        )
    }

    private fun calculateMonthlyStats(
        plannedDays: List<PlannedDay>
    ): List<MonthlyUsageStats> {
        // Group planned days by month
        val monthlyData = plannedDays
            .groupBy { plannedDay ->
                // Extract year-month from date (yyyy-MM-dd format)
                plannedDay.date.take(7) // Gets "yyyy-MM"
            }
            .map { (month, days) ->
                val outfitCount = days.sumOf { it.items.size }
                MonthlyUsageStats(
                    month = month,
                    outfitCount = outfitCount,
                    clothesCount = 0 // Simplified - would need to count actual clothes per outfit
                )
            }
            .sortedBy { it.month }

        return monthlyData
    }

    private fun calculateTopUsedOutfits(
        outfits: List<Outfit>,
        plannedDays: List<PlannedDay>
    ): List<TopUsedOutfit> {
        // Count usage per outfit
        val usageCounts = plannedDays
            .flatMap { it.items }
            .groupingBy { it.outfitId }
            .eachCount()

        // Map to TopUsedOutfit, sorted by usage
        return usageCounts
            .map { (outfitId, count) ->
                val outfit = outfits.find { it.id == outfitId }
                TopUsedOutfit(
                    outfitId = outfitId,
                    outfitName = outfit?.name ?: "Outfit sconosciuto",
                    usageCount = count,
                    thumbnailUrl = outfit?.thumbnailUrl
                )
            }
            .sortedByDescending { it.usageCount }
            .take(5) // Top 5
    }
}
